<script setup>
import { ref, onMounted, computed } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const users = ref([])
const loading = ref(false)
const error = ref('')
const selected = ref(null)
const orders = ref([])
const suggestions = ref([])

// Filters & pagination
const search = ref('')
const page = ref(1)
const pageSize = ref(10)
const filtered = computed(() => {
  let arr = users.value
  if (search.value.trim()) {
    const q = search.value.trim().toLowerCase()
    arr = arr.filter(u =>
      String(u.id).includes(q) ||
      String(u.fullName || '').toLowerCase().includes(q) ||
      String(u.username || '').toLowerCase().includes(q) ||
      String(u.email || '').toLowerCase().includes(q)
    )
  }
  return arr
})
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)))
const paginated = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

// Metrics and export
const metrics = computed(() => {
  const out = { count: filtered.value.length, admins: 0, users: 0 }
  for (const u of filtered.value) {
    if (u.role === 'ADMIN') out.admins++
    else out.users++
  }
  return out
})

const exportCsv = () => {
  const cols = ['id','username','fullName','email','role']
  const lines = [cols.join(',')]
  for (const u of filtered.value) {
    const row = cols.map(k => {
      const val = u[k] == null ? '' : String(u[k]).replaceAll('"','""')
      return `"${val}"`
    })
    lines.push(row.join(','))
  }
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `users_export_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const load = async () => {
  loading.value = true
  error.value = ''
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  const user = localStorage.getItem('user')
  console.log('Загрузка пользователей. userId:', userId, 'user:', user)
  
  if (!userId) {
    error.value = 'Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.'
    loading.value = false
    return
  }
  
  try {
    console.log('Отправка запроса на /admin/users')
    const response = await admin.users.list()
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    console.log('response.data type:', typeof response.data)
    console.log('response.data isArray:', Array.isArray(response.data))
    
    // Проверяем разные форматы ответа
    let data = response.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      // Если это объект, возможно данные внутри
      if (data.data && Array.isArray(data.data)) {
        data = data.data
      } else if (data.users && Array.isArray(data.users)) {
        data = data.users
      } else if (data.content && Array.isArray(data.content)) {
        data = data.content
      }
    }
    
    users.value = Array.isArray(data) ? data : []
    console.log('Загружено пользователей:', users.value.length)
    
    if (!Array.isArray(users.value)) {
      console.error('Ожидался массив, получено:', users.value)
      users.value = []
    }
  } catch (e) {
    console.error('Ошибка загрузки пользователей:', e)
    console.error('Error response:', e.response)
    console.error('Error message:', e.message)
    error.value = e.response?.data?.message || e.response?.data || e.message || 'Ошибка загрузки'
    users.value = []
  } finally {
    loading.value = false
  }
}

const emailError = ref('')
const usernameError = ref('')

const selectUser = async (u) => {
  selected.value = u
  const { data } = await admin.users.orders(u.id)
  orders.value = data
  try {
    const sug = await admin.users.suggestions(u.id)
    suggestions.value = sug.data
  } catch { suggestions.value = [] }
}

const setRole = async (u, role) => {
  if (!confirm(`Изменить роль пользователя #${u.id} на ${role}?`)) return
  await admin.users.update(u.id, { role })
  await load()
}

const removeUser = async (u) => {
  if (!confirm('Удалить пользователя?')) return
  await admin.users.remove(u.id)
  selected.value = null
  await load()
}

// Generate one-time admin invite code for user
const generatedCode = ref('')
const generating = ref(false)
const generateAdminCode = async () => {
  generating.value = true
  generatedCode.value = ''
  try {
    const { data } = await admin.invites.create(24)
    generatedCode.value = data?.invite?.code || data?.code || ''
    if (!generatedCode.value) alert('Код не получен')
  } catch (e) {
    alert(e.response?.data || 'Не удалось создать код')
  } finally {
    generating.value = false
  }
}

const copyCode = async () => {
  if (!generatedCode.value) return
  try {
    await navigator.clipboard.writeText(generatedCode.value)
    alert('Код скопирован')
  } catch {}
}

onMounted(load)
</script>

<template>
  <div class="max-w-7xl mx-auto mt-10">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Панель управления · Пользователи
      </h1>
      <p class="text-brown-600">Управление пользователями и их правами</p>
    </div>
    <AdminNav />
    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <div class="lg:col-span-2">
      <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6 mb-6">
        <div class="flex flex-wrap gap-4 mb-4">
          <div class="flex-1 min-w-[240px]">
            <input 
              v-model="search" 
              placeholder="Поиск по имени, email, username, #" 
              class="input-field w-full"
            />
          </div>
          <select v-model.number="pageSize" class="input-field min-w-[100px]">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
          <button class="btn-secondary whitespace-nowrap" @click="exportCsv">
            <span class="flex items-center gap-2">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Экспорт CSV
            </span>
          </button>
        </div>
        <div class="grid grid-cols-3 gap-4">
          <div class="bg-gradient-to-br from-cookie-50 to-cookie-100 rounded-xl p-4 border-2 border-cookie-200">
            <div class="text-sm text-brown-600 mb-1">Всего</div>
            <div class="text-2xl font-extrabold text-brown-800">{{ metrics.count }}</div>
          </div>
          <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-4 border-2 border-blue-200">
            <div class="text-sm text-blue-700 mb-1">Админов</div>
            <div class="text-2xl font-extrabold text-blue-800">{{ metrics.admins }}</div>
          </div>
          <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-4 border-2 border-green-200">
            <div class="text-sm text-green-700 mb-1">Пользователей</div>
            <div class="text-2xl font-extrabold text-green-800">{{ metrics.users }}</div>
          </div>
        </div>
      </div>
      <div v-if="loading" class="text-center py-16">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-cookie-600 border-t-transparent"></div>
        <p class="text-brown-600 mt-4 font-medium">Загрузка пользователей...</p>
      </div>
      <div v-if="error" class="mb-6 p-4 bg-red-50 border-2 border-red-200 text-red-700 rounded-xl">
        <div class="flex items-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {{ error }}
        </div>
      </div>
      <div v-if="!loading && !error && paginated.length === 0" class="text-center py-16 bg-white rounded-2xl border-2 border-beige-200">
        <div class="w-24 h-24 mx-auto mb-4 rounded-full bg-gradient-to-br from-beige-100 to-cookie-100 flex items-center justify-center">
          <svg class="w-12 h-12 text-brown-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
        </div>
        <p class="text-brown-600 font-medium text-lg">Пользователи не найдены</p>
      </div>
      <div v-if="!loading && paginated.length > 0" class="space-y-3">
        <div 
          v-for="u in paginated" 
          :key="u.id" 
          class="bg-white rounded-xl border-2 border-beige-200 p-5 hover:border-brown-300 hover:shadow-lg transition-all duration-300"
        >
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <div class="font-bold text-lg text-brown-800 mb-1">
                {{ u.fullName }} 
                <span class="text-sm font-normal text-brown-500">({{ u.username }})</span>
              </div>
              <div class="text-brown-600 text-sm mb-2">{{ u.email }}</div>
              <div class="flex items-center gap-2">
                <span 
                  class="px-3 py-1 rounded-full text-xs font-semibold"
                  :class="u.role === 'ADMIN' ? 'bg-purple-100 text-purple-800' : 'bg-green-100 text-green-800'"
                >
                  {{ u.role === 'ADMIN' ? 'Администратор' : 'Пользователь' }}
                </span>
              </div>
            </div>
            <div class="flex flex-wrap gap-2">
              <button 
                class="px-4 py-2 rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition-all duration-300 transform hover:scale-105 text-sm font-semibold" 
                @click="selectUser(u)"
              >
                Заказы
              </button>
              <button 
                class="px-4 py-2 rounded-lg bg-purple-500 text-white hover:bg-purple-600 transition-all duration-300 transform hover:scale-105 text-sm font-semibold" 
                @click="setRole(u, 'ADMIN')"
              >
                Админ
              </button>
              <button 
                class="px-4 py-2 rounded-lg bg-green-500 text-white hover:bg-green-600 transition-all duration-300 transform hover:scale-105 text-sm font-semibold" 
                @click="setRole(u, 'USER')"
              >
                Юзер
              </button>
              <button 
                class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 text-sm font-semibold" 
                @click="removeUser(u)"
              >
                Удалить
              </button>
            </div>
          </div>
        </div>
      </div>
      <div class="flex items-center justify-between mt-6 p-4 bg-white rounded-xl border-2 border-beige-200">
        <div class="text-sm font-semibold text-brown-700">Страница {{ page }} из {{ totalPages }}</div>
        <div class="flex gap-3">
          <button 
            class="btn-secondary" 
            :disabled="page <= 1" 
            @click="page = Math.max(1, page - 1)"
          >
            ← Назад
          </button>
          <button 
            class="btn-secondary" 
            :disabled="page >= totalPages" 
            @click="page = Math.min(totalPages, page + 1)"
          >
            Вперёд →
          </button>
        </div>
      </div>
    </div>
    
    <div v-if="selected" class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6">
      <h2 class="text-2xl font-extrabold mb-4 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Пользователь #{{ selected.id }}
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Имя</label>
          <input v-model="selected.fullName" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Username</label>
          <input v-model="selected.username" @blur="usernameError = !selected.username?.trim() ? 'Username обязателен' : ''" class="input-field" />
          <div v-if="usernameError" class="text-xs text-red-600 mt-1">{{ usernameError }}</div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Email</label>
          <input v-model="selected.email" @blur="emailError = (/^\S+@\S+\.\S+$/.test(selected.email) ? '' : 'Некорректный email')" class="input-field" />
          <div v-if="emailError" class="text-xs text-red-600 mt-1">{{ emailError }}</div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Avatar URL</label>
          <input v-model="selected.avatarUrl" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Роль</label>
          <select v-model="selected.role" class="input-field">
            <option>USER</option>
            <option>ADMIN</option>
          </select>
        </div>
        <div class="flex flex-wrap gap-3 items-end">
          <button class="btn-primary" @click="admin.users.update(selected.id, { fullName: selected.fullName, username: selected.username, email: selected.email, avatarUrl: selected.avatarUrl, role: selected.role })">Сохранить</button>
          <button class="btn-secondary" @click="() => selectUser(selected)">Отменить</button>
          <button :disabled="generating" class="px-4 py-2 rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 disabled:opacity-50 transition-all duration-300 transform hover:scale-105 font-semibold" @click="generateAdminCode">
            {{ generating ? 'Генерация...' : 'Сделать админом по коду' }}
          </button>
        </div>
      </div>
      <div v-if="generatedCode" class="mt-4 p-4 border-2 border-emerald-200 rounded-xl bg-gradient-to-br from-emerald-50 to-green-50 flex items-center justify-between">
        <div class="flex-1">
          <div class="text-sm font-semibold text-emerald-800 mb-1">Секретный одноразовый код:</div>
          <div class="text-xl font-bold text-emerald-900 font-mono mb-2">{{ generatedCode }}</div>
          <div class="text-xs text-emerald-700">Передайте код пользователю — он введёт его при регистрации, чтобы стать админом.</div>
        </div>
        <button class="px-4 py-2 rounded-lg bg-emerald-600 text-white hover:bg-emerald-700 transition-all duration-300 transform hover:scale-105 font-semibold" @click="copyCode">
          <span class="flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
            Копировать
          </span>
        </button>
      </div>

      <h3 class="text-xl font-bold text-brown-800 mb-4 flex items-center gap-2">
        <svg class="w-6 h-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Заказы
      </h3>
      <div class="space-y-3 max-h-[60vh] overflow-auto">
        <div v-for="o in orders" :key="o.id" class="p-4 border-2 border-beige-200 rounded-xl bg-gradient-to-br from-white to-beige-50 hover:border-brown-300 transition-all">
          <div class="flex justify-between items-center">
            <div>
              <div class="text-sm font-semibold text-brown-600">Заказ #{{ o.id }}</div>
              <div class="text-lg font-bold text-brown-800 mt-1">{{ o.totalPrice }} бун</div>
            </div>
            <span 
              class="px-3 py-1 rounded-full text-xs font-semibold"
              :class="{
                'bg-yellow-100 text-yellow-800': o.status === 'PENDING',
                'bg-blue-100 text-blue-800': o.status === 'CONFIRMED',
                'bg-green-100 text-green-800': o.status === 'DELIVERED',
                'bg-red-100 text-red-800': o.status === 'CANCELLED'
              }"
            >
              {{ o.status }}
            </span>
          </div>
        </div>
        <div v-if="!orders.length" class="text-center py-8 text-brown-500">
          Нет заказов
        </div>
      </div>
      
      <h3 class="text-xl font-bold text-brown-800 mt-8 mb-4 flex items-center gap-2">
        <svg class="w-6 h-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        Отзывы/предложения
      </h3>
      <div class="space-y-3 max-h-[40vh] overflow-auto">
        <div v-for="s in suggestions" :key="s.id" class="p-4 border-2 border-beige-200 rounded-xl bg-gradient-to-br from-white to-cookie-50 hover:border-brown-300 transition-all">
          <div class="flex justify-between items-start gap-4">
            <div class="flex-1">
              <div class="text-sm font-semibold text-brown-600 mb-1">{{ s.productName }}</div>
              <div class="font-bold text-brown-800 mb-2">{{ s.author }}</div>
              <div class="text-sm text-brown-700">{{ s.description }}</div>
            </div>
            <button 
              class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 font-semibold text-sm" 
              @click="async () => { if (confirm('Удалить запись?')) { await admin.suggestions.remove(s.id); await selectUser(selected) } }"
            >
              Удалить
            </button>
          </div>
        </div>
        <div v-if="!suggestions.length" class="text-center py-8 text-brown-500">
          Нет отзывов/предложений
        </div>
      </div>
    </div>
    </div>
  </div>
</template>



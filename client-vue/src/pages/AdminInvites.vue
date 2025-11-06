<script setup>
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const code = ref('')
const hours = ref(24)
const creating = ref(false)
const list = ref([])
const loading = ref(false)

const createInvite = async () => {
  creating.value = true
  code.value = ''
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  if (!userId) {
    alert('Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.')
    creating.value = false
    return
  }
  
  try {
    console.log('Создание инвайт-кода. expiresInHours:', hours.value)
    const payload = hours.value ? { expiresInHours: hours.value } : {}
    console.log('Отправка payload:', payload)
    
    const response = await admin.invites.create(hours.value)
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Обрабатываем разные форматы ответа
    const data = response.data
    if (data?.invite?.code) {
      code.value = data.invite.code
    } else if (data?.code) {
      code.value = data.code
    } else if (typeof data === 'string') {
      code.value = data
    } else {
      console.error('Неожиданный формат ответа:', data)
      alert('Код создан, но не удалось его получить из ответа сервера')
    }
    
    // Перезагружаем список после создания
    await load()
  } catch (e) {
    console.error('Ошибка создания инвайт-кода:', e)
    console.error('Error response:', e.response)
    const errorMsg = e.response?.data?.message || e.response?.data || e.message || 'Не удалось создать код'
    alert(errorMsg)
  } finally {
    creating.value = false
  }
}

const load = async () => {
  loading.value = true
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  console.log('Загрузка инвайт-кодов. userId:', userId)
  
  if (!userId) {
    loading.value = false
    return
  }
  
  try {
    console.log('Отправка запроса на /admin/invites')
    const response = await admin.invites.list()
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Проверяем разные форматы ответа
    let data = response.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      if (data.data && Array.isArray(data.data)) {
        data = data.data
      } else if (data.invites && Array.isArray(data.invites)) {
        data = data.invites
      } else if (data.content && Array.isArray(data.content)) {
        data = data.content
      }
    }
    
    list.value = Array.isArray(data) ? data : []
    console.log('Загружено инвайт-кодов:', list.value.length)
    
    if (!Array.isArray(list.value)) {
      console.error('Ожидался массив, получено:', list.value)
      list.value = []
    }
  } catch (e) {
    console.error('Ошибка загрузки инвайт-кодов:', e)
    console.error('Error response:', e.response)
    list.value = []
  } finally {
    loading.value = false
  }
}

const revoke = async (inv) => {
  if (!confirm('Удалить инвайт?')) return
  await admin.invites.remove(inv.id)
  await load()
}

const copyCode = async () => {
  if (!code.value) return
  try {
    await navigator.clipboard.writeText(code.value)
    alert('Код скопирован в буфер обмена!')
  } catch (e) {
    // Fallback для старых браузеров
    const textArea = document.createElement('textarea')
    textArea.value = code.value
    document.body.appendChild(textArea)
    textArea.select()
    try {
      document.execCommand('copy')
      alert('Код скопирован в буфер обмена!')
    } catch (err) {
      alert('Не удалось скопировать код. Скопируйте вручную: ' + code.value)
    }
    document.body.removeChild(textArea)
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-7xl mx-auto mt-10">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Панель управления · Инвайт‑коды
      </h1>
      <p class="text-brown-600">Создание и управление инвайт-кодами для администраторов</p>
    </div>
    <AdminNav />
    <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6">
    <div class="flex items-end gap-4 mb-6">
      <div>
        <label class="block text-sm font-semibold text-brown-700 mb-2">Срок действия (часы)</label>
        <input type="number" v-model.number="hours" min="1" class="input-field w-32" />
      </div>
      <button :disabled="creating" class="btn-primary disabled:opacity-50" @click="createInvite">
        {{ creating ? 'Создание...' : 'Создать код' }}
      </button>
    </div>
    <div v-if="code" class="p-6 bg-gradient-to-br from-emerald-50 to-green-50 border-2 border-emerald-200 rounded-xl mb-6">
      <div class="flex items-center justify-between">
        <div class="flex-1">
          <p class="text-sm font-semibold text-emerald-800 mb-2">Секретный код создан:</p>
          <p class="text-2xl font-bold text-emerald-900 font-mono mb-3">{{ code }}</p>
          <p class="text-sm text-emerald-700">Передайте этот код новому администратору. Код можно использовать один раз при регистрации.</p>
        </div>
        <button 
          @click="copyCode" 
          class="px-6 py-3 bg-emerald-600 text-white rounded-xl hover:bg-emerald-700 transition-all duration-300 transform hover:scale-105 font-semibold"
          title="Копировать код"
        >
          <span class="flex items-center gap-2">
            <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
            </svg>
            Копировать
          </span>
        </button>
      </div>
    </div>

    <div class="border-t-2 border-beige-200 pt-6 mt-6">
      <h2 class="text-xl font-bold mb-4 text-brown-800">Список инвайтов</h2>
      <div v-if="loading" class="text-center py-16">
        <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-cookie-600 border-t-transparent"></div>
        <p class="text-brown-600 mt-4 font-medium">Загрузка инвайт-кодов...</p>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full text-left">
          <thead>
            <tr class="border-b-2 border-beige-200 bg-gradient-to-r from-brown-50 to-beige-50">
              <th class="p-4 font-bold text-brown-800">ID</th>
              <th class="p-4 font-bold text-brown-800">Код</th>
              <th class="p-4 font-bold text-brown-800">Использован</th>
              <th class="p-4 font-bold text-brown-800">Использовал аккаунт</th>
              <th class="p-4 font-bold text-brown-800">Создан</th>
              <th class="p-4 font-bold text-brown-800">Действует до</th>
              <th class="p-4 font-bold text-brown-800">Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="inv in list" :key="inv.id" class="border-b border-beige-100 hover:bg-cookie-50/30 transition-colors">
              <td class="p-4 text-brown-700">{{ inv.id }}</td>
              <td class="p-4 font-mono font-semibold text-brown-800">{{ inv.code }}</td>
              <td class="p-4">
                <span 
                  class="px-3 py-1 rounded-full text-xs font-semibold"
                  :class="inv.used ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-600'"
                >
                  {{ inv.used ? 'Да' : 'Нет' }}
                </span>
              </td>
            <td class="p-2">
              <div v-if="inv.usedBy">
                <div class="font-semibold">{{ inv.usedBy.fullName || inv.usedBy.username }}</div>
                <div class="text-xs text-gray-500">{{ inv.usedBy.email }}</div>
                <div class="text-xs text-gray-400">ID: {{ inv.usedBy.id }}</div>
              </div>
              <span v-else class="text-gray-400">—</span>
            </td>
            <td class="p-2 text-xs text-gray-600">
              <div v-if="inv.createdBy">
                {{ inv.createdBy.fullName || inv.createdBy.username }}
              </div>
              <div v-else>—</div>
              <div class="text-xs text-gray-400 mt-1">{{ inv.createdAt?.slice(0,16).replace('T',' ') || '—' }}</div>
            </td>
            <td class="p-2">
              <div v-if="inv.expiresAt" class="text-xs">
                <div>{{ new Date(inv.expiresAt).toLocaleString('ru-RU') }}</div>
                <div :class="new Date(inv.expiresAt) < new Date() ? 'text-red-600 font-semibold' : 'text-green-600'">
                  {{ new Date(inv.expiresAt) < new Date() ? 'Истёк' : 'Активен' }}
                </div>
              </div>
              <span v-else class="text-gray-400">Без ограничений</span>
            </td>
              <td class="p-4">
                <button 
                  class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 font-semibold text-sm" 
                  @click="revoke(inv)"
                >
                  Удалить
                </button>
              </td>
            </tr>
            <tr v-if="!list.length && !loading">
              <td class="p-8 text-center text-brown-500" colspan="7">
                <div class="flex flex-col items-center">
                  <svg class="w-12 h-12 text-brown-300 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                  </svg>
                  Пока пусто
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
    </div>
  </div>
</template>



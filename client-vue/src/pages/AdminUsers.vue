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
  try {
    const { data } = await admin.users.list()
    users.value = data
  } catch (e) {
    error.value = e.response?.data || 'Ошибка загрузки'
  } finally {
    loading.value = false
  }
}

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

onMounted(load)
</script>

<template>
  <div class="max-w-6xl mx-auto mt-10 grid grid-cols-1 md:grid-cols-2 gap-8">
    <div>
      <h1 class="text-2xl font-bold mb-4">Админ · Пользователи</h1>
      <AdminNav />
      <div class="flex flex-wrap gap-3 mb-4">
        <input v-model="search" placeholder="Поиск по имени, email, username, #" class="border rounded px-3 py-2 flex-1 min-w-[240px]" />
        <select v-model.number="pageSize" class="border rounded px-3 py-2">
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
        <button class="px-3 py-2 rounded bg-slate-200" @click="exportCsv">Экспорт CSV</button>
      </div>
      <div class="flex gap-4 items-center mb-4 text-sm text-slate-700">
        <div><b>Всего:</b> {{ metrics.count }}</div>
        <div><b>Админов:</b> {{ metrics.admins }}</div>
        <div><b>Пользователей:</b> {{ metrics.users }}</div>
      </div>
      <div v-if="loading" class="text-gray-500">Загрузка…</div>
      <div v-if="error" class="text-red-600 mb-4">{{ error }}</div>
      <ul class="bg-white rounded-xl shadow divide-y">
        <li v-for="u in paginated" :key="u.id" class="p-4 flex items-center justify-between hover:bg-slate-50">
          <div>
            <div class="font-semibold">{{ u.fullName }} <span class="text-xs text-slate-500">({{ u.username }})</span></div>
            <div class="text-slate-500 text-sm">{{ u.email }} · Роль: <b>{{ u.role }}</b></div>
          </div>
          <div class="flex gap-2">
            <button class="px-3 py-1 rounded bg-slate-200 hover:bg-slate-300" @click="selectUser(u)">Заказы</button>
            <button class="px-3 py-1 rounded bg-slate-200 hover:bg-slate-300" @click="setRole(u, 'ADMIN')">Сделать админом</button>
            <button class="px-3 py-1 rounded bg-slate-200 hover:bg-slate-300" @click="setRole(u, 'USER')">Сделать юзером</button>
            <button class="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600" @click="removeUser(u)">Удалить</button>
          </div>
        </li>
      </ul>
      <div class="flex items-center justify-between mt-4">
        <div class="text-sm text-slate-600">Стр. {{ page }} из {{ totalPages }}</div>
        <div class="flex gap-2">
          <button class="px-3 py-1 rounded bg-slate-200" :disabled="page <= 1" @click="page = Math.max(1, page - 1)">Назад</button>
          <button class="px-3 py-1 rounded bg-slate-200" :disabled="page >= totalPages" @click="page = Math.min(totalPages, page + 1)">Вперёд</button>
        </div>
      </div>
    </div>
    
    <div v-if="selected" class="bg-white rounded-xl shadow p-4">
      <h2 class="text-xl font-semibold mb-3">Пользователь #{{ selected.id }}</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-3 mb-4">
        <div>
          <label class="block text-xs text-slate-500 mb-1">Имя</label>
          <input v-model="selected.fullName" class="border rounded px-2 py-1 w-full" />
        </div>
        <div>
          <label class="block text-xs text-slate-500 mb-1">Username</label>
          <input v-model="selected.username" class="border rounded px-2 py-1 w-full" />
        </div>
        <div>
          <label class="block text-xs text-slate-500 mb-1">Email</label>
          <input v-model="selected.email" class="border rounded px-2 py-1 w-full" />
        </div>
        <div>
          <label class="block text-xs text-slate-500 mb-1">Avatar URL</label>
          <input v-model="selected.avatarUrl" class="border rounded px-2 py-1 w-full" />
        </div>
        <div>
          <label class="block text-xs text-slate-500 mb-1">Роль</label>
          <select v-model="selected.role" class="border rounded px-2 py-1 w-full">
            <option>USER</option>
            <option>ADMIN</option>
          </select>
        </div>
        <div class="flex gap-2 items-end">
          <button class="px-3 py-1 rounded bg-black text-white" @click="admin.users.update(selected.id, { fullName: selected.fullName, username: selected.username, email: selected.email, avatarUrl: selected.avatarUrl, role: selected.role })">Сохранить</button>
          <button class="px-3 py-1 rounded bg-slate-200" @click="() => selectUser(selected)">Отменить</button>
        </div>
      </div>

      <h3 class="text-lg font-semibold mb-2">Заказы</h3>
      <div class="space-y-2 max-h-[60vh] overflow-auto">
        <div v-for="o in orders" :key="o.id" class="p-3 border rounded">
          <div class="text-sm text-slate-600">Заказ #{{ o.id }} · {{ o.status }}</div>
          <div class="font-semibold">{{ o.totalPrice }}</div>
        </div>
      </div>
      
      <h3 class="text-lg font-semibold mt-6 mb-2">Отзывы/предложения</h3>
      <div class="space-y-2 max-h-[40vh] overflow-auto">
        <div v-for="s in suggestions" :key="s.id" class="p-3 border rounded flex justify-between items-start gap-3">
          <div class="text-sm text-slate-600">{{ s.productName }}</div>
          <div class="flex-1">
            <div class="font-semibold">{{ s.author }}</div>
            <div class="text-sm">{{ s.description }}</div>
          </div>
          <button class="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600" @click="async () => { if (confirm('Удалить запись?')) { await admin.suggestions.remove(s.id); await selectUser(selected) } }">Удалить</button>
        </div>
        <div v-if="!suggestions.length" class="text-sm text-slate-500">Нет отзывов/предложений</div>
      </div>
    </div>
  </div>
</template>



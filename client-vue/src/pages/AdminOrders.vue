<script setup>
import { ref, onMounted, computed, onErrorCaptured } from 'vue'
import { useRouter } from 'vue-router'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const router = useRouter()
const { admin } = useApi()

// Обработка ошибок компонента
onErrorCaptured((err, instance, info) => {
  console.error('AdminOrders component error:', err, info)
  // Не редиректим, просто логируем ошибку
  return true // Разрешаем дальнейшее распространение ошибки для отладки
})
const orders = ref([])
const loading = ref(false)
const error = ref('')
const editing = ref(null)
const form = ref({ recipient: '', address: '', comment: '', paymentMethod: 'CASH' })

// Filters & pagination
const search = ref('')
const statusFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const page = ref(1)
const pageSize = ref(10)

const filtered = computed(() => {
  let arr = orders.value
  if (statusFilter.value) {
    arr = arr.filter(o => o.status === statusFilter.value)
  }
  if (dateFrom.value) {
    const from = new Date(dateFrom.value)
    arr = arr.filter(o => o.createdAt && new Date(o.createdAt) >= from)
  }
  if (dateTo.value) {
    // Include entire day
    const to = new Date(dateTo.value)
    to.setHours(23,59,59,999)
    arr = arr.filter(o => o.createdAt && new Date(o.createdAt) <= to)
  }
  if (search.value.trim()) {
    const q = search.value.trim().toLowerCase()
    arr = arr.filter(o =>
      String(o.id).includes(q) ||
      String(o.userId || '').toLowerCase().includes(q) ||
      String(o.recipient || '').toLowerCase().includes(q) ||
      String(o.address || '').toLowerCase().includes(q)
    )
  }
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)))
const paginated = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

// Metrics
const metrics = computed(() => {
  const out = { count: filtered.value.length, sum: 0, byStatus: {} }
  for (const o of filtered.value) {
    const st = o.status || 'UNKNOWN'
    out.byStatus[st] = (out.byStatus[st] || 0) + 1
    const v = Number(o.totalPrice || 0)
    if (!Number.isNaN(v)) out.sum += v
  }
  return out
})

// Build data series for last 14 days
const lastDays = 14
const seriesByDay = computed(() => {
  const map = new Map()
  const today = new Date()
  for (let i = lastDays - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    const key = d.toISOString().slice(0,10)
    map.set(key, { date: key, count: 0, sum: 0 })
  }
  for (const o of filtered.value) {
    if (!o.createdAt) continue
    const key = new Date(o.createdAt).toISOString().slice(0,10)
    if (map.has(key)) {
      const it = map.get(key)
      it.count++
      const v = Number(o.totalPrice || 0)
      if (!Number.isNaN(v)) it.sum += v
    }
  }
  return Array.from(map.values())
})

const maxCount = computed(() => Math.max(1, ...seriesByDay.value.map(d => d.count)))
const maxSum = computed(() => Math.max(1, ...seriesByDay.value.map(d => d.sum)))

const exportCsv = () => {
  const cols = ['id','userId','totalPrice','status','createdAt','recipient','address','promoCode']
  const lines = [cols.join(',')]
  for (const o of filtered.value) {
    const row = cols.map(k => {
      const val = o[k] == null ? '' : String(o[k]).replaceAll('"','""')
      return `"${val}"`
    })
    lines.push(row.join(','))
  }
  const blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `orders_export_${new Date().toISOString().slice(0,10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const load = async () => {
  loading.value = true
  error.value = ''
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  console.log('Загрузка заказов. userId:', userId)
  
  if (!userId) {
    error.value = 'Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.'
    loading.value = false
    return
  }
  
  try {
    console.log('Отправка запроса на /admin/orders')
    const response = await admin.orders.list()
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Проверяем разные форматы ответа
    let data = response.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      if (data.data && Array.isArray(data.data)) {
        data = data.data
      } else if (data.orders && Array.isArray(data.orders)) {
        data = data.orders
      } else if (data.content && Array.isArray(data.content)) {
        data = data.content
      }
    }
    
    orders.value = Array.isArray(data) ? data : []
    console.log('Загружено заказов:', orders.value.length)
    
    if (!Array.isArray(orders.value)) {
      console.error('Ожидался массив, получено:', orders.value)
      orders.value = []
    }
  } catch (e) {
    console.error('Ошибка загрузки заказов:', e)
    console.error('Error response:', e.response)
    error.value = e.response?.data?.message || e.response?.data || e.message || 'Ошибка загрузки'
    orders.value = []
  } finally {
    loading.value = false
  }
  }

  const changeStatus = async (orderId, status) => {
  try {
    await admin.orders.changeStatus(orderId, status)
    await load()
  } catch (e) {
    alert(e.response?.data || 'Не удалось сменить статус')
  }
  }

  onMounted(async () => {
  console.log('AdminOrders component mounted')
  try {
    await load()
  } catch (err) {
    console.error('Error in AdminOrders onMounted:', err)
  }
  })

const startEdit = (o) => {
  editing.value = o.id
  form.value = {
    recipient: o.recipient || '',
    address: o.address || '',
    comment: o.comment || '',
    paymentMethod: o.paymentMethod || 'CASH'
  }
}

const saveEdit = async (o) => {
  try {
    await admin.orders.update(o.id, form.value)
    editing.value = null
    await load()
  } catch (e) {
    alert(e.response?.data || 'Не удалось сохранить изменения')
  }
}

const cancelEdit = () => {
  editing.value = null
}

// Details modal
const showDetails = ref(false)
const details = ref(null)
const openDetails = async (o) => {
  try {
    const { data } = await admin.orders.get(o.id)
    details.value = data
    showDetails.value = true
  } catch (e) {
    alert(e.response?.data || 'Не удалось загрузить детали заказа')
  }
}
const closeDetails = () => { showDetails.value = false; details.value = null }
</script>

<template>
  <div class="max-w-7xl mx-auto mt-10">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Панель управления · Заказы
      </h1>
      <p class="text-brown-600">Управление всеми заказами и их статусами</p>
    </div>
    <AdminNav />
    <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6 mb-6">
      <h2 class="text-xl font-bold text-brown-800 mb-4 flex items-center gap-2">
        <svg class="w-5 h-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.293A1 1 0 013 6.586V4z" />
        </svg>
        Фильтры и поиск
      </h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="lg:col-span-2">
          <label class="block text-sm font-semibold text-brown-700 mb-2">Поиск</label>
          <input 
            v-model="search" 
            placeholder="Поиск по #, userId, получателю, адресу" 
            class="input-field w-full"
          />
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Статус</label>
          <select v-model="statusFilter" class="input-field w-full">
            <option value="">Все статусы</option>
            <option value="PENDING">В обработке</option>
            <option value="CONFIRMED">Подтверждён</option>
            <option value="DELIVERED">Доставлен</option>
            <option value="CANCELLED">Отменён</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">На странице</label>
          <select v-model.number="pageSize" class="input-field w-full">
            <option :value="10">10</option>
            <option :value="20">20</option>
            <option :value="50">50</option>
          </select>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Дата от</label>
          <input type="date" v-model="dateFrom" class="input-field w-full" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Дата до</label>
          <input type="date" v-model="dateTo" class="input-field w-full" />
        </div>
        <div class="lg:col-span-2 flex items-end">
          <button class="btn-secondary w-full whitespace-nowrap" @click="exportCsv">
            <span class="flex items-center justify-center gap-2">
              <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
              Экспорт CSV
            </span>
          </button>
        </div>
      </div>
    </div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div class="bg-gradient-to-br from-cookie-50 to-cookie-100 rounded-xl p-5 border-2 border-cookie-200 shadow-md hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm font-semibold text-brown-600 uppercase tracking-wide">Всего заказов</div>
          <svg class="w-5 h-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
        </div>
        <div class="text-3xl font-extrabold text-brown-800">{{ metrics.count }}</div>
      </div>
      <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-5 border-2 border-green-200 shadow-md hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm font-semibold text-green-700 uppercase tracking-wide">Общая сумма</div>
          <svg class="w-5 h-5 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="text-3xl font-extrabold text-green-800">{{ Math.round(metrics.sum) }} бун</div>
      </div>
      <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-5 border-2 border-blue-200 shadow-md hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm font-semibold text-blue-700 uppercase tracking-wide">В обработке</div>
          <svg class="w-5 h-5 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="text-3xl font-extrabold text-blue-800">{{ metrics.byStatus.PENDING || 0 }}</div>
      </div>
      <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-5 border-2 border-purple-200 shadow-md hover:shadow-lg transition-shadow">
        <div class="flex items-center justify-between mb-2">
          <div class="text-sm font-semibold text-purple-700 uppercase tracking-wide">Доставлено</div>
          <svg class="w-5 h-5 text-purple-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
          </svg>
        </div>
        <div class="text-3xl font-extrabold text-purple-800">{{ metrics.byStatus.DELIVERED || 0 }}</div>
      </div>
    </div>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
    <div class="p-6 bg-white rounded-2xl shadow-lg border-2 border-beige-200">
      <div class="text-lg font-bold text-brown-800 mb-4 flex items-center gap-2">
        <svg class="w-6 h-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
        </svg>
        Распределение по статусам
      </div>
      <div class="flex items-end gap-3 h-48 px-2">
        <div v-for="(v,k) in metrics.byStatus" :key="k" class="flex-1 text-center group">
          <div 
            class="bg-gradient-to-t from-cookie-500 to-cookie-600 w-full rounded-t-xl transition-all duration-300 group-hover:from-cookie-600 group-hover:to-cookie-700 shadow-md cursor-pointer" 
            :style="{ height: Math.max(12, (v / Math.max(1, ...Object.values(metrics.byStatus))) * 180) + 'px' }"
            :title="`${k === 'PENDING' ? 'В обработке' : k === 'CONFIRMED' ? 'Подтверждён' : k === 'DELIVERED' ? 'Доставлен' : 'Отменён'}: ${v}`"
          ></div>
          <div class="text-xs mt-2 font-semibold text-brown-700">{{ k === 'PENDING' ? 'В обработке' : k === 'CONFIRMED' ? 'Подтверждён' : k === 'DELIVERED' ? 'Доставлен' : 'Отменён' }}</div>
          <div class="text-sm font-bold text-brown-800 mt-1">{{ v }}</div>
        </div>
      </div>
    </div>
    <div class="p-6 bg-white rounded-2xl shadow-lg border-2 border-beige-200">
      <div class="text-lg font-bold text-brown-800 mb-4 flex items-center gap-2">
        <svg class="w-6 h-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 12l3-3 3 3 4-4M8 21l4-4 4 4M3 4h18M4 4h16v12a1 1 0 01-1 1H5a1 1 0 01-1-1V4z" />
        </svg>
        Заказы по дням ({{ lastDays }} дн.)
      </div>
      <div class="bg-gradient-to-br from-beige-50 to-cookie-50 rounded-xl p-4 border-2 border-beige-200">
        <svg viewBox="0 0 300 140" class="w-full h-48">
          <defs>
            <linearGradient id="cookieGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" style="stop-color:#FF6B9D;stop-opacity:0.8" />
              <stop offset="100%" style="stop-color:#FF6B9D;stop-opacity:0.3" />
            </linearGradient>
            <linearGradient id="brownGradient" x1="0%" y1="0%" x2="0%" y2="100%">
              <stop offset="0%" style="stop-color:#8B4513;stop-opacity:0.8" />
              <stop offset="100%" style="stop-color:#8B4513;stop-opacity:0.3" />
            </linearGradient>
          </defs>
          <polyline
            fill="url(#cookieGradient)"
            stroke="#FF6B9D"
            stroke-width="3"
            :points="seriesByDay.map((d,i) => {
              const x = (i/(seriesByDay.length-1))*280 + 10
              const y = 130 - (d.count/maxCount)*110
              return `${x},${y} ${x},130`
            }).join(' ')"
          />
          <polyline
            fill="none"
            stroke="#FF6B9D"
            stroke-width="3"
            :points="seriesByDay.map((d,i) => {
              const x = (i/(seriesByDay.length-1))*280 + 10
              const y = 130 - (d.count/maxCount)*110
              return `${x},${y}`
            }).join(' ')"
          />
          <polyline
            fill="none"
            stroke="#8B4513"
            stroke-width="3"
            :points="seriesByDay.map((d,i) => {
              const x = (i/(seriesByDay.length-1))*280 + 10
              const y = 130 - (d.sum/maxSum)*110
              return `${x},${y}`
            }).join(' ')"
          />
        </svg>
      </div>
      <div class="flex gap-4 mt-4 text-xs text-brown-600 justify-center">
        <div class="flex items-center gap-2">
          <div class="w-4 h-1 bg-cookie-500 rounded"></div>
          <span class="font-semibold">Количество</span>
        </div>
        <div class="flex items-center gap-2">
          <div class="w-4 h-1 bg-brown-700 rounded"></div>
          <span class="font-semibold">Сумма</span>
        </div>
      </div>
    </div>
  </div>
    <div v-if="loading" class="text-center py-16">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-cookie-600 border-t-transparent"></div>
      <p class="text-brown-600 mt-4 font-medium">Загрузка заказов...</p>
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
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      </div>
      <p class="text-brown-600 font-medium text-lg">Заказы не найдены</p>
    </div>
    <div v-if="!loading && paginated.length > 0" class="w-full">
      <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 overflow-hidden w-full">
        <div class="w-full">
          <table class="w-full divide-y divide-beige-200">
            <thead class="bg-gradient-to-r from-brown-50 to-beige-50">
              <tr>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">#</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Пользователь</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Сумма</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Статус</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Дата</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Действия</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-beige-100">
              <tr v-for="o in paginated" :key="o.id" class="hover:bg-cookie-50/30 transition-colors">
                <td class="px-6 py-4 whitespace-nowrap">
                  <button 
                    class="font-bold text-cookie-600 hover:text-cookie-700 hover:underline transition-colors text-sm" 
                    @click="openDetails(o)"
                  >
                    #{{ o.id }}
                  </button>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-medium text-brown-800">{{ o.userId ?? '—' }}</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm font-bold text-brown-800">{{ o.totalPrice }} бун</div>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <span 
                    class="px-3 py-1.5 inline-flex items-center rounded-full text-xs font-semibold"
                    :class="{
                      'bg-yellow-100 text-yellow-800': o.status === 'PENDING',
                      'bg-blue-100 text-blue-800': o.status === 'CONFIRMED',
                      'bg-green-100 text-green-800': o.status === 'DELIVERED',
                      'bg-red-100 text-red-800': o.status === 'CANCELLED'
                    }"
                  >
                    {{ o.status === 'PENDING' ? 'В обработке' : o.status === 'CONFIRMED' ? 'Подтверждён' : o.status === 'DELIVERED' ? 'Доставлен' : 'Отменён' }}
                  </span>
                </td>
                <td class="px-6 py-4 whitespace-nowrap">
                  <div class="text-sm text-brown-600">
                    {{ o.createdAt ? new Date(o.createdAt).toLocaleDateString('ru-RU') : '—' }}
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="flex flex-wrap gap-2">
                    <button 
                      v-if="o.status !== 'CONFIRMED' && o.status !== 'DELIVERED' && o.status !== 'CANCELLED'"
                      class="px-3 py-1.5 rounded-lg bg-blue-500 text-white hover:bg-blue-600 transition-all duration-300 transform hover:scale-105 text-xs font-semibold shadow-sm" 
                      @click="changeStatus(o.id, 'CONFIRMED')"
                      title="Подтвердить заказ"
                    >
                      ✓
                    </button>
                    <button 
                      v-if="o.status !== 'DELIVERED' && o.status !== 'CANCELLED'"
                      class="px-3 py-1.5 rounded-lg bg-green-500 text-white hover:bg-green-600 transition-all duration-300 transform hover:scale-105 text-xs font-semibold shadow-sm" 
                      @click="changeStatus(o.id, 'DELIVERED')"
                      title="Отметить как доставленный"
                    >
                      ✓✓
                    </button>
                    <button 
                      v-if="o.status !== 'CANCELLED'"
                      class="px-3 py-1.5 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 text-xs font-semibold shadow-sm" 
                      @click="changeStatus(o.id, 'CANCELLED')"
                      title="Отменить заказ"
                    >
                      ✕
                    </button>
                    <button 
                      v-if="editing !== o.id" 
                      class="px-3 py-1.5 rounded-lg bg-brown-700 text-white hover:bg-brown-800 transition-all duration-300 transform hover:scale-105 text-xs font-semibold shadow-sm" 
                      @click="startEdit(o)"
                      title="Редактировать заказ"
                    >
                      ✎
                    </button>
                  </div>
                  <div v-if="editing === o.id" class="mt-4 p-4 border-2 border-cookie-200 rounded-xl grid grid-cols-1 md:grid-cols-2 gap-4 bg-gradient-to-br from-cookie-50 to-beige-50">
                    <div>
                      <label class="block text-sm font-semibold text-brown-700 mb-2">Получатель</label>
                      <input v-model="form.recipient" class="input-field" />
                    </div>
                    <div>
                      <label class="block text-sm font-semibold text-brown-700 mb-2">Адрес</label>
                      <input v-model="form.address" class="input-field" />
                    </div>
                    <div class="md:col-span-2">
                      <label class="block text-sm font-semibold text-brown-700 mb-2">Комментарий</label>
                      <textarea v-model="form.comment" class="input-field h-24 resize-none"></textarea>
                    </div>
                    <div>
                      <label class="block text-sm font-semibold text-brown-700 mb-2">Оплата</label>
                      <select v-model="form.paymentMethod" class="input-field">
                        <option value="CASH">Наличные</option>
                        <option value="CARD_ONLINE">Карта онлайн</option>
                        <option value="CARD_ON_DELIVERY">Карта при получении</option>
                      </select>
                    </div>
                    <div class="flex gap-3 items-end md:col-span-2">
                      <button class="btn-primary" @click="saveEdit(o)">Сохранить</button>
                      <button class="btn-secondary" @click="cancelEdit">Отмена</button>
                    </div>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      <div class="flex items-center justify-between mt-6 p-5 bg-white rounded-xl border-2 border-beige-200 shadow-md">
        <div class="text-sm font-semibold text-brown-700">
          Показано {{ (page - 1) * pageSize + 1 }} - {{ Math.min(page * pageSize, filtered.length) }} из {{ filtered.length }} заказов
        </div>
        <div class="flex items-center gap-3">
          <span class="text-sm text-brown-600">Страница {{ page }} из {{ totalPages }}</span>
          <div class="flex gap-2">
            <button 
              class="px-4 py-2 rounded-lg bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50 transition-all duration-300 transform hover:scale-105 font-semibold disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100" 
              :disabled="page <= 1" 
              @click="page = Math.max(1, page - 1)"
            >
              ← Назад
            </button>
            <button 
              class="px-4 py-2 rounded-lg bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50 transition-all duration-300 transform hover:scale-105 font-semibold disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:scale-100" 
              :disabled="page >= totalPages" 
              @click="page = Math.min(totalPages, page + 1)"
            >
              Вперёд →
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Order details modal -->
  <div v-if="showDetails" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
    <div class="bg-white rounded-2xl shadow-2xl max-w-6xl w-full max-h-[90vh] overflow-y-auto border-2 border-beige-200">
      <div class="sticky top-0 bg-white border-b-2 border-beige-200 p-6 flex justify-between items-center z-10">
        <h3 class="text-2xl font-extrabold bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
          Заказ #{{ details?.id }}
        </h3>
        <button 
          class="p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95" 
          @click="closeDetails"
        >
          <svg class="w-6 h-6 text-brown-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
      </div>
      <div class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
        <div class="bg-cookie-50 rounded-xl p-4 border-2 border-cookie-200">
          <div class="text-xs font-semibold text-brown-600 uppercase mb-1">Пользователь</div>
          <div class="text-lg font-bold text-brown-800">{{ details?.userId ?? '—' }}</div>
        </div>
        <div class="bg-green-50 rounded-xl p-4 border-2 border-green-200">
          <div class="text-xs font-semibold text-green-700 uppercase mb-1">Сумма</div>
          <div class="text-lg font-bold text-green-800">{{ details?.totalPrice }} бун</div>
        </div>
        <div class="bg-blue-50 rounded-xl p-4 border-2 border-blue-200">
          <div class="text-xs font-semibold text-blue-700 uppercase mb-1">Статус</div>
          <div class="text-lg font-bold text-blue-800">{{ details?.status }}</div>
        </div>
        <div class="bg-purple-50 rounded-xl p-4 border-2 border-purple-200">
          <div class="text-xs font-semibold text-purple-700 uppercase mb-1">Промокод</div>
          <div class="text-lg font-bold text-purple-800">{{ details?.promoCode || '—' }}</div>
        </div>
        <div class="bg-yellow-50 rounded-xl p-4 border-2 border-yellow-200">
          <div class="text-xs font-semibold text-yellow-700 uppercase mb-1">Оплата</div>
          <div class="text-lg font-bold text-yellow-800">{{ details?.paymentMethod }}</div>
        </div>
        <div class="bg-pink-50 rounded-xl p-4 border-2 border-pink-200">
          <div class="text-xs font-semibold text-pink-700 uppercase mb-1">Чаевые</div>
          <div class="text-lg font-bold text-pink-800">{{ details?.tip || 0 }} бун</div>
        </div>
        <div class="bg-orange-50 rounded-xl p-4 border-2 border-orange-200">
          <div class="text-xs font-semibold text-orange-700 uppercase mb-1">Скидка</div>
          <div class="text-lg font-bold text-orange-800">{{ details?.discount || 0 }} бун</div>
        </div>
        <div class="bg-indigo-50 rounded-xl p-4 border-2 border-indigo-200">
          <div class="text-xs font-semibold text-indigo-700 uppercase mb-1">Дата создания</div>
          <div class="text-sm font-bold text-indigo-800">{{ details?.createdAt ? new Date(details.createdAt).toLocaleString('ru-RU') : '—' }}</div>
        </div>
        <div class="md:col-span-2 bg-beige-50 rounded-xl p-4 border-2 border-beige-300">
          <div class="text-xs font-semibold text-brown-700 uppercase mb-2">Получатель</div>
          <div class="text-base font-semibold text-brown-800">{{ details?.recipient || '—' }}</div>
        </div>
        <div class="md:col-span-2 bg-beige-50 rounded-xl p-4 border-2 border-beige-300">
          <div class="text-xs font-semibold text-brown-700 uppercase mb-2">Адрес доставки</div>
          <div class="text-base font-semibold text-brown-800">{{ details?.address || '—' }}</div>
        </div>
        <div v-if="details?.comment" class="md:col-span-2 bg-white rounded-xl p-4 border-2 border-beige-200">
          <div class="text-xs font-semibold text-brown-700 uppercase mb-2">Комментарий</div>
          <div class="text-sm text-brown-700 whitespace-pre-wrap">{{ details.comment }}</div>
        </div>
      </div>
      <div class="mb-4">
        <h4 class="text-lg font-bold text-brown-800 mb-3 flex items-center gap-2">
          <svg class="w-5 h-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          Товары в заказе
        </h4>
        <div class="bg-white rounded-xl border-2 border-beige-200 overflow-hidden">
          <table class="w-full text-left">
            <thead>
              <tr class="border-b-2 border-beige-200 bg-gradient-to-r from-brown-50 to-beige-50">
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider w-32">Изображение</th>
                <th class="px-6 py-4 text-left text-xs font-bold text-brown-800 uppercase tracking-wider">Название</th>
                <th class="px-6 py-4 text-center text-xs font-bold text-brown-800 uppercase tracking-wider w-32">Количество</th>
                <th class="px-6 py-4 text-right text-xs font-bold text-brown-800 uppercase tracking-wider w-32">Цена</th>
              </tr>
            </thead>
            <tbody class="bg-white divide-y divide-beige-100">
              <tr v-for="it in (details?.items || [])" :key="it.id" class="hover:bg-cookie-50/30 transition-colors">
                <td class="px-6 py-4">
                  <div class="w-20 h-20 rounded-lg overflow-hidden bg-gradient-to-br from-beige-50 to-cookie-50 flex items-center justify-center">
                    <img :src="it.product?.imageUrl" class="w-full h-full object-contain p-2" alt="Product image" />
                  </div>
                </td>
                <td class="px-6 py-4">
                  <div class="text-sm font-semibold text-brown-800">{{ it.product?.title || it.productId }}</div>
                </td>
                <td class="px-6 py-4 text-center">
                  <span class="inline-flex items-center justify-center px-4 py-2 bg-cookie-100 text-cookie-700 rounded-lg font-bold text-sm min-w-[60px]">
                    ×{{ it.quantity }}
                  </span>
                </td>
                <td class="px-6 py-4 text-right">
                  <div class="text-sm font-bold text-brown-800">{{ it.price }} бун</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
  </div>
</template>



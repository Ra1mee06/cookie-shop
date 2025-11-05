<script setup>
import { ref, onMounted, computed } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
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
  try {
    const { data } = await admin.orders.list()
    orders.value = data
  } catch (e) {
    error.value = e.response?.data || 'Ошибка загрузки'
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

onMounted(load)

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
  <div class="max-w-5xl mx-auto mt-10">
    <h1 class="text-2xl font-bold mb-6">Админ · Заказы</h1>
    <AdminNav />
    <div class="flex flex-wrap gap-3 mb-4">
      <input v-model="search" placeholder="Поиск по #, userId, получателю, адресу" class="border rounded px-3 py-2 flex-1 min-w-[240px]" />
      <select v-model="statusFilter" class="border rounded px-3 py-2">
        <option value="">Все статусы</option>
        <option value="PENDING">PENDING</option>
        <option value="CONFIRMED">CONFIRMED</option>
        <option value="DELIVERED">DELIVERED</option>
        <option value="CANCELLED">CANCELLED</option>
      </select>
      <input type="date" v-model="dateFrom" class="border rounded px-3 py-2" />
      <input type="date" v-model="dateTo" class="border rounded px-3 py-2" />
      <select v-model.number="pageSize" class="border rounded px-3 py-2">
        <option :value="10">10</option>
        <option :value="20">20</option>
        <option :value="50">50</option>
      </select>
      <button class="px-3 py-2 rounded bg-slate-200" @click="exportCsv">Экспорт CSV</button>
    </div>
    <div class="flex flex-wrap gap-4 items-center mb-4 text-sm text-slate-700">
      <div><b>Всего:</b> {{ metrics.count }}</div>
      <div><b>Сумма:</b> {{ metrics.sum.toFixed(2) }}</div>
      <div class="flex gap-3">
        <span v-for="(v,k) in metrics.byStatus" :key="k"><b>{{ k }}:</b> {{ v }}</span>
      </div>
    </div>
    <div v-if="loading" class="text-gray-500">Загрузка…</div>
    <div v-if="error" class="text-red-600 mb-4">{{ error }}</div>
    <div v-if="!loading">
      <div class="overflow-x-auto bg-white rounded-xl shadow">
        <table class="min-w-full text-left">
          <thead>
            <tr class="border-b">
              <th class="p-3">#</th>
              <th class="p-3">Пользователь</th>
              <th class="p-3">Сумма</th>
              <th class="p-3">Статус</th>
              <th class="p-3">Действия</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in paginated" :key="o.id" class="border-b hover:bg-slate-50">
              <td class="p-3"><button class="underline" @click="openDetails(o)">#{{ o.id }}</button></td>
              <td class="p-3">{{ o.userId ?? '—' }}</td>
              <td class="p-3">{{ o.totalPrice }}</td>
              <td class="p-3">{{ o.status }}</td>
              <td class="p-3">
                <div class="flex flex-wrap gap-2 mb-2">
                  <button class="px-3 py-1 rounded bg-slate-200 hover:bg-slate-300" @click="changeStatus(o.id, 'CONFIRMED')">Подтвердить</button>
                  <button class="px-3 py-1 rounded bg-slate-200 hover:bg-slate-300" @click="changeStatus(o.id, 'DELIVERED')">Доставлен</button>
                  <button class="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600" @click="changeStatus(o.id, 'CANCELLED')">Отменить</button>
                  <button v-if="editing !== o.id" class="px-3 py-1 rounded bg-black text-white" @click="startEdit(o)">Редактировать</button>
                </div>
                <div v-if="editing === o.id" class="p-3 border rounded grid grid-cols-1 md:grid-cols-2 gap-3 bg-slate-50">
                  <div>
                    <label class="block text-xs text-slate-500 mb-1">Получатель</label>
                    <input v-model="form.recipient" class="border rounded px-2 py-1 w-full" />
                  </div>
                  <div>
                    <label class="block text-xs text-slate-500 mb-1">Адрес</label>
                    <input v-model="form.address" class="border rounded px-2 py-1 w-full" />
                  </div>
                  <div class="md:col-span-2">
                    <label class="block text-xs text-slate-500 mb-1">Комментарий</label>
                    <input v-model="form.comment" class="border rounded px-2 py-1 w-full" />
                  </div>
                  <div>
                    <label class="block text-xs text-slate-500 mb-1">Оплата</label>
                    <select v-model="form.paymentMethod" class="border rounded px-2 py-1 w-full">
                      <option value="CASH">Наличные</option>
                      <option value="CARD_ONLINE">Карта онлайн</option>
                      <option value="CARD_ON_DELIVERY">Карта при получении</option>
                    </select>
                  </div>
                  <div class="flex gap-2 md:col-span-2">
                    <button class="px-3 py-1 rounded bg-black text-white" @click="saveEdit(o)">Сохранить</button>
                    <button class="px-3 py-1 rounded bg-slate-200" @click="cancelEdit">Отмена</button>
                  </div>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="flex items-center justify-between mt-4">
        <div class="text-sm text-slate-600">Стр. {{ page }} из {{ totalPages }}</div>
        <div class="flex gap-2">
          <button class="px-3 py-1 rounded bg-slate-200" :disabled="page <= 1" @click="page = Math.max(1, page - 1)">Назад</button>
          <button class="px-3 py-1 rounded bg-slate-200" :disabled="page >= totalPages" @click="page = Math.min(totalPages, page + 1)">Вперёд</button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Order details modal -->
  <div v-if="showDetails" class="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
    <div class="bg-white rounded-xl shadow max-w-3xl w-full p-5">
      <div class="flex justify-between items-center mb-3">
        <h3 class="text-xl font-semibold">Заказ #{{ details?.id }}</h3>
        <button class="px-2 py-1" @click="closeDetails">✕</button>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4 text-sm">
        <div><b>Пользователь:</b> {{ details?.userId ?? '—' }}</div>
        <div><b>Сумма:</b> {{ details?.totalPrice }}</div>
        <div><b>Статус:</b> {{ details?.status }}</div>
        <div><b>Промокод:</b> {{ details?.promoCode || '—' }}</div>
        <div><b>Оплата:</b> {{ details?.paymentMethod }}</div>
        <div><b>Чаевые:</b> {{ details?.tip }}</div>
        <div class="md:col-span-2"><b>Скидка:</b> {{ details?.discount }}</div>
        <div class="md:col-span-2"><b>Получатель:</b> {{ details?.recipient || '—' }}</div>
        <div class="md:col-span-2"><b>Адрес:</b> {{ details?.address || '—' }}</div>
        <div class="md:col-span-2"><b>Комментарий:</b> {{ details?.comment || '—' }}</div>
      </div>
      <div class="overflow-x-auto">
        <table class="min-w-full text-left text-sm">
          <thead>
            <tr class="border-b">
              <th class="p-2">Товар</th>
              <th class="p-2">Название</th>
              <th class="p-2">Кол-во</th>
              <th class="p-2">Сумма</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="it in (details?.items || [])" :key="it.id" class="border-b">
              <td class="p-2"><img :src="it.product?.imageUrl" class="w-12 h-12 object-cover rounded" /></td>
              <td class="p-2">{{ it.product?.title || it.productId }}</td>
              <td class="p-2">{{ it.quantity }}</td>
              <td class="p-2">{{ it.price }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  
</template>



<script setup>
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const list = ref([])
const loading = ref(false)
const error = ref('')

const form = ref({ code: '', type: 'ORDER_PERCENT', value: 10, maxUses: null, expiresAt: '', productId: null })
const errors = ref({})
const hints = {
  ORDER_PERCENT: 'Скидка на весь заказ в процентах (0-100).',
  PRODUCT_PERCENT: 'Скидка в процентах на указанный товар (нужен ID товара).',
  BUY2GET1: 'Акция 2 по цене 1: применяется к позициям при оформлении.',
  GIFT_CERTIFICATE: 'Подарочный сертификат на фиксированную сумму.'
}

const load = async () => {
  loading.value = true
  error.value = ''
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  console.log('Загрузка промокодов. userId:', userId)
  
  if (!userId) {
    error.value = 'Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.'
    loading.value = false
    return
  }
  
  try {
    console.log('Отправка запроса на /admin/promos')
    const response = await admin.promocodes.list()
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Проверяем разные форматы ответа
    let data = response.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      if (data.data && Array.isArray(data.data)) {
        data = data.data
      } else if (data.promocodes && Array.isArray(data.promocodes)) {
        data = data.promocodes
      } else if (data.content && Array.isArray(data.content)) {
        data = data.content
      }
    }
    
    list.value = Array.isArray(data) ? data : []
    console.log('Загружено промокодов:', list.value.length)
    
    if (!Array.isArray(list.value)) {
      console.error('Ожидался массив, получено:', list.value)
      list.value = []
    }
  } catch (e) {
    console.error('Ошибка загрузки промокодов:', e)
    console.error('Error response:', e.response)
    error.value = e.response?.data?.message || e.response?.data || e.message || 'Ошибка загрузки'
    list.value = []
  } finally {
    loading.value = false
  }
}

const createCode = async () => {
  errors.value = {}
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  if (!userId) {
    errors.value.general = 'Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.'
    return
  }
  
  // Validation
  if (!form.value.code.trim()) {
    errors.value.code = 'Код обязателен'
  }
  if (!form.value.type) {
    errors.value.type = 'Тип обязателен'
  }
  if (form.value.type !== 'BUY2GET1' && (form.value.value == null || form.value.value === '')) {
    errors.value.value = 'Значение обязательно'
  }
  if (form.value.type === 'PRODUCT_PERCENT' && !form.value.productId) {
    errors.value.productId = 'Требуется ID товара'
  }
  
  if (Object.keys(errors.value).length) return

  try {
    console.log('Создание промокода. Форма:', form.value)
    
    const payload = {
      code: form.value.code.trim(),
      type: form.value.type,
      active: true
    }
    
    // Добавляем value только если тип не BUY2GET1
    if (form.value.type !== 'BUY2GET1' && form.value.value != null && form.value.value !== '') {
      payload.value = form.value.value
    }
    
    // Добавляем maxUses если указано
    if (form.value.maxUses != null && form.value.maxUses > 0) {
      payload.maxUses = form.value.maxUses
    }
    
    // Добавляем expiresAt если указано
    if (form.value.expiresAt) {
      const date = new Date(form.value.expiresAt)
      // Устанавливаем время на конец дня
      date.setHours(23, 59, 59, 999)
      payload.expiresAt = date.toISOString()
    }
    
    // Добавляем productId если тип PRODUCT_PERCENT
    if (form.value.type === 'PRODUCT_PERCENT' && form.value.productId) {
      payload.productId = Number(form.value.productId)
    }
    
    console.log('Отправка payload:', payload)
    
    const response = await admin.promocodes.create(payload)
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Очищаем форму после успешного создания
    form.value = { code: '', type: 'ORDER_PERCENT', value: 10, maxUses: null, expiresAt: '', productId: null }
    errors.value = {}
    
    // Перезагружаем список
    await load()
    
    alert('Промокод успешно создан!')
  } catch (e) {
    console.error('Ошибка создания промокода:', e)
    console.error('Error response:', e.response)
    const errorMsg = e.response?.data?.message || e.response?.data || e.message || 'Не удалось создать промокод'
    alert(errorMsg)
  }
}

const deactivate = async (p) => {
  if (!confirm('Деактивировать промокод?')) return
  await admin.promocodes.deactivate(p.id)
  await load()
}

onMounted(load)
</script>

<template>
  <div class="max-w-7xl mx-auto mt-10">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Панель управления · Промокоды
      </h1>
      <p class="text-brown-600">Создание и управление промокодами</p>
    </div>
    <AdminNav />
    <div v-if="loading" class="text-center py-16">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-cookie-600 border-t-transparent"></div>
      <p class="text-brown-600 mt-4 font-medium">Загрузка промокодов...</p>
    </div>
    <div v-if="error" class="mb-6 p-4 bg-red-50 border-2 border-red-200 text-red-700 rounded-xl">
      <div class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ error }}
      </div>
    </div>
    <div v-if="errors.general" class="mb-6 p-4 bg-red-50 border-2 border-red-200 text-red-700 rounded-xl">
      {{ errors.general }}
    </div>

    <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6 mb-8">
      <h2 class="text-xl font-bold mb-4 text-brown-800">Создать промокод</h2>
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Код</label>
          <input v-model="form.code" class="input-field" placeholder="SAVE10" />
          <div v-if="errors.code" class="text-xs text-red-600 mt-1">{{ errors.code }}</div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Тип</label>
          <select v-model="form.type" class="input-field">
            <option>ORDER_PERCENT</option>
            <option>PRODUCT_PERCENT</option>
            <option>BUY2GET1</option>
            <option>GIFT_CERTIFICATE</option>
          </select>
          <div class="text-xs text-brown-600 mt-1 bg-beige-50 p-2 rounded">{{ hints[form.type] }}</div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Значение</label>
          <input type="number" v-model.number="form.value" class="input-field" :disabled="form.type==='BUY2GET1'" />
          <div v-if="errors.value" class="text-xs text-red-600 mt-1">{{ errors.value }}</div>
        </div>
        <div v-if="form.type === 'PRODUCT_PERCENT'">
          <label class="block text-sm font-semibold text-brown-700 mb-2">ID товара</label>
          <input type="number" v-model.number="form.productId" class="input-field" />
          <div v-if="errors.productId" class="text-xs text-red-600 mt-1">{{ errors.productId }}</div>
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Max uses</label>
          <input type="number" v-model.number="form.maxUses" class="input-field" />
        </div>
        <div>
          <label class="block text-sm font-semibold text-brown-700 mb-2">Действует до</label>
          <input type="date" v-model="form.expiresAt" class="input-field" />
        </div>
        <div class="md:col-span-2 lg:col-span-3">
          <button class="btn-primary" @click="createCode">Создать промокод</button>
        </div>
      </div>
    </div>

    <div v-if="!loading && list.length === 0 && !error" class="text-center py-16 bg-white rounded-2xl border-2 border-beige-200">
      <div class="w-24 h-24 mx-auto mb-4 rounded-full bg-gradient-to-br from-beige-100 to-cookie-100 flex items-center justify-center">
        <svg class="w-12 h-12 text-brown-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <p class="text-brown-600 font-medium text-lg">Промокоды не найдены</p>
    </div>
    <div v-if="!loading && list.length > 0" class="overflow-x-auto bg-white rounded-2xl shadow-lg border-2 border-beige-200">
      <table class="min-w-full text-left">
        <thead>
          <tr class="border-b-2 border-beige-200 bg-gradient-to-r from-brown-50 to-beige-50">
            <th class="p-4 font-bold text-brown-800">Код</th>
            <th class="p-4 font-bold text-brown-800">Тип</th>
            <th class="p-4 font-bold text-brown-800">Значение</th>
            <th class="p-4 font-bold text-brown-800">Активен</th>
            <th class="p-4 font-bold text-brown-800">Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.id" class="border-b border-beige-100 hover:bg-cookie-50/30 transition-colors">
            <td class="p-4 font-mono font-semibold text-brown-800">{{ p.code }}</td>
            <td class="p-4 text-brown-700">{{ p.type }}</td>
            <td class="p-4 font-semibold text-brown-800">{{ p.value }}</td>
            <td class="p-4">
              <span 
                class="px-3 py-1 rounded-full text-xs font-semibold"
                :class="p.active ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'"
              >
                {{ p.active ? 'Да' : 'Нет' }}
              </span>
            </td>
            <td class="p-4">
              <button 
                class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 font-semibold text-sm" 
                @click="deactivate(p)"
              >
                Деактивировать
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>



<script setup>
import { ref, onMounted } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const list = ref([])
const loading = ref(false)
const error = ref('')

const form = ref({ code: '', type: 'ORDER_PERCENT', value: 10, maxUses: null, expiresAt: '', productId: null })

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const { data } = await admin.promocodes.list()
    list.value = data
  } catch (e) {
    error.value = e.response?.data || 'Ошибка загрузки'
  } finally {
    loading.value = false
  }
}

const createCode = async () => {
  try {
    const payload = { code: form.value.code, type: form.value.type, value: form.value.value, maxUses: form.value.maxUses, active: true }
    if (form.value.expiresAt) payload.expiresAt = new Date(form.value.expiresAt).toISOString()
    if (form.value.type === 'PRODUCT_PERCENT' && form.value.productId) {
      payload.product = { id: Number(form.value.productId) }
    }
    await admin.promocodes.create(payload)
    form.value = { code: '', type: 'ORDER_PERCENT', value: 10, maxUses: null, expiresAt: '', productId: null }
    await load()
  } catch (e) {
    alert(e.response?.data || 'Не удалось создать промокод')
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
  <div class="max-w-5xl mx-auto mt-10">
    <h1 class="text-2xl font-bold mb-6">Админ · Промокоды</h1>
    <AdminNav />
    <div v-if="loading" class="text-gray-500">Загрузка…</div>
    <div v-if="error" class="text-red-600 mb-4">{{ error }}</div>

    <div class="bg-white rounded-xl shadow p-4 mb-8">
      <h2 class="font-semibold mb-3">Создать промокод</h2>
      <div class="flex flex-wrap gap-3 items-end">
        <div>
          <label class="block text-sm">Код</label>
          <input v-model="form.code" class="border rounded px-2 py-1" placeholder="SAVE10" />
        </div>
        <div>
          <label class="block text-sm">Тип</label>
          <select v-model="form.type" class="border rounded px-2 py-1">
            <option>ORDER_PERCENT</option>
            <option>PRODUCT_PERCENT</option>
            <option>BUY2GET1</option>
            <option>GIFT_CERTIFICATE</option>
          </select>
        </div>
        <div>
          <label class="block text-sm">Значение</label>
          <input type="number" v-model.number="form.value" class="border rounded px-2 py-1 w-28" />
        </div>
        <div v-if="form.type === 'PRODUCT_PERCENT'">
          <label class="block text-sm">ID товара</label>
          <input type="number" v-model.number="form.productId" class="border rounded px-2 py-1 w-28" />
        </div>
        <div>
          <label class="block text-sm">Max uses</label>
          <input type="number" v-model.number="form.maxUses" class="border rounded px-2 py-1 w-28" />
        </div>
        <div>
          <label class="block text-sm">Действует до</label>
          <input type="date" v-model="form.expiresAt" class="border rounded px-2 py-1" />
        </div>
        <button class="px-3 py-2 rounded bg-black text-white" @click="createCode">Создать</button>
      </div>
    </div>

    <div class="overflow-x-auto bg-white rounded-xl shadow">
      <table class="min-w-full text-left">
        <thead>
          <tr class="border-b">
            <th class="p-3">Код</th>
            <th class="p-3">Тип</th>
            <th class="p-3">Значение</th>
            <th class="p-3">Активен</th>
            <th class="p-3">Действия</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.id" class="border-b hover:bg-slate-50">
            <td class="p-3">{{ p.code }}</td>
            <td class="p-3">{{ p.type }}</td>
            <td class="p-3">{{ p.value }}</td>
            <td class="p-3">{{ p.active ? 'да' : 'нет' }}</td>
            <td class="p-3">
              <button class="px-3 py-1 rounded bg-red-500 text-white hover:bg-red-600" @click="deactivate(p)">Деактивировать</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>



<script setup>
import { ref } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const code = ref('')
const hours = ref(24)
const creating = ref(false)

const createInvite = async () => {
  creating.value = true
  try {
    const { data } = await admin.invites.create(hours.value)
    code.value = data.code
  } catch (e) {
    alert(e.response?.data || 'Не удалось создать код')
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <div class="max-w-xl mx-auto mt-10 bg-white rounded-xl shadow p-6">
    <h1 class="text-2xl font-bold mb-4">Админ · Инвайт‑коды</h1>
    <AdminNav />
    <div class="flex items-end gap-3 mb-4">
      <div>
        <label class="block text-sm">Срок (часы)</label>
        <input type="number" v-model.number="hours" min="1" class="border rounded px-2 py-1 w-28" />
      </div>
      <button :disabled="creating" class="px-4 py-2 rounded bg-black text-white disabled:opacity-50" @click="createInvite">Создать код</button>
    </div>
    <div v-if="code" class="p-3 bg-slate-100 rounded">Секретный код: <b>{{ code }}</b></div>
    <p class="text-slate-500 mt-3 text-sm">Передайте этот код новому администратору. Код можно использовать один раз при регистрации.</p>
  </div>
</template>



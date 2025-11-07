<script setup>
import { ref, onMounted, computed } from 'vue'
import { useApi } from '@/composables/useApi'
import AdminNav from '@/components/AdminNav.vue'

const { admin } = useApi()
const suggestions = ref([])
const loading = ref(false)
const error = ref('')

// Filters & pagination
const search = ref('')
const page = ref(1)
const pageSize = ref(10)

const filtered = computed(() => {
  let arr = suggestions.value
  if (search.value.trim()) {
    const q = search.value.trim().toLowerCase()
    arr = arr.filter(s =>
      String(s.id).includes(q) ||
      String(s.userId || '').includes(q) ||
      String(s.productName || '').toLowerCase().includes(q) ||
      String(s.author || '').toLowerCase().includes(q) ||
      String(s.description || '').toLowerCase().includes(q)
    )
  }
  return arr
})

const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize.value)))
const paginated = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filtered.value.slice(start, start + pageSize.value)
})

const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const load = async () => {
  loading.value = true
  error.value = ''
  
  // Проверяем наличие userId
  const userId = localStorage.getItem('userId')
  console.log('Загрузка предложений. userId:', userId)
  
  if (!userId) {
    error.value = 'Ошибка: userId не найден в localStorage. Пожалуйста, войдите в систему заново.'
    loading.value = false
    return
  }
  
  try {
    console.log('Отправка запроса на /admin/suggestions')
    const response = await admin.suggestions.list()
    console.log('Ответ от сервера:', response)
    console.log('response.data:', response.data)
    
    // Проверяем разные форматы ответа
    let data = response.data
    if (data && typeof data === 'object' && !Array.isArray(data)) {
      if (data.data && Array.isArray(data.data)) {
        data = data.data
      } else if (data.suggestions && Array.isArray(data.suggestions)) {
        data = data.suggestions
      } else if (data.content && Array.isArray(data.content)) {
        data = data.content
      }
    }
    
    suggestions.value = Array.isArray(data) ? data : []
    console.log('Загружено предложений:', suggestions.value.length)
    
    if (!Array.isArray(suggestions.value)) {
      console.error('Ожидался массив, получено:', suggestions.value)
      suggestions.value = []
    }
  } catch (e) {
    console.error('Ошибка загрузки предложений:', e)
    console.error('Error response:', e.response)
    error.value = e.response?.data?.message || e.response?.data || e.message || 'Ошибка загрузки'
    suggestions.value = []
  } finally {
    loading.value = false
  }
}

const removeSuggestion = async (id) => {
  if (!confirm('Удалить предложение?')) return
  try {
    await admin.suggestions.remove(id)
    await load()
  } catch (e) {
    alert(e.response?.data?.message || 'Ошибка удаления')
  }
}

onMounted(load)
</script>

<template>
  <div class="max-w-7xl mx-auto mt-10">
    <div class="mb-8">
      <h1 class="text-4xl font-extrabold mb-2 bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
        Панель управления · История предложений
      </h1>
      <p class="text-brown-600">Просмотр и управление предложениями пользователей</p>
    </div>
    <AdminNav />
    <div class="bg-white rounded-2xl shadow-lg border-2 border-beige-200 p-6">
    
    <div v-if="error" class="mb-6 p-4 bg-red-50 border-2 border-red-200 text-red-700 rounded-xl">
      <div class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        {{ error }}
      </div>
    </div>

    <!-- Filters -->
    <div class="mb-6">
      <div class="flex gap-4 items-end">
        <div class="flex-1">
          <label class="block text-sm font-semibold text-brown-700 mb-2">Поиск</label>
          <input
            type="text"
            v-model="search"
            placeholder="По ID, названию продукта, автору..."
            class="input-field w-full"
          />
        </div>
      </div>
    </div>

    <!-- Statistics -->
    <div class="mb-6 p-4 bg-gradient-to-br from-cookie-50 to-beige-50 rounded-xl border-2 border-cookie-200">
      <div class="text-sm font-semibold text-brown-700">
        Всего предложений: <span class="text-2xl font-extrabold text-brown-800">{{ filtered.length }}</span>
      </div>
    </div>

    <!-- Suggestions List -->
    <div v-if="loading" class="text-center py-16">
      <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-4 border-cookie-600 border-t-transparent"></div>
      <p class="text-brown-600 mt-4 font-medium">Загрузка предложений...</p>
    </div>

    <div v-else-if="paginated.length === 0" class="text-center py-16 bg-white rounded-2xl border-2 border-beige-200">
      <div class="w-24 h-24 mx-auto mb-4 rounded-full bg-gradient-to-br from-beige-100 to-cookie-100 flex items-center justify-center">
        <svg class="w-12 h-12 text-brown-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
      </div>
      <p class="text-brown-600 font-medium text-lg">Предложений не найдено</p>
    </div>

    <div v-else class="space-y-4">
      <div
        v-for="suggestion in paginated"
        :key="suggestion.id"
        class="bg-gradient-to-br from-white to-cookie-50 rounded-2xl p-6 border-2 border-beige-200 shadow-lg hover:shadow-xl hover:border-brown-300 transition-all duration-300"
      >
        <div class="flex justify-between items-start mb-4 pb-4 border-b-2 border-cookie-200">
          <div class="flex-1">
            <div class="flex items-center gap-3 mb-2">
              <svg class="h-6 w-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
              </svg>
              <h4 class="font-extrabold text-xl bg-gradient-to-r from-brown-700 to-brown-900 bg-clip-text text-transparent">
                #{{ suggestion.id }} · {{ suggestion.productName }}
              </h4>
            </div>
            <p class="text-sm text-brown-600 flex items-center gap-2">
              <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              {{ formatDate(suggestion.createdAt) }}
            </p>
          </div>
          <button
            @click="removeSuggestion(suggestion.id)"
            class="px-4 py-2 rounded-lg bg-red-500 text-white hover:bg-red-600 transition-all duration-300 transform hover:scale-105 font-semibold"
          >
            Удалить
          </button>
        </div>

        <div class="bg-white rounded-xl p-4 mb-3 border-2 border-beige-200">
          <p class="text-xs font-semibold text-brown-600 uppercase mb-2 flex items-center gap-2">
            <svg class="h-4 w-4 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
            Автор предложения
          </p>
          <p class="text-base font-bold text-brown-800">{{ suggestion.author }}</p>
          <div class="mt-3 pt-3 border-t border-beige-200">
            <p class="text-xs font-semibold text-brown-600 uppercase mb-2 flex items-center gap-2">
              <svg class="h-4 w-4 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
              </svg>
              Аккаунт отправителя
            </p>
            <div class="space-y-1">
              <p v-if="suggestion.userFullName" class="text-sm font-semibold text-brown-800">
                Имя: <span class="font-normal">{{ suggestion.userFullName }}</span>
              </p>
              <p v-if="suggestion.userEmail" class="text-sm font-semibold text-brown-800">
                Email: <span class="font-normal">{{ suggestion.userEmail }}</span>
              </p>
              <p v-if="suggestion.userUsername" class="text-sm font-semibold text-brown-800">
                Логин: <span class="font-normal">@{{ suggestion.userUsername }}</span>
              </p>
              <p v-if="suggestion.userId" class="text-sm font-semibold text-brown-800">
                ID: <span class="font-normal">{{ suggestion.userId }}</span>
              </p>
              <p v-if="!suggestion.userId && !suggestion.userUsername && !suggestion.userEmail" class="text-xs text-brown-500 italic">
                Информация о пользователе недоступна
              </p>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl p-4 border-2 border-beige-200">
          <p class="text-xs font-semibold text-brown-600 uppercase mb-2 flex items-center gap-2">
            <svg class="h-4 w-4 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
            </svg>
            Описание
          </p>
          <p class="text-sm text-brown-700 whitespace-pre-wrap leading-relaxed">{{ suggestion.description }}</p>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center items-center gap-3 p-4 bg-white rounded-xl border-2 border-beige-200">
      <button
        @click="page = Math.max(1, page - 1)"
        :disabled="page === 1"
        class="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
      >
        ← Назад
      </button>
      <span class="px-4 py-2 text-sm font-semibold text-brown-700">
        Страница {{ page }} из {{ totalPages }}
      </span>
      <button
        @click="page = Math.min(totalPages, page + 1)"
        :disabled="page === totalPages"
        class="btn-secondary disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Вперёд →
      </button>
    </div>
    </div>
  </div>
</template>


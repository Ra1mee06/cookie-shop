<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import NewsModal from './NewsModal.vue'

const router = useRouter()
const selectedNews = ref(null)
const showNewsModal = ref(false)

// Данные для ленты новостей и спецпредложений
const newsItems = ref([
  {
    id: 1,
    type: 'promo',
    title: 'Скидка 20% на все печеньки!',
    description: 'Только сегодня! Успейте заказать любимые печеньки со скидкой 20%. Акция действует до конца дня.',
    image: '/cookies/cookie1.png',
    badge: 'Акция',
    badgeColor: 'bg-red-500',
    link: '/',
    gradient: 'from-red-500 to-pink-500'
  },
  {
    id: 2,
    type: 'news',
    title: 'Новинка: Шоколадный рай',
    description: 'Представляем новую коллекцию печенья с бельгийским шоколадом. Ограниченная серия!',
    image: '/cookies/cookie3.png',
    badge: 'Новинка',
    badgeColor: 'bg-cookie-500',
    link: '/',
    gradient: 'from-cookie-500 to-brown-600'
  },
  {
    id: 3,
    type: 'promo',
    title: '2+1 = Бесплатно!',
    description: 'Купите 2 печеньки и получите третью в подарок! Акция действует на все позиции.',
    image: '/cookies/cookie5.png',
    badge: 'Спецпредложение',
    badgeColor: 'bg-green-500',
    link: '/',
    gradient: 'from-green-500 to-emerald-600'
  },
  {
    id: 4,
    type: 'news',
    title: 'Открытие нового филиала',
    description: 'Рады сообщить об открытии нового филиала в центре города! Приходите и попробуйте наши новинки.',
    image: '/cookies/cookie7.png',
    badge: 'Событие',
    badgeColor: 'bg-blue-500',
    link: '/',
    gradient: 'from-blue-500 to-cyan-600'
  }
])

const currentIndex = ref(0)
const isAutoPlaying = ref(true)

const nextSlide = () => {
  currentIndex.value = (currentIndex.value + 1) % newsItems.value.length
}

const prevSlide = () => {
  currentIndex.value = (currentIndex.value - 1 + newsItems.value.length) % newsItems.value.length
}

const goToSlide = (index) => {
  currentIndex.value = index
  isAutoPlaying.value = false
  setTimeout(() => {
    isAutoPlaying.value = true
  }, 5000)
}

const openNewsModal = (item) => {
  selectedNews.value = item
  showNewsModal.value = true
  isAutoPlaying.value = false
}

const closeNewsModal = () => {
  showNewsModal.value = false
  setTimeout(() => {
    isAutoPlaying.value = true
  }, 1000)
}

onMounted(() => {
  // Автоматическая прокрутка каждые 5 секунд
  setInterval(() => {
    if (isAutoPlaying.value) {
      nextSlide()
    }
  }, 5000)
})
</script>

<template>
  <div class="relative w-full mb-12 overflow-hidden rounded-2xl shadow-2xl">
    <!-- Основной слайдер -->
    <div class="relative h-96 md:h-[500px]">
      <div 
        v-for="(item, index) in newsItems" 
        :key="item.id"
        class="absolute inset-0 transition-all duration-700 ease-in-out transform"
        :class="{
          'opacity-100 translate-x-0 z-10': index === currentIndex,
          'opacity-0 translate-x-full z-0': index > currentIndex,
          'opacity-0 -translate-x-full z-0': index < currentIndex
        }"
      >
        <div 
          class="relative h-full w-full bg-gradient-to-br"
          :class="`${item.gradient}`"
        >
          <!-- Фоновое изображение -->
          <div class="absolute inset-0 opacity-20">
            <img 
              :src="item.image" 
              :alt="item.title"
              class="w-full h-full object-cover"
            />
          </div>
          
          <!-- Контент -->
          <div class="relative z-10 h-full flex flex-col justify-center items-start p-8 md:p-16 text-white">
            <div class="max-w-2xl">
              <span 
                class="inline-block px-4 py-2 mb-4 rounded-full text-sm font-bold text-white shadow-lg"
                :class="item.badgeColor"
              >
                {{ item.badge }}
              </span>
              
              <h2 class="text-3xl md:text-5xl font-extrabold mb-4 leading-tight drop-shadow-lg">
                {{ item.title }}
              </h2>
              
              <p class="text-lg md:text-xl mb-6 text-white/90 drop-shadow-md">
                {{ item.description }}
              </p>
              
              <button
                @click="openNewsModal(item)"
                class="px-8 py-4 bg-white text-cookie-600 rounded-xl font-bold text-lg shadow-2xl hover:shadow-3xl transform hover:-translate-y-1 hover:scale-105 transition-all duration-300"
              >
                Узнать больше →
              </button>
            </div>
          </div>
          
          <!-- Декоративные элементы -->
          <div class="absolute top-10 right-10 w-32 h-32 bg-white/10 rounded-full blur-3xl"></div>
          <div class="absolute bottom-10 left-10 w-40 h-40 bg-white/10 rounded-full blur-3xl"></div>
        </div>
      </div>
    </div>
    
    <!-- Навигационные стрелки -->
    <button
      @click="prevSlide"
      class="absolute left-4 top-1/2 -translate-y-1/2 z-20 p-3 bg-white/90 backdrop-blur-sm rounded-full shadow-lg hover:bg-white transform hover:scale-110 transition-all duration-300"
      aria-label="Предыдущий слайд"
    >
      <svg class="w-6 h-6 text-brown-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
      </svg>
    </button>
    
    <button
      @click="nextSlide"
      class="absolute right-4 top-1/2 -translate-y-1/2 z-20 p-3 bg-white/90 backdrop-blur-sm rounded-full shadow-lg hover:bg-white transform hover:scale-110 transition-all duration-300"
      aria-label="Следующий слайд"
    >
      <svg class="w-6 h-6 text-brown-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
      </svg>
    </button>
    
    <!-- Индикаторы -->
    <div class="absolute bottom-6 left-1/2 -translate-x-1/2 z-20 flex gap-2">
      <button
        v-for="(item, index) in newsItems"
        :key="item.id"
        @click="goToSlide(index)"
        class="w-3 h-3 rounded-full transition-all duration-300"
        :class="index === currentIndex 
          ? 'bg-white w-8 shadow-lg' 
          : 'bg-white/50 hover:bg-white/75'"
        :aria-label="`Перейти к слайду ${index + 1}`"
      ></button>
    </div>

    <!-- Модальное окно новости -->
    <NewsModal
      :isOpen="showNewsModal"
      :newsItem="selectedNews"
      @close="closeNewsModal"
    />
  </div>
</template>

<style scoped>
.shadow-3xl {
  box-shadow: 0 35px 60px -12px rgba(0, 0, 0, 0.5);
}
</style>


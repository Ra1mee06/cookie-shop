<template>
  <div class="fixed inset-0 pointer-events-none overflow-hidden z-0">
    <!-- Плавающие печеньки -->
    <div 
      v-for="(cookie, index) in cookies" 
      :key="index"
      class="absolute animate-float"
      :style="{
        left: cookie.x + '%',
        top: cookie.y + '%',
        animationDelay: cookie.delay + 's',
        animationDuration: cookie.duration + 's'
      }"
    >
      <div class="w-16 h-16 md:w-20 md:h-20 rounded-full bg-gradient-to-br from-cookie-200 to-cookie-400 opacity-20 blur-sm transform rotate-12">
        <div class="w-full h-full rounded-full bg-gradient-to-br from-brown-300/30 to-brown-500/30"></div>
      </div>
    </div>

    <!-- Облака -->
    <div 
      v-for="(cloud, index) in clouds" 
      :key="'cloud-' + index"
      class="absolute animate-float-slow"
      :style="{
        left: cloud.x + '%',
        top: cloud.y + '%',
        animationDelay: cloud.delay + 's',
        animationDuration: cloud.duration + 's',
        width: cloud.size + 'px',
        height: cloud.size + 'px'
      }"
    >
      <div class="w-full h-full rounded-full bg-white/10 blur-2xl"></div>
    </div>

    <!-- Плавающие сердечки -->
    <div 
      v-for="(heart, index) in hearts" 
      :key="'heart-' + index"
      class="absolute animate-float-heart"
      :style="{
        left: heart.x + '%',
        top: heart.y + '%',
        animationDelay: heart.delay + 's',
        animationDuration: heart.duration + 's'
      }"
    >
      <svg class="w-8 h-8 md:w-10 md:h-10 text-cookie-300/20" fill="currentColor" viewBox="0 0 24 24">
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
      </svg>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const cookies = ref([])
const clouds = ref([])
const hearts = ref([])

const generateElements = () => {
  // Генерируем плавающие печеньки
  cookies.value = Array.from({ length: 8 }, (_, i) => ({
    x: Math.random() * 100,
    y: Math.random() * 100,
    delay: Math.random() * 5,
    duration: 8 + Math.random() * 4
  }))

  // Генерируем облака
  clouds.value = Array.from({ length: 5 }, (_, i) => ({
    x: Math.random() * 100,
    y: Math.random() * 100,
    delay: Math.random() * 3,
    duration: 15 + Math.random() * 10,
    size: 100 + Math.random() * 150
  }))

  // Генерируем сердечки
  hearts.value = Array.from({ length: 6 }, (_, i) => ({
    x: Math.random() * 100,
    y: Math.random() * 100,
    delay: Math.random() * 4,
    duration: 6 + Math.random() * 3
  }))
}

onMounted(() => {
  generateElements()
})
</script>



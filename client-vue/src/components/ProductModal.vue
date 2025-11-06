<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: Object
})

const emit = defineEmits(['close'])

const cookieDetails = {
  "Бабушкины тайны": {
    description: "Традиционная печенька с секретным рецептом, передающимся в семье из поколения в поколение.",
    ingredients: "Мука, масло, мёд, специи",
    calories: "350 ккал/100г",
    story: "Рецепт создан в 1890 году в маленькой деревне на севере России"
  },
  "Ореховый кранч": {
    description: "Хрустящая печенька с кусочками грецких орехов и миндаля.",
    ingredients: "Мука, масло, сахар, грецкие орехи, миндаль",
    calories: "420 ккал/100г",
    story: "Любимое лакомство альпинистов и путешественников"
  },
  "Шоколадный рай": {
    description: "Для настоящих ценителей шоколада с какао-бобами высшего сорта.",
    ingredients: "Мука, масло, какао-бобы, шоколадные капли",
    calories: "380 ккал/100г",
    story: "Вдохновлено рецептами бельгийских шоколатье"
  }
}

const details = computed(() => {
  return cookieDetails[props.product?.title] || {
    description: "Ароматная домашняя печенька по традиционному рецепту",
    ingredients: "Натуральные ингредиенты",
    calories: "около 400 ккал/100г",
    story: "Сделано с любовью"
  }
})
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <button class="close-btn" @click="emit('close')">
        <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
        </svg>
      </button>
      
      <div class="grid md:grid-cols-2 gap-8">
        <!-- Левая часть - изображение -->
        <div class="relative flex items-center justify-center bg-gradient-to-br from-white via-cookie-50/40 to-beige-50/60 rounded-2xl p-8 overflow-hidden">
          <!-- Декоративные элементы -->
          <div class="absolute top-0 right-0 w-40 h-40 bg-cookie-200/20 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
          <div class="absolute bottom-0 left-0 w-48 h-48 bg-brown-200/20 rounded-full blur-3xl translate-y-1/2 -translate-x-1/2"></div>
          
          <img 
            :src="product.imageUrl" 
            :alt="product.title"
            class="w-full max-h-96 object-contain rounded-lg relative z-10 drop-shadow-2xl"
          >
        </div>
        
        <!-- Правая часть - информация -->
        <div class="space-y-6">
          <!-- Заголовок и цена -->
          <div class="pb-4 border-b-2 border-beige-200">
            <h2 class="text-4xl md:text-5xl font-black bg-gradient-to-r from-cookie-600 via-cookie-500 to-brown-700 bg-clip-text text-transparent mb-4 leading-tight">
              {{ product.title }}
            </h2>
            <div class="flex items-baseline gap-2">
              <span class="text-brown-400 text-lg font-semibold">Цена:</span>
              <p class="text-4xl font-black bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
                {{ product.price }}
              </p>
              <span class="text-brown-500 text-xl font-bold">бун</span>
            </div>
          </div>
          
          <!-- Информационные блоки -->
          <div class="space-y-5 pt-2">
            <!-- Описание -->
            <div class="bg-gradient-to-br from-cookie-50 to-cookie-100/50 rounded-2xl p-6 border-2 border-cookie-200/50 shadow-lg">
              <h3 class="text-xl font-bold text-brown-900 mb-3 flex items-center gap-3">
                <div class="p-2 bg-cookie-500 rounded-lg">
                  <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <span>Описание</span>
              </h3>
              <p class="text-brown-800 leading-relaxed text-base font-medium">{{ details.description }}</p>
            </div>
            
            <!-- История -->
            <div class="bg-gradient-to-br from-beige-50 to-beige-100/50 rounded-2xl p-6 border-2 border-beige-200/50 shadow-lg">
              <h3 class="text-xl font-bold text-brown-900 mb-3 flex items-center gap-3">
                <div class="p-2 bg-brown-500 rounded-lg">
                  <svg class="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v13m0-13V6a2 2 0 112 2h-2zm0 0V5.5A2.5 2.5 0 109.5 8H12zm-7 4h14M5 12a2 2 0 110-4h14a2 2 0 110 4M5 12v7a2 2 0 002 2h10a2 2 0 002-2v-7" />
                  </svg>
                </div>
                <span>История</span>
              </h3>
              <p class="text-brown-800 leading-relaxed text-base font-medium">{{ details.story }}</p>
            </div>
            
            <!-- Состав и пищевая ценность -->
            <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div class="bg-white border-2 border-cookie-200 rounded-2xl p-5 shadow-md hover:shadow-lg transition-shadow duration-300">
                <h3 class="text-lg font-bold text-brown-900 mb-3 flex items-center gap-2">
                  <div class="p-1.5 bg-cookie-500 rounded-md">
                    <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <span>Состав</span>
                </h3>
                <p class="text-brown-700 font-medium">{{ details.ingredients }}</p>
              </div>
              
              <div class="bg-white border-2 border-cookie-200 rounded-2xl p-5 shadow-md hover:shadow-lg transition-shadow duration-300">
                <h3 class="text-lg font-bold text-brown-900 mb-3 flex items-center gap-2">
                  <div class="p-1.5 bg-cookie-500 rounded-md">
                    <svg class="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <span>Пищевая ценность</span>
                </h3>
                <p class="text-brown-700 font-bold text-lg">{{ details.calories }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  animation: fadeIn 0.3s ease-out;
}

.modal-content {
  background: white;
  padding: 2.5rem;
  border-radius: 2rem;
  border: 2px solid rgba(230, 213, 184, 0.3);
  max-width: 1000px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.4s ease-out;
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.close-btn {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  width: 2.5rem;
  height: 2.5rem;
  background: white;
  border: 2px solid #E6D5B8;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  z-index: 10;
  color: #8B4513;
}

.close-btn:hover {
  transform: scale(1.1) rotate(90deg);
  background: #FFE8ED;
  border-color: #FF6B9D;
  color: #FF6B9D;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}
</style>
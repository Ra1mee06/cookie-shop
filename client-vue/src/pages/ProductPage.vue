<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const product = ref(null)

const cookieDescriptions = {
  "Бабушкины тайны": {
    description: "Традиционная печенька с секретным рецептом, передающимся в семье из поколения в поколение. Содержит натуральные специи и мёд.",
    ingredients: "Мука, масло, мёд, специи",
    calories: "350 ккал/100г"
  },
  "Ореховый кранч": {
    description: "Хрустящая печенька с кусочками грецких орехов и миндаля. Идеальный баланс сладости и орехового послевкусия.",
    ingredients: "Мука, масло, сахар, грецкие орехи, миндаль",
    calories: "420 ккал/100г"
  },
  "Шоколадный рай": {
    description: "Для настоящих ценителей шоколада. Содержит какао-бобы высшего сорта и шоколадные капли.",
    ingredients: "Мука, масло, какао-бобы, шоколадные капли",
    calories: "380 ккал/100г"
  }
}

onMounted(async () => {
  setTimeout(() => {
    product.value = {
      id: route.params.id,
      title: route.query.title || "Бабушкины тайны", 
      imageUrl: "/cookie-" + route.params.id + ".jpg",
      price: 150,
      ...cookieDescriptions[route.query.title || "Бабушкины тайны"]
    }
  }, 300)
})
</script>

<template>
  <div v-if="product" class="product-page max-w-6xl mx-auto p-6">
    <div class="grid md:grid-cols-2 gap-8">
      <div class="bg-white rounded-xl shadow-md p-6">
        <img 
          :src="product.imageUrl" 
          :alt="product.title"
          class="w-full h-96 object-contain"
        >
      </div>
      
      <div class="space-y-6">
        <h1 class="text-4xl font-bold text-gray-800">{{ product.title }}</h1>
        
        <div class="flex items-center gap-4">
          <span class="text-3xl font-semibold text-lime-600">{{ product.price }} бун</span>
          <span class="text-sm text-gray-500">{{ product.calories }}</span>
        </div>
        
        <div class="prose max-w-none">
          <h3 class="text-xl font-semibold">Описание</h3>
          <p class="text-gray-700">{{ product.description }}</p>
          
          <h3 class="text-xl font-semibold mt-4">Состав</h3>
          <p class="text-gray-700">{{ product.ingredients }}</p>
        </div>
        
        <div class="pt-4">
          <button 
            @click="$router.back()"
            class="bg-lime-500 hover:bg-lime-600 text-white px-6 py-3 rounded-lg transition"
          >
            ← Назад к каталогу
          </button>
        </div>
      </div>
    </div>
  </div>
  
  <div v-else class="text-center py-20">
    <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-lime-500 mx-auto"></div>
    <p class="mt-4 text-gray-600">Загружаем информацию о печеньке...</p>
  </div>
</template>

<style scoped>
.product-page {
  min-height: calc(100vh - 200px);
}
</style>
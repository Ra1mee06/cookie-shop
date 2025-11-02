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
      <button class="close-btn" @click="emit('close')">×</button>
      
      <div class="grid md:grid-cols-2 gap-8">
        <div class="flex items-center">
          <img 
            :src="product.imageUrl" 
            :alt="product.title"
            class="w-full max-h-96 object-contain rounded-lg"
          >
        </div>
        
        <div class="space-y-4">
          <h2 class="text-3xl font-bold text-gray-800">{{ product.title }}</h2>
          <p class="text-2xl text-lime-600">{{ product.price }} бун</p>
          
          <div class="space-y-6">
            <div>
              <h3 class="text-lg font-semibold">Описание</h3>
              <p class="text-gray-700">{{ details.description }}</p>
            </div>
            
            <div>
              <h3 class="text-lg font-semibold">История</h3>
              <p class="text-gray-700">{{ details.story }}</p>
            </div>
            
            <div class="grid grid-cols-2 gap-4">
              <div>
                <h3 class="text-lg font-semibold">Состав</h3>
                <p class="text-gray-700">{{ details.ingredients }}</p>
              </div>
              
              <div>
                <h3 class="text-lg font-semibold">Пищевая ценность</h3>
                <p class="text-gray-700">{{ details.calories }}</p>
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
  padding: 2rem;
  border-radius: 1rem;
  max-width: 800px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: slideUp 0.4s ease-out;
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  font-size: 1.5rem;
  background: none;
  border: none;
  cursor: pointer;
  transition: transform 0.2s;
}

.close-btn:hover {
  transform: scale(1.2);
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
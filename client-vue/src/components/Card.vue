<script setup>
import { ref } from 'vue'

const props = defineProps({
  id: Number,
  title: String,
  imageUrl: String,
  price: Number,
  isFavorite: Boolean,
  isAdded: Boolean,
  onClickFavorite: Function,
  onClickAdd: Function,
  onClickRemove: Function,
  quantity: Number
})

const emit = defineEmits(['productClick'])

const isHovered = ref(false)

const handleProductClick = () => {
  emit('productClick', {
    id: props.id,
    title: props.title,
    imageUrl: props.imageUrl,
    price: props.price
  })
}
</script>

<template>
  <div
    class="card relative group overflow-hidden transform transition-all duration-500 hover:-translate-y-1 flex flex-col h-full"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- Badge для избранного/удаления -->
    <div v-if="onClickRemove" class="absolute top-4 right-4 z-30">
      <button
        @click.stop="onClickRemove"
        class="p-2.5 bg-white/95 backdrop-blur-sm rounded-full border-2 border-red-200 shadow-xl hover:bg-red-50 hover:border-red-400 transition-all duration-300 transform hover:scale-110 active:scale-95"
      >
        <img src="/trash.svg" alt="Remove" class="w-5 h-5 opacity-80 hover:opacity-100" />
      </button>
    </div>

    <button
      v-if="onClickFavorite"
      @click.stop="onClickFavorite"
      class="absolute top-4 left-4 z-30 p-2.5 bg-white/95 backdrop-blur-sm rounded-full shadow-xl hover:scale-110 transition-all duration-300 transform active:scale-95"
      :class="isFavorite ? 'bg-cookie-100/95 border-2 border-cookie-400' : 'border-2 border-beige-300 hover:border-cookie-400'"
    >
      <img
        :src="isFavorite ? '/like-2.svg' : '/like-1.svg'"
        class="w-6 h-6"
        alt="Favorite"
      />
    </button>

    <!-- Изображение товара - крупное, как в Crumbl -->
    <div 
      class="relative overflow-hidden bg-white h-80 flex-shrink-0 flex items-center justify-center transition-all duration-500" 
      @click="handleProductClick"
    >
      <img 
        :src="imageUrl" 
        :alt="title"
        class="w-full h-full object-contain transition-transform duration-700 p-8"
        :class="{'scale-105': isHovered, 'scale-100': !isHovered}"
      />
    </div>

    <!-- Информационный блок в стиле Crumbl - коричневый фон с белым текстом -->
    <div class="bg-gradient-to-br from-brown-700 via-brown-600 to-brown-800 p-6 md:p-8 text-white flex-1 flex flex-col">
      <!-- Название продукта -->
      <h3 class="font-black text-2xl md:text-2xl lg:text-3xl mb-4 leading-tight flex-grow">
        {{ title }}
      </h3>

      <!-- Цена -->
      <div class="mb-5">
        <div class="flex items-baseline gap-2">
          <span class="text-white/80 text-sm md:text-base font-semibold">Цена:</span>
          <span class="text-3xl md:text-4xl font-black text-white">{{ price }}</span>
          <span class="text-white/80 text-lg md:text-xl font-bold">BYN</span>
        </div>
      </div>

      <!-- Кнопки действий -->
      <div class="flex flex-col sm:flex-row gap-3 mt-auto">
        <button
          @click.stop="handleProductClick"
          class="flex-1 px-6 py-3 bg-white text-brown-700 rounded-lg font-bold text-sm hover:bg-brown-50 transition-all duration-300 transform hover:scale-105 active:scale-95 border-2 border-transparent hover:border-brown-300"
        >
          Подробнее
        </button>
        <button
          v-if="onClickAdd"
          @click.stop="onClickAdd"
          class="flex-1 px-6 py-3 rounded-lg font-bold text-sm transition-all duration-300 transform hover:scale-105 active:scale-95"
          :class="isAdded 
            ? 'bg-green-500 hover:bg-green-600 text-white' 
            : 'bg-white text-brown-700 hover:bg-brown-50 border-2 border-transparent hover:border-brown-300'"
        >
          <span class="flex items-center justify-center gap-2">
            <img
              :src="!isAdded ? '/plus.svg' : '/checked.svg'"
              alt="Add to cart"
              class="w-5 h-5"
              :class="isAdded ? 'filter brightness-0 invert' : ''"
            />
            {{ isAdded ? 'В корзине' : 'Заказать' }}
          </span>
        </button>
      </div>
    </div>
  </div>
</template>
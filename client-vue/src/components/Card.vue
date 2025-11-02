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
    class="relative bg-white border border-slate-100 rounded-3xl p-8 cursor-pointer transition-all duration-300 hover:shadow-xl hover:-translate-y-1"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <div v-if="onClickRemove" class="absolute top-3 left-3 z-10">
      <button
        @click.stop="onClickRemove"
        class="p-2 bg-white rounded-full border border-slate-200 shadow-sm hover:bg-red-50 hover:border-red-200 transition-all duration-300"
      >
        <img src="/trash.svg" alt="Remove" class="w-5 h-5 opacity-80 hover:opacity-100" />
      </button>
    </div>

    <img
      v-if="onClickFavorite"
      :src="isFavorite ? '/like-2.svg' : '/like-1.svg'"
      class="absolute top-4 left-4 hover:scale-110 transition z-10"
      @click.stop="onClickFavorite"
      alt="Favorite"
    />

    <div class="relative overflow-hidden rounded-lg mb-4 h-48" @click="handleProductClick">
      <img 
        :src="imageUrl" 
        :alt="title"
        class="w-full h-full object-contain transition-transform duration-300"
        :class="{'scale-105': isHovered}"
      />

      <div
        class="absolute inset-0 flex items-center justify-center bg-black bg-opacity-0 transition-all duration-300"
        :class="{'bg-opacity-20': isHovered}"
      >
        <button
          v-show="isHovered"
          class="bg-white text-lime-600 px-4 py-2 rounded-full font-medium shadow-lg transform transition-all duration-300 hover:scale-105"
          @click.stop="handleProductClick"
        >
          Подробнее →
        </button>
      </div>
    </div>

    <p class="mt-2 font-medium text-lg">{{ title }}</p>

    <div class="flex justify-between mt-3 items-center">
      <div class="flex flex-col">
        <span class="text-slate-400 text-sm">Цена:</span>
        <b class="text-lg">{{ price }} бун.</b>
      </div>

      <img
        v-if="onClickAdd"
        @click.stop="onClickAdd"
        :src="!isAdded ? '/plus.svg' : '/checked.svg'"
        alt="Add to cart"
        class="w-8 h-8 hover:scale-110 transition cursor-pointer"
      />
    </div>
  </div>
</template>
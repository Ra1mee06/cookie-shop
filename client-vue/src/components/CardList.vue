<script setup>
import Card from './Card.vue'

defineProps({
  items: Array,
  isFavorites: Boolean
})

const emit = defineEmits(['addToFavorite', 'addToCart', 'removeFromFavorites', 'productClick'])

const handleProductClick = (product) => {
  emit('productClick', product)
}
</script>

<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5" v-auto-animate>
    <Card
      v-for="item in items"
      :key="item.id"
      :id="item.id"
      :title="item.title"
      :imageUrl="item.imageUrl"
      :price="item.price"
      :onClickFavorite="isFavorites ? null : () => emit('addToFavorite', item)"
      :onClickAdd="isFavorites ? null : () => emit('addToCart', item)"
      :onClickRemove="isFavorites ? () => emit('removeFromFavorites', item) : null"
      :isFavorite="item.isFavorite"
      :isAdded="item.isAdded"
      @product-click="handleProductClick"
    />
  </div>
</template>
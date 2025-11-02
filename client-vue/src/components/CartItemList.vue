<script setup>
import { inject } from 'vue'
import CartItem from './CartItem.vue'

const { 
  cart, 
  removeFromCart,
  increaseQuantity,
  decreaseQuantity 
} = inject('cart')
</script>

<template>
  <div class="flex flex-col flex-1 gap-4 justify-between" v-auto-animate>
    <CartItem
      v-for="item in cart"
      :key="item.id"
      :title="item.title"
      :quantity="item.quantity"
      :price="item.price"
      :image-url="item.imageUrl"
      @on-click-remove="() => removeFromCart(item)"
      @on-click-add="() => increaseQuantity(item)"
      @on-click-delete="() => decreaseQuantity(item)"
    />
  </div>
</template>
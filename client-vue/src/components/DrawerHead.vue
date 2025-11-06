<script setup>
import { useRouter } from 'vue-router';
import { inject } from 'vue';

const router = useRouter();
const { closeDrawer } = inject('cart');
</script>

<template>
  <div class="flex items-center justify-between gap-5 p-6 border-b-2 border-beige-200 bg-gradient-to-r from-white to-cookie-50/30">
    <div class="flex items-center gap-4">
      <button
        @click="closeDrawer"
        class="p-2 rounded-lg hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95"
      >
        <svg
          class="w-6 h-6 text-brown-700"
          width="16"
          height="14"
          viewBox="0 0 16 14"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1 7H14.7143"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
          <path
            d="M8.71436 1L14.7144 7L8.71436 13"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          />
        </svg>
      </button>
      <h2 class="text-2xl md:text-3xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
        Корзина
      </h2>
    </div>
  </div>
</template>

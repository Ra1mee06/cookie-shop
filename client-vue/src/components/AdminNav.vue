<script setup>
import { useRoute } from 'vue-router'

const route = useRoute()
const is = (p) => route.path.startsWith(p)
</script>

<template>
  <nav class="flex flex-wrap gap-3 mb-8 p-4 bg-gradient-to-r from-brown-50 to-beige-50 rounded-2xl border-2 border-beige-200">
    <router-link 
      to="/admin/orders" 
      class="px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      :class="is('/admin/orders') 
        ? 'bg-gradient-to-r from-brown-700 to-brown-800 text-white shadow-lg' 
        : 'bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50'"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        Заказы
      </span>
    </router-link>
    <router-link 
      to="/admin/users" 
      class="px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      :class="is('/admin/users') 
        ? 'bg-gradient-to-r from-brown-700 to-brown-800 text-white shadow-lg' 
        : 'bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50'"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
        Пользователи
      </span>
    </router-link>
    <router-link 
      to="/admin/suggestions" 
      class="px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      :class="is('/admin/suggestions') 
        ? 'bg-gradient-to-r from-brown-700 to-brown-800 text-white shadow-lg' 
        : 'bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50'"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
        </svg>
        Предложения
      </span>
    </router-link>
    <router-link 
      to="/admin/promocodes" 
      class="px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      :class="is('/admin/promocodes') 
        ? 'bg-gradient-to-r from-brown-700 to-brown-800 text-white shadow-lg' 
        : 'bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50'"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        Промокоды
      </span>
    </router-link>
    <router-link 
      to="/admin/invites" 
      class="px-5 py-3 rounded-xl font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      :class="is('/admin/invites') 
        ? 'bg-gradient-to-r from-brown-700 to-brown-800 text-white shadow-lg' 
        : 'bg-white text-brown-700 border-2 border-beige-300 hover:border-brown-400 hover:bg-brown-50'"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
        </svg>
        Инвайты
      </span>
    </router-link>
  </nav>
</template>



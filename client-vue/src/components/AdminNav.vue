<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
const is = (p) => route.path.startsWith(p)
</script>

<template>
  <nav class="flex flex-wrap gap-2 mb-4">
    <router-link to="/admin/orders" class="px-3 py-2 rounded border" :class="is('/admin/orders') ? 'bg-black text-white' : 'bg-white'">Заказы</router-link>
    <router-link to="/admin/users" class="px-3 py-2 rounded border" :class="is('/admin/users') ? 'bg-black text-white' : 'bg-white'">Пользователи</router-link>
    <router-link to="/admin/promocodes" class="px-3 py-2 rounded border" :class="is('/admin/promocodes') ? 'bg-black text-white' : 'bg-white'">Промокоды</router-link>
    <router-link to="/admin/invites" class="px-3 py-2 rounded border" :class="is('/admin/invites') ? 'bg-black text-white' : 'bg-white'">Инвайты</router-link>
  </nav>
</template>



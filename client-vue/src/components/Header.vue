<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();
const currentRoute = computed(() => router.currentRoute.value.path);

defineProps({
  totalPrice: Number,
});

const emit = defineEmits(['openDrawer']);
</script>

<template>
  <header class="flex justify-between border-b border-slate-200 px-10 py-8">
    <router-link to="/" class="group"> 
      <div class="flex items-center gap-4 ">
        <img 
  src="/logo.png" 
  alt="Logo" 
  class="w-14 transition-all duration-500 hover:scale-110 hover:rotate-3 hover:shadow-md"
/>
        <div>
          <h2 class="text-xl font-bold uppercase flex items-center gap-3 text-gray-500 hover:text-black p-2 rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 hover:shadow-md">RЯЛЬНА COOKIES</h2>
          <p class="text-slate-400">Самые лучше cookies в истории</p>
        </div>
      </div>
    </router-link>

    <ul class="flex items-center gap-10">
      <li
        @click="() => emit('openDrawer')"
        class="flex items-center cursor-pointer gap-3 text-gray-500 hover:text-black transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 hover:shadow-md p-2 rounded-lg"
      >
        <img src="/cart.svg" alt="Cart" class="transition-all duration-300" />
        <b>{{ totalPrice }} бун.</b>
      </li>

      <router-link 
        to="/favorites" 
        class="flex items-center gap-3 p-2 rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 hover:shadow-md"
        :class="currentRoute === '/favorites' ? 'text-black' : 'text-gray-500 hover:text-black'"
      >
        <img src="/heart.svg" alt="Favorites" class="transition-all duration-300" />
        <span>Закладки</span>
      </router-link>

      <router-link 
        to="/profile" 
        class="flex items-center gap-3 text-gray-500 hover:text-black p-2 rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 hover:shadow-md"
      >
        <img src="/profile.svg" alt="Profile" class="transition-all duration-300" />
        <span>Профиль</span>
      </router-link>
    </ul>
  </header>
</template>
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
  <header class="sticky top-0 z-50 w-full flex flex-col md:flex-row justify-between items-center border-b-2 border-beige-200 px-6 md:px-12 py-6 bg-gradient-to-r from-white to-cookie-50/30 backdrop-blur-md bg-white/95 shadow-md">
    <div class="w-full max-w-[95%] xl:max-w-[1600px] mx-auto flex flex-col md:flex-row justify-between items-center">
    <router-link to="/" class="group mb-4 md:mb-0"> 
      <div class="flex items-center gap-4">
        <div class="relative">
          <img 
            src="/logo.png" 
            alt="Logo" 
            class="w-16 h-16 transition-all duration-500 hover:scale-110 hover:rotate-6 hover:shadow-lg rounded-full"
          />
          <div class="absolute -top-1 -right-1 w-5 h-5 bg-cookie-500 rounded-full animate-pulse"></div>
        </div>
        <div>
          <h2 class="text-2xl md:text-3xl font-extrabold uppercase flex items-center gap-3 bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent group-hover:from-cookie-500 group-hover:to-brown-600 transition-all duration-300">
            RЯЛЬНА COOKIES
          </h2>
          <p class="text-brown-600 font-medium text-sm md:text-base">Самые лучшие cookies в истории</p>
        </div>
      </div>
    </router-link>

    <nav class="flex items-center gap-4 md:gap-8 flex-wrap justify-center">
      <button
        @click="() => emit('openDrawer')"
        class="flex items-center cursor-pointer gap-2 md:gap-3 px-4 py-2 rounded-xl bg-gradient-to-r from-cookie-500 to-cookie-600 text-white font-semibold shadow-md hover:shadow-lg transition-all duration-300 transform hover:-translate-y-1 hover:scale-105 active:scale-95"
      >
        <img src="/cart.svg" alt="Cart" class="w-5 h-5 md:w-6 md:h-6 filter brightness-0 invert" />
        <span class="text-sm md:text-base">{{ totalPrice }} бун.</span>
      </button>

      <router-link 
        to="/favorites" 
        class="flex items-center gap-2 md:gap-3 px-4 py-2 rounded-xl transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
        :class="currentRoute === '/favorites' 
          ? 'text-cookie-600 bg-cookie-100 font-semibold shadow-md' 
          : 'text-brown-600 hover:text-cookie-600 hover:bg-cookie-50'"
      >
        <img src="/heart.svg" alt="Favorites" class="w-5 h-5 md:w-6 md:h-6" />
        <span class="text-sm md:text-base font-medium">Закладки</span>
      </router-link>

      <router-link 
        to="/profile" 
        class="flex items-center gap-2 md:gap-3 px-4 py-2 rounded-xl text-brown-600 hover:text-cookie-600 hover:bg-cookie-50 transition-all duration-300 transform hover:-translate-y-1 hover:scale-105"
      >
        <img src="/profile.svg" alt="Profile" class="w-5 h-5 md:w-6 md:h-6" />
        <span class="text-sm md:text-base font-medium">Профиль</span>
      </router-link>
    </nav>
    </div>
  </header>
</template>
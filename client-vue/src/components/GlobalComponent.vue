<script setup>
import { onMounted } from 'vue';
import { useCart } from '../composables/useCart';
import { useFavorites } from '../composables/useFavorites';
import { useRouter } from 'vue-router';
import Header from './Header.vue';
import Drawer from './Drawer.vue';
import Footer from './Footer.vue';

const {
  drawerOpen,
  totalPrice,
  vatPrice,
  openDrawer,
  closeDrawer,
} = useCart();

// Инициализируем избранное для глобального состояния и загружаем данные с сервера
const { fetchFavorites } = useFavorites();

// Загружаем избранное при монтировании компонента
onMounted(async () => {
  // Восстанавливаем userId если пользователь залогинен
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    try {
      const parsedUser = JSON.parse(savedUser)
      if (parsedUser?.id && !localStorage.getItem('userId')) {
        localStorage.setItem('userId', parsedUser.id.toString())
      }
    } catch (e) {
      console.error('Ошибка при восстановлении userId:', e)
    }
  }
  
  // Загружаем избранное с сервера при инициализации приложения
  // Используем force=true для гарантии актуальных данных при первой загрузке
  try {
    await fetchFavorites(true);
  } catch (err) {
    console.error('Ошибка при загрузке избранного при инициализации:', err);
  }
});
</script>

<template>
  <div class="min-h-screen flex flex-col">
    <!-- Header вынесен наружу для правильной работы sticky -->
    <Header 
      :total-price="totalPrice" 
      @open-drawer="openDrawer" 
    />
    
    <div class="bg-white w-full max-w-[95%] xl:max-w-[1600px] m-auto rounded-2xl shadow-2xl mt-8 mb-8 overflow-hidden flex-1">
      <div class="p-6 md:p-8 lg:p-12">
        <router-view ></router-view>
      </div>
    </div>

    <Footer />

    <Drawer 
      v-if="drawerOpen" 
      :total-price="totalPrice" 
      :vat-price="vatPrice" 
    />
  </div>
</template>
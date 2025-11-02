<script setup>
import { onMounted } from 'vue';
import { useCart } from '../composables/useCart';
import { useFavorites } from '../composables/useFavorites';
import { useRouter } from 'vue-router';
import Header from './Header.vue';
import Drawer from './Drawer.vue';

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
  <div class="bg-white w-4/5 m-auto rounded-xl shadow-xl mt-14">
    <Header 
      :total-price="totalPrice" 
      @open-drawer="openDrawer" 
    ></Header>
    
    <div class="p-10">
      <router-view ></router-view>
    </div>
  </div>

  <Drawer 
    v-if="drawerOpen" 
    :total-price="totalPrice" 
    :vat-price="vatPrice" 
  />
</template>
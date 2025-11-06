<script setup>
import { computed, onMounted, inject, ref } from 'vue'
import CardList from '../components/CardList.vue'
import ProductModal from '../components/ProductModal.vue'

// Используем глобальное состояние избранного через inject
const favoritesState = inject('favorites')
if (!favoritesState) {
  throw new Error('Favorites state not provided. Make sure useFavorites is called in GlobalComponent.')
}
const { favorites, fetchFavorites, removeFavorite, clearAllFavorites } = favoritesState

// Состояние для модального окна товара
const selectedProduct = ref(null)
const showModal = ref(false)

const openProduct = (product) => {
  selectedProduct.value = product
  showModal.value = true
}

// Преобразуем данные для совместимости с CardList, используя computed для реактивности
const favoriteItems = computed(() => {
  return favorites.value.map(fav => ({
    ...fav.product,
    favoriteId: fav.id,
    isFavorite: true
  }));
});

const removeFromFavorites = async (item) => {
  try {
    await removeFavorite(item.favoriteId);
    // favorites.value уже обновляется в toggleFavorite/removeFavorite
  } catch (err) {
    console.error("Ошибка при удалении из избранного:", err);
    // При ошибке перезагружаем данные с сервера
    await fetchFavorites();
  }
};

const clearFavorites = async () => {
  try {
    await clearAllFavorites();
    // favorites.value уже очищается в clearAllFavorites
  } catch (err) {
    console.error("Ошибка при очистке избранного:", err);
    // При ошибке перезагружаем данные с сервера
    await fetchFavorites();
  }
};

onMounted(async () => {
  // Загружаем избранное при монтировании страницы
  // Используем force=true чтобы гарантировать актуальные данные с сервера
  try {
    await fetchFavorites(true);
  } catch (err) {
    console.error('Ошибка при загрузке избранного на странице Favorites:', err);
  }
});
</script>

<template>
  <div class="space-y-8">
    <!-- Header Section -->
    <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
      <div>
        <h2 class="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-2">
          Мои закладки
        </h2>
        <p class="text-brown-600" v-if="favoriteItems.length > 0">
          У вас {{ favoriteItems.length }} {{ favoriteItems.length === 1 ? 'товар' : favoriteItems.length < 5 ? 'товара' : 'товаров' }} в избранном
        </p>
      </div>
      <button
        @click="clearFavorites"
        class="px-6 py-3 bg-red-500 text-white rounded-xl font-semibold hover:bg-red-600 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
        :disabled="favoriteItems.length === 0"
      >
        <span class="flex items-center gap-2">
          <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
          Очистить всё
        </span>
      </button>
    </div>

    <div v-if="favoriteItems.length === 0" class="text-center py-16">
      <div class="mx-auto w-32 h-32 mb-6 rounded-full bg-gradient-to-br from-cookie-100 to-beige-100 flex items-center justify-center">
        <svg class="w-16 h-16 text-cookie-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
        </svg>
      </div>
      <h3 class="text-2xl font-bold text-brown-700 mb-2">Закладки пусты</h3>
      <p class="text-brown-600 mb-6">Добавьте товары в избранное, чтобы они появились здесь</p>
    </div>

    <CardList 
      v-else
      :items="favoriteItems" 
      is-favorites 
      @remove-from-favorites="removeFromFavorites"
      @product-click="openProduct"
    />

    <ProductModal 
      v-if="showModal"
      :product="selectedProduct"
      @close="showModal = false"
    />
  </div>
</template>
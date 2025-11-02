<script setup>
import { computed, onMounted, inject } from 'vue'
import CardList from '../components/CardList.vue'

// Используем глобальное состояние избранного через inject
const favoritesState = inject('favorites')
if (!favoritesState) {
  throw new Error('Favorites state not provided. Make sure useFavorites is called in GlobalComponent.')
}
const { favorites, fetchFavorites, removeFavorite, clearAllFavorites } = favoritesState

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
  <div class="flex justify-between items-center mb-8">
    <h2 class="text-3xl font-bold">Мои закладки</h2>
    <button
      @click="clearFavorites"
      class="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg disabled:opacity-50 disabled:cursor-not-allowed"
      :disabled="favoriteItems.length === 0"
    >
      Очистить всё
    </button>
  </div>

  <CardList 
    :items="favoriteItems" 
    is-favorites 
    @remove-from-favorites="removeFromFavorites"
  />
</template>
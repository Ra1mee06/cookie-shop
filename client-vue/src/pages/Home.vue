<script setup lang="ts">
import { reactive, watch, ref, onMounted } from 'vue'
import { useApi } from '../composables/useApi'
import debounce from 'lodash.debounce'
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import CardList from '../components/CardList.vue'
import { useSuggestions } from '@/composables/useSuggestions';
import ProductModal from '@/components/ProductModal.vue'

const router = useRouter()

const { products: productsApi } = useApi()
const selectedProduct = ref(null)
const showModal = ref(false)

const openProduct = (product) => {
  selectedProduct.value = product
  showModal.value = true
}

const { cart, addToCart, removeFromCart } = inject('cart')

// Используем глобальное состояние избранного через inject
const favoritesState = inject('favorites')
if (!favoritesState) {
  throw new Error('Favorites state not provided. Make sure useFavorites is called in GlobalComponent.')
}
const { favorites, fetchFavorites, toggleFavorite } = favoritesState

const { 
  showSuggestionForm, 
  isLoading, 
  isSubmitted, 
  showAuthModal,
  suggestionId,
  suggestion, 
  isValid, 
  submitSuggestion, 
  resetForm 
} = useSuggestions()

const goToProfile = () => {
  showAuthModal.value = false
  showSuggestionForm.value = false
  router.push('/profile')
}

const closeAuthModal = () => {
  showAuthModal.value = false
}

const openForm = () => {
  resetForm();
  showSuggestionForm.value = true;
};

const closeForm = () => {
  showSuggestionForm.value = false;
};

const submitForm = async () => {
  await submitSuggestion();
};

const items = ref([])

const filters = reactive({
  sortBy: 'title',
  searchQuery: ''
})

const onClickAddPlus = (item) => {
  if (!item.isAdded) {
    addToCart(item)
  } else {
    removeFromCart(item)
  }
}

const onChangeSelect = (event) => {
  filters.sortBy = event.target.value
}

const onChangeSearchInput = debounce((event) => {
  filters.searchQuery = event.target.value
}, 300)

const addToFavorite = async (item) => {
  try {
    await toggleFavorite(item);
    // После успешного обновления обновляем статусы, чтобы убедиться в синхронизации
    // Это гарантирует, что favoriteId правильно установлен для всех товаров
    updateItemsFavoritesStatus();
  } catch (err) {
    console.error("Ошибка при добавлении в избранное:", err);
    // При ошибке синхронизируем состояние с сервером, чтобы откатить изменения
    // Используем force=false чтобы не потерять существующие данные при ошибке загрузки
    await fetchFavorites(false);
    updateItemsFavoritesStatus();
  }
};

const updateItemsFavoritesStatus = () => {
  // Используем глобальное состояние favorites напрямую, без дополнительного запроса к серверу
  const favoritesList = favorites.value || [];
  
  // Создаем Map для быстрого поиска избранных товаров по product.id
  const favoritesMap = new Map();
  favoritesList.forEach(fav => {
    if (fav.product && fav.product.id) {
      favoritesMap.set(fav.product.id, fav);
    }
  });
  
  items.value = items.value.map(item => {
    const favorite = favoritesMap.get(item.id);
    return {
      ...item,
      isFavorite: !!favorite,
      favoriteId: favorite?.id || null
    };
  });
};

const fetchItems = async () => {
  try {
    const params = {};
    
    // Преобразуем параметры сортировки для Java сервера
    if (filters.sortBy === 'price') {
      params.sortBy = 'price';
      params.sortOrder = 'asc';
    } else if (filters.sortBy === '-price') {
      params.sortBy = 'price';
      params.sortOrder = 'desc';
    } else {
      params.sortBy = 'title';
    }

    if (filters.searchQuery) {
      params.search = filters.searchQuery;
    }

    const response = await productsApi.getAll(params);
    
    // Сначала загружаем товары без статусов избранного
    const loadedItems = response.data.map(obj => ({
      ...obj,
      isFavorite: false,
      favoriteId: null,
      isAdded: false
    }));
    
    // Сразу обновляем статусы избранного на основе уже загруженного favorites.value
    // Создаем Map для быстрого поиска избранных товаров по product.id
    const favoritesList = favorites.value || [];
    const favoritesMap = new Map();
    favoritesList.forEach(fav => {
      if (fav.product && fav.product.id) {
        favoritesMap.set(fav.product.id, fav);
      }
    });
    
    // Применяем статусы избранного к загруженным товарам
    items.value = loadedItems.map(item => {
      const favorite = favoritesMap.get(item.id);
      return {
        ...item,
        isFavorite: !!favorite,
        favoriteId: favorite?.id || null
      };
    });
  } catch (err) {
    console.error("Ошибка загрузки товаров:", err);
    items.value = [];
  }
};

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

  const localCart = localStorage.getItem('cart')
  cart.value = localCart ? JSON.parse(localCart) : []
  
  cart.value = cart.value.map(item => ({
    ...item,
    quantity: item.quantity || 1
  }))

  // ВАЖНО: Сначала загружаем избранное, чтобы статусы товаров были правильными
  // Это предотвращает race condition, когда товары загружаются раньше избранного
  // Используем force=true для гарантии актуальных данных при первой загрузке
  try {
    await fetchFavorites(true)
  } catch (err) {
    console.error('Ошибка при загрузке избранного на странице Home:', err)
  }

  // Теперь загружаем товары - они автоматически получат правильные статусы избранного
  await fetchItems()
  
  // Дополнительно обновляем статусы на случай, если избранное изменилось во время загрузки
  updateItemsFavoritesStatus()
})

watch(cart, () => {
  items.value = items.value.map((item) => ({
    ...item,
    isAdded: cart.value.some((cartItem) => cartItem.id === item.id)
  }))
}, { deep: true })

// Следим за изменениями в избранном и обновляем статусы товаров
watch(favorites, () => {
  // Обновляем статусы товаров только если товары уже загружены
  if (items.value.length > 0) {
    updateItemsFavoritesStatus()
  }
}, { deep: true, immediate: false })

watch(filters, fetchItems, { deep: true })
</script>

<template>
  <!-- Остальная часть шаблона остается БЕЗ ИЗМЕНЕНИЙ -->
  <div class="flex justify-between items-center">
    <h2 class="text-3xl font-bold mb-8">Все изделия</h2>

    <div class="flex gap-4">
      <select @change="onChangeSelect" class="py-2 px-3 border rounded-md outline-none">
        <option value="title">По названию</option>
        <option value="price">По цене (дешевые)</option>
        <option value="-price">По цене (дорогие)</option>
      </select>

      <div class="relative">
        <img class="absolute left-4 top-3" src="/search.svg" />
        <input
          @input="onChangeSearchInput"
          class="border rounded-md py-2 pl-11 pr-4 outline-none focus:border-gray-400"
          type="text"
          placeholder="Поиск..."
        />
      </div>
    </div>
    
  </div>

  <div class="mt-10">
    <CardList 
      :items="items"
      @add-to-favorite="addToFavorite"
      @add-to-cart="onClickAddPlus"
      @product-click="openProduct"
    />

    <ProductModal 
      v-if="showModal"
      :product="selectedProduct"
      @close="showModal = false"
    />
  </div>

  <div class="w-full flex justify-center mt-8 mb-12"> 
    <button 
      @click="openForm"
      class="px-6 py-3 bg-indigo-500 text-white rounded-xl transition-all duration-300 
             hover:-translate-y-1 hover:shadow-lg hover:bg-indigo-600 focus:outline-none
             transform mx-auto" 
    >
      Предложить товар
    </button>
  </div>

  <!-- Модальное окно предложения товара остается БЕЗ ИЗМЕНЕНИЙ -->
  <transition name="modal-fade">
    <div 
      v-if="showSuggestionForm" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <transition name="modal-content">
        <div 
          v-if="showSuggestionForm"
          class="bg-white p-8 rounded-xl w-full max-w-md shadow-2xl"
        >
          <div v-if="!isSubmitted">
            <h3 class="text-2xl font-bold mb-6 text-center text-gray-800">Ваше предложение</h3>
            
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Ваше имя</label>
                <input 
                  v-model="suggestion.author"
                  class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Как к вам обращаться?"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Название товара</label>
                <input 
                  v-model="suggestion.productName"
                  class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                  placeholder="Какой товар предлагаете?"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Описание</label>
                <textarea 
                  v-model="suggestion.description"
                  class="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 h-32"
                  placeholder="Опишите товар или ваше предложение"
                ></textarea>
              </div>
            </div>

            <div class="flex justify-end gap-4 mt-8">
              <button 
                @click="closeForm"
                class="px-5 py-2.5 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors duration-200"
              >
                Отмена
              </button>
              <button 
                @click="submitForm"
                :disabled="!isValid() || isLoading"
                class="px-5 py-2.5 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 
                       disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 transform hover:-translate-y-0.5 hover:shadow-md"
              >
                <span v-if="!isLoading">Отправить</span>
                <span v-else class="flex items-center justify-center gap-2">
                  <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Отправка...
                </span>
              </button>
            </div>
          </div>

          <div v-else class="text-center py-6">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-green-100 mb-4">
              <svg class="h-6 w-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-800 mb-2">Спасибо за ваше предложение!</h3>
            <p class="text-gray-600 mb-4" v-if="suggestionId">
              Номер вашего предложения: <strong class="text-indigo-600">#{{ suggestionId }}</strong>
            </p>
            <p class="text-gray-600 mb-6">Мы обязательно рассмотрим его в ближайшее время.</p>
            <button 
              @click="closeForm"
              class="px-6 py-3 bg-indigo-500 text-white rounded-xl hover:bg-indigo-600 
                     transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg mx-auto"
            >
              Закрыть форму
            </button>
          </div>
        </div>
      </transition>
    </div>
  </transition>

  <!-- Модальное окно авторизации для предложений -->
  <transition name="modal-fade">
    <div 
      v-if="showAuthModal" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="closeAuthModal"
    >
      <transition name="modal-content">
        <div 
          v-if="showAuthModal"
          class="bg-white p-8 rounded-xl w-full max-w-md shadow-2xl"
          @click.stop
        >
          <div class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100 mb-4">
              <svg class="h-6 w-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-bold text-gray-800 mb-2">Вы не авторизованы</h3>
            <p class="text-gray-600 mb-6">Для отправки предложения необходимо авторизоваться. Пожалуйста, войдите в систему.</p>
            <div class="flex gap-4 justify-center">
              <button 
                @click="closeAuthModal"
                class="px-6 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
              >
                Отмена
              </button>
              <button 
                @click="goToProfile"
                class="px-6 py-2 bg-indigo-500 text-white rounded-lg hover:bg-indigo-600 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg"
              >
                Перейти к авторизации
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>
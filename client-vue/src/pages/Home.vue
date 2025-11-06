<script setup lang="ts">
import { reactive, watch, ref, onMounted } from 'vue'
import { useApi } from '../composables/useApi'
import debounce from 'lodash.debounce'
import { inject } from 'vue'
import { useRouter } from 'vue-router'
import CardList from '../components/CardList.vue'
import { useSuggestions } from '@/composables/useSuggestions';
import ProductModal from '@/components/ProductModal.vue'
import NewsFeed from '@/components/NewsFeed.vue'
import DecorativeElements from '@/components/DecorativeElements.vue'

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

// Модальное окно для авторизации при добавлении в избранное
const showFavoriteAuthModal = ref(false)

const goToProfile = () => {
  showAuthModal.value = false
  showFavoriteAuthModal.value = false
  showSuggestionForm.value = false
  router.push('/profile')
}

const closeAuthModal = () => {
  showAuthModal.value = false
}

const closeFavoriteAuthModal = () => {
  showFavoriteAuthModal.value = false
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
    const result = await toggleFavorite(item);
    
    // Проверяем, требуется ли авторизация
    if (result && 'needsAuth' in result && result.needsAuth) {
      showFavoriteAuthModal.value = true;
      return;
    }
    
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
  <div class="relative space-y-8">
    <!-- Декоративные элементы -->
    <DecorativeElements />
    
    <!-- News Feed / Лента новостей и спецпредложений -->
    <div class="relative z-10">
      <NewsFeed />
    </div>
    
    <!-- Hero Section -->
    <div class="relative z-10 text-center mb-12 py-12 md:py-16 bg-gradient-to-r from-cookie-50 via-beige-50 to-brown-50 rounded-2xl border-2 border-beige-200 overflow-hidden">
      <!-- Декоративные элементы внутри hero -->
      <div class="absolute top-0 left-0 w-32 h-32 bg-cookie-200/20 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2"></div>
      <div class="absolute bottom-0 right-0 w-40 h-40 bg-brown-200/20 rounded-full blur-3xl translate-x-1/2 translate-y-1/2"></div>
      
      <div class="relative z-10">
        <div class="inline-block mb-6 animate-bounce">
          <div class="w-24 h-24 md:w-32 md:h-32 mx-auto rounded-full bg-gradient-to-br from-cookie-400 to-brown-500 shadow-2xl flex items-center justify-center transform hover:scale-110 transition-transform duration-300">
            <span class="text-4xl md:text-6xl">🍪</span>
          </div>
        </div>
        <h1 class="text-4xl md:text-6xl font-extrabold mb-4 bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent animate-fade-in">
          Добро пожаловать в RЯЛЬНА COOKIES!
        </h1>
        <p class="text-lg md:text-xl text-brown-600 font-medium animate-fade-in-delay">
          Откройте для себя самые вкусные печеньки в истории
        </p>
        <div class="mt-8 flex flex-wrap justify-center gap-4">
          <div class="px-6 py-3 bg-white/80 backdrop-blur-sm rounded-xl border-2 border-cookie-200 shadow-lg">
            <div class="text-2xl font-bold text-cookie-600">100%</div>
            <div class="text-sm text-brown-600">Натурально</div>
          </div>
          <div class="px-6 py-3 bg-white/80 backdrop-blur-sm rounded-xl border-2 border-cookie-200 shadow-lg">
            <div class="text-2xl font-bold text-cookie-600">24/7</div>
            <div class="text-sm text-brown-600">Доставка</div>
          </div>
          <div class="px-6 py-3 bg-white/80 backdrop-blur-sm rounded-xl border-2 border-cookie-200 shadow-lg">
            <div class="text-2xl font-bold text-cookie-600">⭐</div>
            <div class="text-sm text-brown-600">Лучшее качество</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filters Section -->
    <div class="relative z-10 flex flex-col md:flex-row justify-between items-start md:items-center gap-4 mb-8">
      <div class="flex items-center gap-4">
        <div class="hidden md:block w-16 h-1 bg-gradient-to-r from-cookie-500 to-brown-600 rounded-full"></div>
        <h2 class="text-3xl md:text-4xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
          Все изделия
        </h2>
        <div class="hidden md:block w-16 h-1 bg-gradient-to-r from-brown-600 to-cookie-500 rounded-full"></div>
      </div>

      <div class="flex flex-col sm:flex-row gap-4 w-full md:w-auto">
        <div class="relative flex-1 sm:flex-none">
          <img class="absolute left-4 top-1/2 transform -translate-y-1/2 w-5 h-5 opacity-60" src="/search.svg" />
          <input
            @input="onChangeSearchInput"
            class="input-field pl-12 pr-4"
            type="text"
            placeholder="Поиск по названию..."
          />
        </div>

        <select 
          @change="onChangeSelect" 
          class="input-field appearance-none bg-white cursor-pointer pr-10"
        >
          <option value="title">По названию</option>
          <option value="price">По цене (дешевые)</option>
          <option value="-price">По цене (дорогие)</option>
        </select>
      </div>
    </div>

  <div class="relative z-10 mt-10">
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

  <div class="relative z-10 w-full flex justify-center mt-12 mb-8"> 
    <button 
      @click="openForm"
      class="btn-primary text-lg px-8 py-4"
    >
      <span class="flex items-center gap-2">
        <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4" />
        </svg>
        Предложить товар
      </span>
    </button>
  </div>

  <!-- Модальное окно предложения товара -->
  <div class="relative z-50">
  <transition name="modal-fade">
    <div 
      v-if="showSuggestionForm" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
    >
      <transition name="modal-content">
        <div 
          v-if="showSuggestionForm"
          class="bg-white p-8 rounded-2xl w-full max-w-md shadow-2xl border-2 border-beige-200"
        >
          <div v-if="!isSubmitted">
            <h3 class="text-3xl font-extrabold mb-6 text-center bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
              Ваше предложение
            </h3>
            
            <div class="space-y-5">
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">Ваше имя</label>
                <input 
                  v-model="suggestion.author"
                  class="input-field"
                  placeholder="Как к вам обращаться?"
                >
              </div>
              
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">Название товара</label>
                <input 
                  v-model="suggestion.productName"
                  class="input-field"
                  placeholder="Какой товар предлагаете?"
                >
              </div>
              
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">Описание</label>
                <textarea 
                  v-model="suggestion.description"
                  class="input-field h-32 resize-none"
                  placeholder="Опишите товар или ваше предложение"
                ></textarea>
              </div>
            </div>

            <div class="flex justify-end gap-4 mt-8">
              <button 
                @click="closeForm"
                class="btn-secondary"
              >
                Отмена
              </button>
              <button 
                @click="submitForm"
                :disabled="!isValid() || isLoading"
                class="btn-primary"
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
            <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-cookie-100 to-green-100 mb-6 shadow-lg">
              <svg class="h-8 w-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="3" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
            <h3 class="text-3xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-3">
              Спасибо за ваше предложение!
            </h3>
            <p class="text-brown-600 mb-4 font-medium" v-if="suggestionId">
              Номер вашего предложения: <strong class="text-cookie-600 text-lg">#{{ suggestionId }}</strong>
            </p>
            <p class="text-brown-600 mb-8">Мы обязательно рассмотрим его в ближайшее время.</p>
            <button 
              @click="closeForm"
              class="btn-primary"
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
          class="bg-white p-8 rounded-2xl w-full max-w-md shadow-2xl border-2 border-beige-200"
          @click.stop
        >
          <div class="text-center">
            <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-yellow-100 to-orange-100 mb-6 shadow-lg">
              <svg class="h-8 w-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-3">
              Вы не авторизованы
            </h3>
            <p class="text-brown-600 mb-8">Для отправки предложения необходимо авторизоваться. Пожалуйста, войдите в систему.</p>
            <div class="flex gap-4 justify-center">
              <button 
                @click="closeAuthModal"
                class="btn-secondary"
              >
                Отмена
              </button>
              <button 
                @click="goToProfile"
                class="btn-primary"
              >
                Перейти к авторизации
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </transition>
  </div>

  <!-- Модальное окно авторизации для избранного -->
  <transition name="modal-fade">
    <div 
      v-if="showFavoriteAuthModal" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="closeFavoriteAuthModal"
    >
      <transition name="modal-content">
        <div 
          v-if="showFavoriteAuthModal"
          class="bg-white p-8 rounded-2xl w-full max-w-md shadow-2xl border-2 border-beige-200"
          @click.stop
        >
          <div class="text-center">
            <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-yellow-100 to-orange-100 mb-6 shadow-lg">
              <svg class="h-8 w-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-3">
              Вы не авторизованы
            </h3>
            <p class="text-brown-600 mb-8">Для добавления товара в избранное необходимо авторизоваться. Пожалуйста, войдите в систему.</p>
            <div class="flex gap-4 justify-center">
              <button 
                @click="closeFavoriteAuthModal"
                class="btn-secondary"
              >
                Отмена
              </button>
              <button 
                @click="goToProfile"
                class="btn-primary"
              >
                Перейти к авторизации
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </transition>
  </div>
</template>
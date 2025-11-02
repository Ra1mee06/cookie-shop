<script setup>
import { ref, computed, onMounted } from 'vue';
import { useApi } from '../composables/useApi';

const { auth: authApi, orders: ordersApi, suggestions: suggestionsApi } = useApi();

const isAuthenticated = ref(false);
const showLoginForm = ref(true);
const isLoading = ref(false);
const errorMessage = ref('');

const loginEmail = ref('');
const loginPassword = ref('');
const registerName = ref('');
const registerEmail = ref('');
const registerPassword = ref('');

const userData = ref({
  fullName: '',
  email: '',
  favorites: [],
  cartItems: []
});

const userOrders = ref([]);
const userSuggestions = ref([]);
const isLoadingOrders = ref(false);
const isLoadingSuggestions = ref(false);
const activeTab = ref('orders'); // 'orders' or 'suggestions'

const userInitials = computed(() => {
  if (!userData.value.fullName) return '?';
  return userData.value.fullName
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase();
});

const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('ru-RU', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const formatPrice = (price) => {
  return new Intl.NumberFormat('ru-RU', {
    style: 'currency',
    currency: 'RUB',
    minimumFractionDigits: 0
  }).format(price);
};

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': 'В обработке',
    'CONFIRMED': 'Подтверждён',
    'DELIVERED': 'Доставлен',
    'CANCELLED': 'Отменён'
  };
  return statusMap[status] || status;
};

const getStatusColor = (status) => {
  const colorMap = {
    'PENDING': 'bg-yellow-100 text-yellow-800',
    'CONFIRMED': 'bg-blue-100 text-blue-800',
    'DELIVERED': 'bg-green-100 text-green-800',
    'CANCELLED': 'bg-red-100 text-red-800'
  };
  return colorMap[status] || 'bg-gray-100 text-gray-800';
};

const loadUserOrders = async () => {
  if (!isAuthenticated.value) return;
  
  isLoadingOrders.value = true;
  try {
    const response = await ordersApi.getByUser();
    userOrders.value = response.data || [];
  } catch (error) {
    console.error('Ошибка загрузки заказов:', error);
    userOrders.value = [];
  } finally {
    isLoadingOrders.value = false;
  }
};

const loadUserSuggestions = async () => {
  if (!isAuthenticated.value) return;
  
  isLoadingSuggestions.value = true;
  try {
    const response = await suggestionsApi.getByUser();
    userSuggestions.value = response.data || [];
  } catch (error) {
    console.error('Ошибка загрузки предложений:', error);
    userSuggestions.value = [];
  } finally {
    isLoadingSuggestions.value = false;
  }
};

const isValidEmail = (email) => {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
};

const handleRegister = async () => {
  errorMessage.value = '';
  
  if (!registerName.value.trim()) {
    errorMessage.value = 'Введите имя';
    return;
  }
  
  if (!isValidEmail(registerEmail.value)) {
    errorMessage.value = 'Введите корректный email';
    return;
  }

  if (registerPassword.value.length < 6) {
    errorMessage.value = 'Пароль должен содержать минимум 6 символов';
    return;
  }

  isLoading.value = true;

  try {
    const response = await authApi.register({
      fullName: registerName.value.trim(),
      email: registerEmail.value.toLowerCase().trim(),
      password: registerPassword.value
    });

    const data = response.data;

    userData.value = {
      ...data.user,
      favorites: [],
      cartItems: []
    };
    
    isAuthenticated.value = true;
    localStorage.setItem('authToken', data.token || 'stub-token');
    localStorage.setItem('user', JSON.stringify(userData.value));
    // Сохраняем userId для работы с favorites
    if (data.user?.id) {
      localStorage.setItem('userId', data.user.id.toString());
    }
    
    registerName.value = '';
    registerEmail.value = '';
    registerPassword.value = '';
    
    // Загружаем историю заказов и предложений
    await Promise.all([loadUserOrders(), loadUserSuggestions()]);
    
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Ошибка регистрации. Попробуйте позже.';
    console.error('Registration error:', error);
  } finally {
    isLoading.value = false;
  }
};

const handleLogin = async () => {
  errorMessage.value = '';
  
  if (!isValidEmail(loginEmail.value)) {
    errorMessage.value = 'Введите корректный email';
    return;
  }

  isLoading.value = true;

  try {
    const response = await authApi.login({
      email: loginEmail.value.toLowerCase().trim(),
      password: loginPassword.value
    });

    const data = response.data;

    userData.value = {
      ...data.user,
      favorites: [],
      cartItems: []
    };
    
    isAuthenticated.value = true;
    localStorage.setItem('authToken', data.token || 'stub-token');
    localStorage.setItem('user', JSON.stringify(userData.value));
    // Сохраняем userId для работы с favorites
    if (data.user?.id) {
      localStorage.setItem('userId', data.user.id.toString());
    }
    
    loginEmail.value = '';
    loginPassword.value = '';
    
    // Загружаем историю заказов и предложений
    await Promise.all([loadUserOrders(), loadUserSuggestions()]);
    
  } catch (error) {
    errorMessage.value = error.response?.data?.message || 'Неверный email или пароль';
    console.error('Login error:', error);
  } finally {
    isLoading.value = false;
  }
};

const handleLogout = () => {
  isAuthenticated.value = false;
  userData.value = {
    fullName: '',
    email: '',
    favorites: [],
    cartItems: []
  };
  localStorage.removeItem('authToken');
  localStorage.removeItem('user');
  localStorage.removeItem('userId');
  showLoginForm.value = true;
};

const checkAuth = async () => {
  const token = localStorage.getItem('authToken');
  const savedUser = localStorage.getItem('user');
  
  if (token && savedUser) {
    try {
      const parsedUser = JSON.parse(savedUser);
      if (parsedUser?.email) {
        userData.value = parsedUser;
        isAuthenticated.value = true;
        // Восстанавливаем userId если есть
        if (parsedUser?.id) {
          localStorage.setItem('userId', parsedUser.id.toString());
        }
        // Загружаем историю заказов и предложений
        await Promise.all([loadUserOrders(), loadUserSuggestions()]);
      }
    } catch (e) {
      localStorage.removeItem('authToken');
      localStorage.removeItem('user');
      localStorage.removeItem('userId');
    }
  }
};

onMounted(checkAuth);
</script>

<template>
  <div class="max-w-2xl mx-auto mt-12 p-8 bg-white rounded-xl shadow-md">
    <h2 class="text-3xl font-bold mb-8 text-center">Профиль</h2>
    
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
      {{ errorMessage }}
    </div>
    
    <div v-if="!isAuthenticated">
      <div class="space-y-4" v-if="showLoginForm">
        <h3 class="text-xl font-semibold text-center">Вход в аккаунт</h3>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            type="email" 
            v-model="loginEmail"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-lime-500 focus:border-lime-500 transition-all"
            placeholder="Ваш email"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
          <input 
            type="password" 
            v-model="loginPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-lime-500 focus:border-lime-500 transition-all"
            placeholder="Ваш пароль"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <button
          @click="handleLogin"
          :disabled="isLoading"
          class="w-full mt-4 py-3 bg-lime-500 text-white rounded-xl transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-lime-600 active:bg-lime-700 active:scale-95 disabled:opacity-70"
        >
          <span v-if="!isLoading">Войти</span>
          <span v-else>Вход...</span>
        </button>
        
        <p class="text-center text-sm text-gray-500 mt-2">
          Нет аккаунта? 
          <button 
            @click="showLoginForm = false"
            class="text-lime-600 hover:text-lime-800 font-medium transition-colors"
          >
            Зарегистрироваться
          </button>
        </p>
      </div>
      
      <div class="space-y-4" v-else>
        <h3 class="text-xl font-semibold text-center">Регистрация</h3>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Имя</label>
          <input 
            type="text" 
            v-model="registerName"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-lime-500 focus:border-lime-500 transition-all"
            placeholder="Ваше имя"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            type="email" 
            v-model="registerEmail"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-lime-500 focus:border-lime-500 transition-all"
            placeholder="Ваш email"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
          <input 
            type="password" 
            v-model="registerPassword"
            class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-lime-500 focus:border-lime-500 transition-all"
            placeholder="Придумайте пароль"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <button
          @click="handleRegister"
          :disabled="isLoading"
          class="w-full mt-4 py-3 bg-lime-500 text-white rounded-xl transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-lime-600 active:bg-lime-700 active:scale-95 disabled:opacity-70"
        >
          <span v-if="!isLoading">Зарегистрироваться</span>
          <span v-else>Регистрация...</span>
        </button>
        
        <p class="text-center text-sm text-gray-500 mt-2">
          Уже есть аккаунт? 
          <button 
            @click="showLoginForm = true"
            class="text-lime-600 hover:text-lime-800 font-medium transition-colors"
          >
            Войти
          </button>
        </p>
      </div>
    </div>
    
    <div v-else class="text-center">
      <div class="mb-6">
        <div class="w-20 h-20 bg-lime-100 rounded-full mx-auto flex items-center justify-center mb-4">
          <span class="text-2xl font-bold text-lime-600">{{ userInitials }}</span>
        </div>
        <h3 class="text-xl font-semibold">{{ userData.fullName }}</h3>
        <p class="text-gray-500">{{ userData.email }}</p>
      </div>
      
      <!-- Табы для истории заказов и предложений -->
      <div class="mb-6 flex justify-center gap-2 border-b border-gray-200">
        <button
          @click="activeTab = 'orders'"
          :class="[
            'px-6 py-2 font-medium transition-colors',
            activeTab === 'orders' 
              ? 'text-lime-600 border-b-2 border-lime-600' 
              : 'text-gray-500 hover:text-gray-700'
          ]"
        >
          Заказы ({{ userOrders.length }})
        </button>
        <button
          @click="activeTab = 'suggestions'"
          :class="[
            'px-6 py-2 font-medium transition-colors',
            activeTab === 'suggestions' 
              ? 'text-lime-600 border-b-2 border-lime-600' 
              : 'text-gray-500 hover:text-gray-700'
          ]"
        >
          Предложения ({{ userSuggestions.length }})
        </button>
      </div>
      
      <!-- История заказов -->
      <div v-if="activeTab === 'orders'" class="text-left mb-6">
        <div v-if="isLoadingOrders" class="text-center py-8">
          <p class="text-gray-500">Загрузка заказов...</p>
        </div>
        <div v-else-if="userOrders.length === 0" class="text-center py-8">
          <p class="text-gray-500">У вас пока нет оформленных заказов</p>
        </div>
        <div v-else class="space-y-4 max-h-96 overflow-y-auto">
          <div 
            v-for="order in userOrders" 
            :key="order.id"
            class="bg-gray-50 rounded-lg p-4 border border-gray-200"
          >
            <div class="flex justify-between items-start mb-3">
              <div>
                <h4 class="font-semibold text-lg">Заказ #{{ order.id }}</h4>
                <p class="text-sm text-gray-500">{{ formatDate(order.createdAt) }}</p>
              </div>
              <span :class="['px-3 py-1 rounded-full text-xs font-medium', getStatusColor(order.status)]">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            
            <div class="mb-3">
              <p class="text-sm font-medium text-gray-700 mb-2">Товары:</p>
              <div class="space-y-2">
                <div 
                  v-for="item in order.items" 
                  :key="item.id"
                  class="flex justify-between items-center bg-white rounded p-2"
                >
                  <div class="flex-1">
                    <p class="font-medium text-sm">{{ item.product?.title || 'Товар' }}</p>
                    <p class="text-xs text-gray-500">Количество: {{ item.quantity }} × {{ formatPrice(item.price) }}</p>
                  </div>
                  <p class="font-semibold text-sm">{{ formatPrice(item.price * item.quantity) }}</p>
                </div>
              </div>
            </div>
            
            <div class="flex justify-between items-center pt-3 border-t border-gray-200">
              <span class="text-sm text-gray-600">Итого:</span>
              <span class="text-xl font-bold text-lime-600">{{ formatPrice(order.totalPrice) }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- История предложений -->
      <div v-if="activeTab === 'suggestions'" class="text-left mb-6">
        <div v-if="isLoadingSuggestions" class="text-center py-8">
          <p class="text-gray-500">Загрузка предложений...</p>
        </div>
        <div v-else-if="userSuggestions.length === 0" class="text-center py-8">
          <p class="text-gray-500">У вас пока нет отправленных предложений</p>
        </div>
        <div v-else class="space-y-4 max-h-96 overflow-y-auto">
          <div 
            v-for="suggestion in userSuggestions" 
            :key="suggestion.id"
            class="bg-gray-50 rounded-lg p-4 border border-gray-200"
          >
            <div class="flex justify-between items-start mb-2">
              <div class="flex-1">
                <h4 class="font-semibold text-lg text-lime-600">{{ suggestion.productName }}</h4>
                <p class="text-sm text-gray-500">{{ formatDate(suggestion.createdAt) }}</p>
              </div>
            </div>
            <div class="mb-2">
              <p class="text-sm font-medium text-gray-700 mb-1">Автор:</p>
              <p class="text-sm text-gray-600">{{ suggestion.author }}</p>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-700 mb-1">Описание:</p>
              <p class="text-sm text-gray-600 whitespace-pre-wrap">{{ suggestion.description }}</p>
            </div>
          </div>
        </div>
      </div>
      
      <button
        @click="handleLogout"
        class="px-6 py-2 bg-red-500 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-red-600 active:bg-red-700 active:scale-95"
      >
        Выйти
      </button>
    </div>
  </div>
</template>
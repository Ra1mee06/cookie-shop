<script setup>
import { ref, computed, onMounted, inject } from 'vue';
import { useRouter } from 'vue-router';
import { useApi } from '../composables/useApi';
import { useFavorites } from '../composables/useFavorites';

const router = useRouter();
const { auth: authApi, orders: ordersApi, suggestions: suggestionsApi } = useApi();

// Получаем функции для работы с избранным
const favoritesState = inject('favorites');
let syncLocalFavoritesToServer = null;
let clearLocalOnLogout = null;

if (favoritesState) {
  syncLocalFavoritesToServer = favoritesState.syncLocalFavoritesToServer;
  clearLocalOnLogout = favoritesState.clearLocalOnLogout;
} else {
  // Если inject не работает, используем напрямую useFavorites
  const { syncLocalFavoritesToServer: syncFn, clearLocalOnLogout: clearFn } = useFavorites();
  syncLocalFavoritesToServer = syncFn;
  clearLocalOnLogout = clearFn;
}

const isAuthenticated = ref(false);
const showLoginForm = ref(true);
const isLoading = ref(false);
const errorMessage = ref('');

const loginEmail = ref('');
const loginPassword = ref('');
const registerName = ref('');
const registerEmail = ref('');
const registerPassword = ref('');
const adminInviteCode = ref('');

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

const showEditForm = ref(false);
const isSaving = ref(false);
const isSavingPassword = ref(false);
const editError = ref('');
const editSuccess = ref('');

const editForm = ref({
  email: '',
  fullName: '',
  username: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const selectedAvatar = ref('');
const avatarFile = ref(null);
const avatarPreview = ref('');
const showAvatarModal = ref(false);
const isUploadingAvatar = ref(false);
const showAdminModal = ref(false);
const cookieAvatars = [
  '/cookies/cookie1.png',
  '/cookies/cookie2.png',
  '/cookies/cookie3.png',
  '/cookies/cookie4.png',
  '/cookies/cookie5.png',
  '/cookies/cookie6.png',
  '/cookies/cookie7.png',
  '/cookies/cookie8.png'
];

const isAdmin = computed(() => {
  return userData.value?.role === 'ADMIN';
});

const openAdminModal = () => {
  showAdminModal.value = true;
};

const closeAdminModal = () => {
  showAdminModal.value = false;
};

const navigateToAdmin = (path) => {
  closeAdminModal();
  console.log('Navigating to admin path:', path);
  router.push(path).catch(err => {
    console.error('Navigation error:', err);
    if (err.name !== 'NavigationDuplicated') {
      // Если это не дублирование навигации, попробуем еще раз
      setTimeout(() => {
        router.push(path);
      }, 100);
    }
  });
};

const openEditForm = () => {
  editForm.value = {
    email: userData.value.email || '',
    fullName: userData.value.fullName || '',
    username: userData.value.username || '',
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
  editError.value = '';
  editSuccess.value = '';
  showEditForm.value = true;
};

const closeEditForm = () => {
  showEditForm.value = false;
  editError.value = '';
  editSuccess.value = '';
};

const handleUpdateProfile = async () => {
  editError.value = '';
  editSuccess.value = '';
  
  if (!editForm.value.email || !isValidEmail(editForm.value.email)) {
    editError.value = 'Введите корректный email';
    return;
  }
  
  if (!editForm.value.fullName || !editForm.value.fullName.trim()) {
    editError.value = 'Введите имя';
    return;
  }
  
  if (!editForm.value.username || !editForm.value.username.trim()) {
    editError.value = 'Введите логин';
    return;
  }
  
  isSaving.value = true;
  
  try {
    const response = await authApi.updateProfile({
      email: editForm.value.email.toLowerCase().trim(),
      fullName: editForm.value.fullName.trim(),
      username: editForm.value.username.trim()
    });
    
    if (response.data.success) {
      userData.value = {
        ...userData.value,
        ...response.data.user
      };
      localStorage.setItem('user', JSON.stringify(userData.value));
      editSuccess.value = 'Профиль успешно обновлён!';
      setTimeout(() => {
        closeEditForm();
      }, 1500);
    } else {
      editError.value = response.data.message || 'Ошибка обновления профиля';
    }
  } catch (error) {
    editError.value = error.response?.data?.message || 'Ошибка обновления профиля';
    console.error('Update profile error:', error);
  } finally {
    isSaving.value = false;
  }
};

const handleUpdatePassword = async () => {
  editError.value = '';
  editSuccess.value = '';
  
  if (!editForm.value.oldPassword) {
    editError.value = 'Введите текущий пароль';
    return;
  }
  
  if (!editForm.value.newPassword || editForm.value.newPassword.length < 6) {
    editError.value = 'Новый пароль должен содержать минимум 6 символов';
    return;
  }
  
  if (editForm.value.newPassword !== editForm.value.confirmPassword) {
    editError.value = 'Пароли не совпадают';
    return;
  }
  
  isSavingPassword.value = true;
  
  try {
    const response = await authApi.updatePassword({
      oldPassword: editForm.value.oldPassword,
      newPassword: editForm.value.newPassword,
      confirmPassword: editForm.value.confirmPassword
    });
    
    if (response.data.success) {
      editSuccess.value = 'Пароль успешно изменён!';
      editForm.value.oldPassword = '';
      editForm.value.newPassword = '';
      editForm.value.confirmPassword = '';
      setTimeout(() => {
        editSuccess.value = '';
      }, 3000);
    } else {
      editError.value = response.data.message || 'Ошибка изменения пароля';
    }
  } catch (error) {
    editError.value = error.response?.data?.message || 'Ошибка изменения пароля';
    console.error('Update password error:', error);
  } finally {
    isSavingPassword.value = false;
  }
};

const openAvatarModal = () => {
  selectedAvatar.value = userData.value.avatarUrl || '';
  avatarFile.value = null;
  avatarPreview.value = '';
  showAvatarModal.value = true;
};

const closeAvatarModal = () => {
  showAvatarModal.value = false;
  selectedAvatar.value = '';
  avatarFile.value = null;
  avatarPreview.value = '';
};

const selectCookieAvatar = (avatarUrl) => {
  selectedAvatar.value = avatarUrl;
  avatarFile.value = null;
  avatarPreview.value = '';
};

const handleFileSelect = (event) => {
  const file = event.target.files[0];
  if (file) {
    if (file.size > 5 * 1024 * 1024) {
      alert('Файл слишком большой. Максимальный размер: 5 МБ');
      return;
    }
    if (!file.type.startsWith('image/')) {
      alert('Выберите изображение');
      return;
    }
    avatarFile.value = file;
    selectedAvatar.value = '';
    const reader = new FileReader();
    reader.onload = (e) => {
      avatarPreview.value = e.target.result;
    };
    reader.readAsDataURL(file);
  }
};

const handleAvatarUpload = async () => {
  if (!selectedAvatar.value && !avatarFile.value) {
    alert('Выберите аватар или загрузите изображение');
    return;
  }
  
  isUploadingAvatar.value = true;
  
  try {
    const response = await authApi.updateAvatar(selectedAvatar.value || null, avatarFile.value || null);
    
    if (response.data.success) {
      userData.value = {
        ...userData.value,
        ...response.data.user
      };
      localStorage.setItem('user', JSON.stringify(userData.value));
      closeAvatarModal();
    } else {
      alert(response.data.message || 'Ошибка загрузки аватара');
    }
  } catch (error) {
    alert(error.response?.data?.message || 'Ошибка загрузки аватара');
    console.error('Avatar upload error:', error);
  } finally {
    isUploadingAvatar.value = false;
  }
};

const userInitials = computed(() => {
  if (!userData.value.fullName) return '?';
  return userData.value.fullName
    .split(' ')
    .map(n => n[0])
    .join('')
    .toUpperCase();
});

const avatarUrl = computed(() => {
  if (userData.value.avatarUrl) {
    if (userData.value.avatarUrl.startsWith('/uploads/')) {
      return `http://localhost:8080${userData.value.avatarUrl}`;
    }
    return userData.value.avatarUrl;
  }
  return null;
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
  if (!price) return '0 бун';
  const numPrice = typeof price === 'number' ? price : parseFloat(price);
  return `${Math.round(numPrice)} бун`;
};

const getPaymentMethodText = (method) => {
  const methodMap = {
    'CASH': 'Наличными',
    'CARD_ONLINE': 'Картой сразу',
    'CARD_ON_DELIVERY': 'Картой при получении'
  };
  return methodMap[method] || method;
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
      password: registerPassword.value,
      adminInviteCode: adminInviteCode.value || undefined
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
    adminInviteCode.value = '';
    
    // Синхронизируем локальное избранное с сервером после регистрации
    if (syncLocalFavoritesToServer) {
      try {
        await syncLocalFavoritesToServer();
      } catch (err) {
        console.error('Ошибка при синхронизации избранного:', err);
      }
    }
    
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
    
    // Синхронизируем локальное избранное с сервером после входа
    if (syncLocalFavoritesToServer) {
      try {
        await syncLocalFavoritesToServer();
      } catch (err) {
        console.error('Ошибка при синхронизации избранного:', err);
      }
    }
    
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
  // Очищаем локальное избранное при выходе (серверное остается)
  if (clearLocalOnLogout) {
    clearLocalOnLogout();
  }
  
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
        
        // Синхронизируем локальное избранное с сервером при восстановлении сессии
        if (syncLocalFavoritesToServer) {
          try {
            await syncLocalFavoritesToServer();
          } catch (err) {
            console.error('Ошибка при синхронизации избранного:', err);
          }
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
  <div class="max-w-2xl mx-auto p-8">
    <h2 class="text-3xl md:text-4xl font-extrabold mb-8 text-center bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
      Профиль
    </h2>
    
    <div v-if="errorMessage" class="mb-4 p-3 bg-red-100 text-red-700 rounded-lg">
      {{ errorMessage }}
    </div>
    
    <div v-if="!isAuthenticated">
      <div class="space-y-4" v-if="!isAuthenticated && showLoginForm">
        <h3 class="text-xl font-semibold text-center">Вход в аккаунт</h3>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            type="email" 
            v-model="loginEmail"
            class="input-field"
            placeholder="Ваш email"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
          <input 
            type="password" 
            v-model="loginPassword"
            class="input-field"
            placeholder="Ваш пароль"
            @keyup.enter="handleLogin"
          >
        </div>
        
        <button
          @click="handleLogin"
          :disabled="isLoading"
          class="btn-primary w-full mt-4"
        >
          <span v-if="!isLoading">Войти</span>
          <span v-else>Вход...</span>
        </button>
        
        <p class="text-center text-sm text-gray-500 mt-2">
          Нет аккаунта? 
          <button 
            @click="showLoginForm = false"
            class="text-cookie-600 hover:text-cookie-800 font-medium transition-colors"
          >
            Зарегистрироваться
          </button>
        </p>
      </div>

      <div class="space-y-4" v-if="!isAuthenticated && !showLoginForm">
        <h3 class="text-xl font-semibold text-center">Регистрация</h3>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Имя</label>
          <input 
            type="text" 
            v-model="registerName"
            class="input-field"
            placeholder="Ваше имя"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
          <input 
            type="email" 
            v-model="registerEmail"
            class="input-field"
            placeholder="Ваш email"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Пароль</label>
          <input 
            type="password" 
            v-model="registerPassword"
            class="input-field"
            placeholder="Придумайте пароль"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Секретный код администратора (необязательно)</label>
          <input 
            type="text" 
            v-model="adminInviteCode"
            class="input-field"
            placeholder="Вставьте код, если он у вас есть"
            @keyup.enter="handleRegister"
          >
        </div>
        
        <button
          @click="handleRegister"
          :disabled="isLoading"
          class="btn-primary w-full mt-4"
        >
          <span v-if="!isLoading">Зарегистрироваться</span>
          <span v-else>Регистрация...</span>
        </button>
        
        <p class="text-center text-sm text-gray-500 mt-2">
          Уже есть аккаунт? 
          <button 
            @click="showLoginForm = true"
            class="text-cookie-600 hover:text-cookie-800 font-medium transition-colors"
          >
            Войти
          </button>
        </p>
      </div>
    </div>
    
    <div v-else class="text-center">
      <div class="mb-6">
        <div class="relative inline-block mb-4">
          <div 
            class="w-24 h-24 rounded-full mx-auto flex items-center justify-center overflow-hidden border-4 border-cookie-200 shadow-lg"
            :class="avatarUrl ? 'bg-white' : 'bg-gradient-to-br from-cookie-100 to-beige-200'"
          >
            <img 
              v-if="avatarUrl" 
              :src="avatarUrl" 
              :alt="userData.fullName"
              class="w-full h-full object-cover"
            >
            <span v-else class="text-3xl font-bold text-cookie-600">{{ userInitials }}</span>
          </div>
          <button
            @click="openAvatarModal"
            class="absolute bottom-0 right-0 w-8 h-8 bg-gradient-to-r from-cookie-500 to-cookie-600 rounded-full flex items-center justify-center text-white shadow-lg hover:from-cookie-600 hover:to-cookie-700 transition-all transform hover:scale-110"
            title="Изменить аватар"
          >
            <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z" />
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
          </button>
        </div>
        <h3 class="text-2xl font-bold text-gray-800 mb-1">{{ userData.fullName }}</h3>
        <p class="text-gray-600 mb-1">{{ userData.email }}</p>
        <p v-if="userData.username" class="text-sm text-gray-500">@{{ userData.username }}</p>
        <button
          @click="openEditForm"
          class="btn-primary mt-4 flex items-center gap-2 mx-auto"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          Редактировать профиль
        </button>
        <button
          v-if="isAdmin"
          @click="openAdminModal"
          class="mt-3 px-6 py-2 bg-brown-700 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-brown-800 active:bg-brown-900 active:scale-95 flex items-center gap-2 mx-auto"
        >
          <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.607 2.296.07 2.572-1.065z" />
          </svg>
          Панель управления
        </button>
      </div>
      
      <!-- Табы для истории заказов и предложений -->
      <div class="mb-6 flex justify-center gap-2 border-b border-gray-200">
        <button
          @click="activeTab = 'orders'"
          :class="[
            'px-6 py-2 font-medium transition-colors',
            activeTab === 'orders' 
              ? 'text-cookie-600 border-b-2 border-cookie-600' 
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
              ? 'text-cookie-600 border-b-2 border-cookie-600' 
              : 'text-gray-500 hover:text-gray-700'
          ]"
        >
          Предложения ({{ userSuggestions.length }})
        </button>
      </div>
      
      <!-- История заказов -->
      <div v-if="activeTab === 'orders'" class="text-left mb-6">
        <div v-if="isLoadingOrders" class="text-center py-8">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-cookie-600"></div>
          <p class="text-gray-500 mt-2">Загрузка заказов...</p>
        </div>
        <div v-else-if="userOrders.length === 0" class="text-center py-8">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
          </svg>
          <p class="text-gray-500 mt-2">У вас пока нет оформленных заказов</p>
        </div>
        <div v-else class="space-y-4 max-h-[500px] overflow-y-auto pr-2">
          <div 
            v-for="order in userOrders" 
            :key="order.id"
            class="bg-gradient-to-br from-white to-gray-50 rounded-xl p-5 border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-300"
          >
            <div class="flex justify-between items-start mb-4 pb-3 border-b border-gray-200">
              <div>
                <div class="flex items-center gap-2 mb-1">
                  <svg class="h-5 w-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                  <h4 class="font-bold text-lg text-gray-800">Заказ #{{ order.id }}</h4>
                </div>
                <p class="text-sm text-gray-500 flex items-center gap-1">
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  {{ formatDate(order.createdAt) }}
                </p>
              </div>
              <span :class="['px-4 py-1.5 rounded-full text-xs font-semibold shadow-sm', getStatusColor(order.status)]">
                {{ getStatusText(order.status) }}
              </span>
            </div>
            
            <!-- Информация о доставке и получателе -->
            <div class="mb-4 grid grid-cols-1 md:grid-cols-2 gap-3">
              <div v-if="order.recipient" class="bg-blue-50 rounded-lg p-3 border border-blue-100">
                <p class="text-xs font-semibold text-blue-600 uppercase mb-1 flex items-center gap-1">
                  <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                  </svg>
                  Получатель
                </p>
                <p class="text-sm font-medium text-gray-800">{{ order.recipient }}</p>
              </div>
              <div v-if="order.address" class="bg-purple-50 rounded-lg p-3 border border-purple-100">
                <p class="text-xs font-semibold text-purple-600 uppercase mb-1 flex items-center gap-1">
                  <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  Адрес доставки
                </p>
                <p class="text-sm font-medium text-gray-800">{{ order.address }}</p>
              </div>
              <div v-if="order.paymentMethod" class="bg-green-50 rounded-lg p-3 border border-green-100">
                <p class="text-xs font-semibold text-green-600 uppercase mb-1 flex items-center gap-1">
                  <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" />
                  </svg>
                  Способ оплаты
                </p>
                <p class="text-sm font-medium text-gray-800">{{ getPaymentMethodText(order.paymentMethod) }}</p>
              </div>
              <div v-if="order.promoCode" class="bg-yellow-50 rounded-lg p-3 border border-yellow-100">
                <p class="text-xs font-semibold text-yellow-600 uppercase mb-1 flex items-center gap-1">
                  <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Промокод
                </p>
                <p class="text-sm font-medium text-gray-800">{{ order.promoCode }}</p>
              </div>
            </div>

            <!-- Комментарий к заказу -->
            <div v-if="order.comment" class="mb-4 bg-gray-50 rounded-lg p-3 border border-gray-200">
              <p class="text-xs font-semibold text-gray-500 uppercase mb-2 flex items-center gap-2">
                <svg class="h-3 w-3" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 8h10M7 12h4m1 8l-4-4H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-3l-4 4z" />
                </svg>
                Комментарий к заказу
              </p>
              <p class="text-sm text-gray-700 whitespace-pre-wrap">{{ order.comment }}</p>
            </div>

            <!-- Товары -->
            <div class="mb-4">
              <p class="text-sm font-semibold text-gray-700 mb-3 flex items-center gap-2">
                <svg class="h-4 w-4 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
                </svg>
                Товары:
              </p>
              <div class="space-y-2">
                <div 
                  v-for="item in order.items" 
                  :key="item.id"
                  class="flex justify-between items-center bg-white rounded-lg p-3 border border-gray-100 hover:border-cookie-300 transition-colors"
                >
                  <div class="flex items-center gap-3 flex-1">
                    <div v-if="item.product?.imageUrl" class="w-12 h-12 rounded-lg overflow-hidden flex-shrink-0">
                      <img :src="item.product.imageUrl" :alt="item.product.title" class="w-full h-full object-cover">
                    </div>
                    <div class="flex-1 min-w-0">
                      <p class="font-semibold text-sm text-gray-800 truncate">{{ item.product?.title || 'Товар' }}</p>
                      <p class="text-xs text-gray-500">Количество: {{ item.quantity }} × {{ formatPrice(item.price / item.quantity) }}</p>
                    </div>
                  </div>
                  <p class="font-bold text-sm text-cookie-600 ml-4">{{ formatPrice(item.price) }}</p>
                </div>
              </div>
            </div>
            
            <!-- Итоговая информация -->
            <div class="bg-cookie-50 rounded-lg p-4 border-2 border-cookie-100 space-y-2">
              <div v-if="order.discount && parseFloat(order.discount) > 0" class="flex justify-between items-center text-sm">
                <span class="text-gray-700 flex items-center gap-2">
                  <svg class="h-4 w-4 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Скидка:
                </span>
                <span class="font-semibold text-green-600">-{{ formatPrice(order.discount) }}</span>
              </div>
              <div v-if="order.tip && parseFloat(order.tip) > 0" class="flex justify-between items-center text-sm">
                <span class="text-gray-700 flex items-center gap-2">
                  <svg class="h-4 w-4 text-blue-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 3v4M3 5h4M6 17v4m-2-2h4m5-16l2.286 6.857L21 12l-5.714 2.143L13 21l-2.286-6.857L5 12l5.714-2.143L13 3z" />
                  </svg>
                  Чаевые:
                </span>
                <span class="font-semibold text-blue-600">{{ formatPrice(order.tip) }}</span>
              </div>
              <div class="flex justify-between items-center pt-2 border-t-2 border-cookie-200">
                <span class="text-sm font-semibold text-gray-700 flex items-center gap-2">
                  <svg class="h-5 w-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Итого:
                </span>
                <span class="text-2xl font-bold text-cookie-600">{{ formatPrice(order.totalPrice) }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- История предложений -->
      <div v-if="activeTab === 'suggestions'" class="text-left mb-6">
        <div v-if="isLoadingSuggestions" class="text-center py-8">
          <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-cookie-600"></div>
          <p class="text-gray-500 mt-2">Загрузка предложений...</p>
        </div>
        <div v-else-if="userSuggestions.length === 0" class="text-center py-8">
          <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          <p class="text-gray-500 mt-2">У вас пока нет отправленных предложений</p>
        </div>
        <div v-else class="space-y-4 max-h-[500px] overflow-y-auto pr-2">
          <div 
            v-for="suggestion in userSuggestions" 
            :key="suggestion.id"
            class="bg-gradient-to-br from-white to-lime-50 rounded-xl p-5 border border-gray-200 shadow-sm hover:shadow-md transition-shadow duration-300"
          >
            <div class="flex justify-between items-start mb-4 pb-3 border-b border-cookie-200">
              <div class="flex-1">
                <div class="flex items-center gap-2 mb-2">
                  <svg class="h-5 w-5 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                  <h4 class="font-bold text-lg text-cookie-700">{{ suggestion.productName }}</h4>
                </div>
                <p class="text-sm text-gray-500 flex items-center gap-1">
                  <svg class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  {{ formatDate(suggestion.createdAt) }}
                </p>
              </div>
            </div>
            <div class="bg-white rounded-lg p-4 mb-3 border border-cookie-100">
              <p class="text-xs font-semibold text-gray-500 uppercase mb-1 flex items-center gap-2">
                <svg class="h-3 w-3 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                Автор
              </p>
              <p class="text-sm font-medium text-gray-700">{{ suggestion.author }}</p>
            </div>
            <div class="bg-white rounded-lg p-4 border border-cookie-100">
              <p class="text-xs font-semibold text-gray-500 uppercase mb-2 flex items-center gap-2">
                <svg class="h-3 w-3 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h7" />
                </svg>
                Описание
              </p>
              <p class="text-sm text-gray-700 whitespace-pre-wrap leading-relaxed">{{ suggestion.description }}</p>
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
    
    <!-- Модальное окно редактирования профиля -->
    <div 
      v-if="showEditForm" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeEditForm"
    >
      <div class="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 class="text-2xl font-bold text-gray-800">Редактирование профиля</h3>
          <button
            @click="closeEditForm"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="p-6 space-y-6">
          <div v-if="editError" class="p-3 bg-red-100 text-red-700 rounded-lg">
            {{ editError }}
          </div>
          <div v-if="editSuccess" class="p-3 bg-green-100 text-green-700 rounded-lg">
            {{ editSuccess }}
          </div>
          
          <!-- Основная информация -->
          <div>
            <h4 class="text-lg font-semibold text-gray-800 mb-4">Основная информация</h4>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Имя</label>
                <input 
                  type="text" 
                  v-model="editForm.fullName"
                  class="input-field"
                  placeholder="Ваше имя"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                <input 
                  type="email" 
                  v-model="editForm.email"
                  class="input-field"
                  placeholder="Ваш email"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Логин</label>
                <input 
                  type="text" 
                  v-model="editForm.username"
                  class="input-field"
                  placeholder="Ваш логин"
                >
              </div>
              
              <button
                @click="handleUpdateProfile"
                :disabled="isSaving"
                class="w-full py-3 bg-cookie-500 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-cookie-600 active:bg-cookie-700 active:scale-95 disabled:opacity-70"
              >
                <span v-if="!isSaving">Сохранить изменения</span>
                <span v-else>Сохранение...</span>
              </button>
            </div>
          </div>
          
          <!-- Смена пароля -->
          <div class="border-t border-gray-200 pt-6">
            <h4 class="text-lg font-semibold text-gray-800 mb-4">Смена пароля</h4>
            <div class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Текущий пароль</label>
                <input 
                  type="password" 
                  v-model="editForm.oldPassword"
                  class="input-field"
                  placeholder="Введите текущий пароль"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Новый пароль</label>
                <input 
                  type="password" 
                  v-model="editForm.newPassword"
                  class="input-field"
                  placeholder="Придумайте новый пароль"
                >
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Подтвердите пароль</label>
                <input 
                  type="password" 
                  v-model="editForm.confirmPassword"
                  class="input-field"
                  placeholder="Подтвердите новый пароль"
                >
              </div>
              
              <button
                @click="handleUpdatePassword"
                :disabled="isSavingPassword"
                class="w-full py-3 bg-blue-500 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-blue-600 active:bg-blue-700 active:scale-95 disabled:opacity-70"
              >
                <span v-if="!isSavingPassword">Изменить пароль</span>
                <span v-else>Изменение...</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Модальное окно выбора аватара -->
    <div 
      v-if="showAvatarModal" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeAvatarModal"
    >
      <div class="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 class="text-2xl font-bold text-gray-800">Выбор аватара</h3>
          <button
            @click="closeAvatarModal"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="p-6 space-y-6">
          <!-- Предпросмотр -->
          <div class="text-center">
            <div class="inline-block">
              <div 
                class="w-32 h-32 rounded-full mx-auto flex items-center justify-center overflow-hidden border-4 border-cookie-200 shadow-lg"
                :class="avatarPreview || selectedAvatar ? 'bg-white' : 'bg-gradient-to-br from-cookie-100 to-lime-200'"
              >
                <img 
                  v-if="avatarPreview" 
                  :src="avatarPreview" 
                  alt="Preview"
                  class="w-full h-full object-cover"
                >
                <img 
                  v-else-if="selectedAvatar" 
                  :src="selectedAvatar" 
                  alt="Selected"
                  class="w-full h-full object-cover"
                >
                <span v-else class="text-4xl font-bold text-cookie-600">{{ userInitials }}</span>
              </div>
            </div>
          </div>
          
          <!-- Загрузка своего изображения -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Загрузить своё изображение</label>
            <div class="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-cookie-500 transition-colors">
              <input
                type="file"
                accept="image/*"
                @change="handleFileSelect"
                class="hidden"
                id="avatar-upload"
              >
              <label
                for="avatar-upload"
                class="cursor-pointer flex flex-col items-center gap-2"
              >
                <svg class="w-12 h-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                <span class="text-sm text-gray-600">Нажмите, чтобы выбрать файл</span>
                <span class="text-xs text-gray-400">Макс. размер: 5 МБ</span>
              </label>
            </div>
          </div>
          
          <!-- Выбор из печенек -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Или выберите печеньку</label>
            <div class="grid grid-cols-4 gap-4">
              <button
                v-for="(cookie, index) in cookieAvatars"
                :key="index"
                @click="selectCookieAvatar(cookie)"
                :class="[
                  'w-full aspect-square rounded-lg overflow-hidden border-4 transition-all transform hover:scale-105',
                  selectedAvatar === cookie 
                    ? 'border-cookie-500 shadow-lg ring-2 ring-lime-300' 
                    : 'border-gray-200 hover:border-cookie-300'
                ]"
              >
                <img :src="cookie" :alt="`Cookie ${index + 1}`" class="w-full h-full object-cover">
              </button>
            </div>
          </div>
          
          <!-- Кнопки действий -->
          <div class="flex gap-4">
            <button
              @click="closeAvatarModal"
              class="flex-1 py-3 bg-gray-200 text-gray-700 rounded-lg transition-all hover:bg-gray-300"
            >
              Отмена
            </button>
            <button
              @click="handleAvatarUpload"
              :disabled="isUploadingAvatar || (!selectedAvatar && !avatarFile)"
              class="flex-1 py-3 bg-cookie-500 text-white rounded-lg transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg hover:bg-cookie-600 active:bg-cookie-700 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!isUploadingAvatar">Сохранить аватар</span>
              <span v-else>Загрузка...</span>
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Модальное окно меню администратора -->
    <div 
      v-if="showAdminModal" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click.self="closeAdminModal"
    >
      <div class="bg-white rounded-xl shadow-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center">
          <h3 class="text-2xl font-bold text-gray-800">Панель управления администратора</h3>
          <button
            @click="closeAdminModal"
            class="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg class="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
        
        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <!-- Пользователи -->
            <button
              @click="navigateToAdmin('/admin/users')"
              class="p-6 bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl border-2 border-blue-200 hover:border-blue-400 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg group text-left"
            >
              <div class="flex items-center gap-4 mb-3">
                <div class="w-12 h-12 bg-blue-500 rounded-lg flex items-center justify-center group-hover:bg-blue-600 transition-colors">
                  <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                  </svg>
                </div>
                <h4 class="text-xl font-bold text-gray-800">Пользователи</h4>
              </div>
              <p class="text-sm text-gray-600">Управление всеми зарегистрированными пользователями. Редактирование данных и удаление аккаунтов.</p>
            </button>

            <!-- Заказы -->
            <button
              @click="navigateToAdmin('/admin/orders')"
              class="p-6 bg-gradient-to-br from-green-50 to-green-100 rounded-xl border-2 border-green-200 hover:border-green-400 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg group text-left"
            >
              <div class="flex items-center gap-4 mb-3">
                <div class="w-12 h-12 bg-green-500 rounded-lg flex items-center justify-center group-hover:bg-green-600 transition-colors">
                  <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <h4 class="text-xl font-bold text-gray-800">Заказы</h4>
              </div>
              <p class="text-sm text-gray-600">Просмотр всех заказов и их содержимого. Управление статусами заказов.</p>
            </button>

            <!-- Предложения -->
            <button
              @click="navigateToAdmin('/admin/suggestions')"
              class="p-6 bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl border-2 border-purple-200 hover:border-purple-400 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg group text-left"
            >
              <div class="flex items-center gap-4 mb-3">
                <div class="w-12 h-12 bg-purple-500 rounded-lg flex items-center justify-center group-hover:bg-purple-600 transition-colors">
                  <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                  </svg>
                </div>
                <h4 class="text-xl font-bold text-gray-800">Предложения</h4>
              </div>
              <p class="text-sm text-gray-600">История всех предложений от пользователей. Просмотр и управление предложениями.</p>
            </button>

            <!-- Инвайт-коды -->
            <button
              @click="navigateToAdmin('/admin/invites')"
              class="p-6 bg-gradient-to-br from-orange-50 to-orange-100 rounded-xl border-2 border-orange-200 hover:border-orange-400 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg group text-left"
            >
              <div class="flex items-center gap-4 mb-3">
                <div class="w-12 h-12 bg-orange-500 rounded-lg flex items-center justify-center group-hover:bg-orange-600 transition-colors">
                  <svg class="w-6 h-6 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                  </svg>
                </div>
                <h4 class="text-xl font-bold text-gray-800">Инвайт-коды</h4>
              </div>
              <p class="text-sm text-gray-600">Создание секретных кодов для регистрации новых администраторов.</p>
            </button>
          </div>

          <!-- Дополнительные ссылки -->
          <div class="mt-6 pt-6 border-t border-gray-200">
            <button
              @click="navigateToAdmin('/admin/promocodes')"
              class="w-full p-4 bg-gray-50 rounded-lg hover:bg-gray-100 transition-colors text-left"
            >
              <div class="flex items-center gap-3">
                <svg class="w-5 h-5 text-gray-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span class="font-medium text-gray-700">Промокоды</span>
              </div>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
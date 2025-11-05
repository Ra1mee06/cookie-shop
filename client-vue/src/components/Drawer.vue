<script setup>
import { useApi } from '../composables/useApi'
import { ref, computed, inject } from 'vue'
import { useRouter } from 'vue-router'
import CartItemList from './CartItemList.vue'
import InfoBlock from './InfoBlock.vue'
import DrawerHead from './DrawerHead.vue'
import OrderModal from './OrderModal.vue'

const { orders: ordersApi } = useApi();
const router = useRouter();

const props = defineProps({
  totalPrice: Number,
  vatPrice: Number
})

const { cart, closeDrawer } = inject('cart')

const isCreating = ref(false)
const orderId = ref(null)
const showAuthModal = ref(false)
const showOrderModal = ref(false)

const isAuthenticated = () => {
  const userId = localStorage.getItem('userId')
  const authToken = localStorage.getItem('authToken')
  return !!(userId && authToken)
}

const openOrderModal = () => {
  // Проверка авторизации
  if (!isAuthenticated()) {
    showAuthModal.value = true
    return
  }
  
  showOrderModal.value = true
}

const closeOrderModal = () => {
  showOrderModal.value = false
}

const handleOrderSubmit = async (orderFormData) => {
  try {
    isCreating.value = true
    showOrderModal.value = false
    
    const orderData = {
      items: cart.value.map(item => ({
        productId: item.id,
        quantity: item.quantity,
        price: item.price
      })),
      totalPrice: props.totalPrice,
      ...orderFormData
    }
    
    const response = await ordersApi.create(orderData)
    cart.value = []
    orderId.value = response.data.id
  } catch (err) {
    console.error("Ошибка создания заказа:", err)
    if (err.response?.status === 401 || err.response?.status === 403) {
      showAuthModal.value = true
    } else {
      alert('Ошибка при создании заказа. Попробуйте позже.')
    }
  } finally {
    isCreating.value = false
  }
}

const goToProfile = () => {
  showAuthModal.value = false
  closeDrawer()
  router.push('/profile')
}

const closeAuthModal = () => {
  showAuthModal.value = false
}

const cartIsEmpty = computed(() => cart.value.length === 0)
const buttonDisabled = computed(() => isCreating.value || cartIsEmpty.value)
</script>

<template>
  <!-- Остальная часть шаблона остается БЕЗ ИЗМЕНЕНИЙ -->
  <div class="fixed right-0 top-0 h-full z-50 flex">
    <div 
      @click="closeDrawer"
      class="fixed inset-0 bg-black bg-opacity-40 z-40"
    ></div>
    
    <div class="bg-white w-96 h-full flex flex-col z-50 relative">
      <DrawerHead />

      <div class="flex-1 overflow-y-auto px-6">
        <div v-if="!totalPrice || orderId" class="flex h-full items-center">
          <InfoBlock
            v-if="!totalPrice && !orderId"
            title="Корзина пустая"
            description="Добавьте хотя бы одну печеньку, чтобы сделать заказ."
            image-url="/package-icon.png"
          />
          <InfoBlock
            v-if="orderId"
            title="Заказ оформлен!"
            :description="`Ваш заказ #${orderId} скоро будет передан курьерской доставке`"
            image-url="/order-success-icon.png"
          />
        </div>

        <div v-else>
          <CartItemList />
        </div>
      </div>

      <div class="p-6 border-t border-gray-200">
        <div class="flex flex-col gap-4">
          <div class="flex gap-2">
            <span>Итого:</span>
            <div class="flex-1 border-b border-dashed"></div>
            <b>{{ totalPrice }} бун</b>
          </div>

          <div class="flex gap-2">
            <span>Налог 5%:</span>
            <div class="flex-1 border-b border-dashed"></div>
            <b>{{ vatPrice }} бун</b>
          </div>

          <button
            :disabled="buttonDisabled"
            @click="openOrderModal"
            class="mt-2 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg bg-lime-500 w-full rounded-xl py-3 text-white disabled:bg-slate-300 hover:bg-lime-600 active:bg-lime-700 active:scale-95 cursor-pointer"
          >
            Оформить заказ
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Модальное окно авторизации -->
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
            <p class="text-gray-600 mb-6">Для оформления заказа необходимо авторизоваться. Пожалуйста, войдите в систему.</p>
            <div class="flex gap-4 justify-center">
              <button 
                @click="closeAuthModal"
                class="px-6 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors"
              >
                Отмена
              </button>
              <button 
                @click="goToProfile"
                class="px-6 py-2 bg-lime-500 text-white rounded-lg hover:bg-lime-600 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg"
              >
                Перейти к авторизации
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
  </transition>

  <!-- Модальное окно оформления заказа -->
  <OrderModal
    :isOpen="showOrderModal"
    :totalPrice="totalPrice"
    @close="closeOrderModal"
    @submit="handleOrderSubmit"
  />
</template>
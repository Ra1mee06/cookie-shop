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

const orderError = ref('')

const handleOrderSubmit = async (orderFormData) => {
  try {
    isCreating.value = true
    orderError.value = ''
    // Не закрываем модальное окно сразу, чтобы показать ошибку, если она будет
    
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
    // Если заказ успешно создан, закрываем модальное окно и очищаем корзину
    cart.value = []
    orderId.value = response.data.id
    orderError.value = ''
    showOrderModal.value = false
  } catch (err) {
    console.error("Ошибка создания заказа:", err)
    if (err.response?.status === 401 || err.response?.status === 403) {
      showOrderModal.value = false
      showAuthModal.value = true
      orderError.value = ''
    } else {
      // Показываем конкретную ошибку от сервера, если она есть
      const errorMessage = err.response?.data?.error || err.response?.data?.message || 'Ошибка при создании заказа. Попробуйте позже.'
      // Убираем префикс "Error creating order: " если он есть, чтобы сообщение было чище
      const cleanErrorMessage = errorMessage.replace(/^Error creating order:\s*/i, '')
      orderError.value = cleanErrorMessage
      // Оставляем модальное окно открытым, чтобы пользователь мог увидеть ошибку и исправить промокод
      showOrderModal.value = true
      // Для ошибок не связанных с промокодом, показываем alert
      if (!cleanErrorMessage.includes('промокод') && !cleanErrorMessage.includes('Промокод')) {
        alert(cleanErrorMessage)
      }
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
    
    <div class="bg-white w-full md:w-[600px] h-full flex flex-col z-50 relative shadow-2xl">
      <DrawerHead />

      <div class="flex-1 overflow-y-auto px-6 py-4 bg-gradient-to-b from-white to-beige-50/30">
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

      <div class="p-6 border-t-2 border-beige-200 bg-white">
        <div class="flex flex-col gap-4">
          <div class="flex items-center gap-3 text-brown-700">
            <span class="font-medium">Итого:</span>
            <div class="flex-1 border-b-2 border-dashed border-beige-300"></div>
            <b class="text-xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
              {{ totalPrice }} BYN
            </b>
          </div>

          <div class="flex items-center gap-3 text-brown-600">
            <span class="text-sm">Налог 5%:</span>
            <div class="flex-1 border-b border-dashed border-beige-300"></div>
            <b class="text-sm font-semibold">{{ vatPrice }} BYN</b>
          </div>

          <button
            :disabled="buttonDisabled"
            @click="openOrderModal"
            class="btn-primary w-full mt-2"
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
            <p class="text-brown-600 mb-8">Для оформления заказа необходимо авторизоваться. Пожалуйста, войдите в систему.</p>
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

  <!-- Модальное окно оформления заказа -->
  <OrderModal
    :isOpen="showOrderModal"
    :totalPrice="totalPrice"
    :error="orderError"
    @close="closeOrderModal"
    @submit="handleOrderSubmit"
  />
</template>
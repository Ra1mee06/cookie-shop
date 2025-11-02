<script setup>
import { useApi } from '../composables/useApi'
import { ref, computed, inject } from 'vue'
import CartItemList from './CartItemList.vue'
import InfoBlock from './InfoBlock.vue'
import DrawerHead from './DrawerHead.vue'

const { orders: ordersApi } = useApi();

const props = defineProps({
  totalPrice: Number,
  vatPrice: Number
})

const { cart, closeDrawer } = inject('cart')

const isCreating = ref(false)
const orderId = ref(null)

const createOrder = async () => {
  try {
    isCreating.value = true
    const orderData = {
      items: cart.value.map(item => ({
        productId: item.id,
        quantity: item.quantity,
        price: item.price
      })),
      totalPrice: props.totalPrice
    }
    
    const response = await ordersApi.create(orderData)
    cart.value = []
    orderId.value = response.data.id
  } catch (err) {
    console.error("Ошибка создания заказа:", err)
    alert('Ошибка при создании заказа. Попробуйте позже.')
  } finally {
    isCreating.value = false
  }
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
            @click="createOrder"
            class="mt-2 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-lg bg-lime-500 w-full rounded-xl py-3 text-white disabled:bg-slate-300 hover:bg-lime-600 active:bg-lime-700 active:scale-95 cursor-pointer"
          >
            Оформить заказ
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
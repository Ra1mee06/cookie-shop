<template>
  <div>
    <!-- Основное модальное окно оформления заказа -->
    <transition name="modal-fade">
      <div 
        v-if="isOpen && !showMapModal" 
        key="order-modal"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
        @click.self="handleClose"
      >
        <transition name="modal-content">
          <div 
            key="order-modal-content"
            class="bg-white rounded-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto shadow-2xl border-2 border-beige-200"
            @click.stop
          >
          <div class="p-8">
            <div class="flex justify-between items-center mb-8 pb-6 border-b-2 border-beige-200">
              <h3 class="text-3xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
                Оформление заказа
              </h3>
              <button 
                @click="handleClose"
                class="p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95"
              >
                <svg class="w-6 h-6 text-brown-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>

            <form @submit.prevent="handleSubmit" class="space-y-6">
              <!-- Сообщение об ошибке -->
              <div v-if="props.error" class="p-4 bg-red-50 border-2 border-red-200 rounded-xl">
                <p class="text-red-700 font-semibold text-sm">{{ props.error }}</p>
              </div>
              
              <!-- Получатель (обязательно) -->
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">
                  Получатель <span class="text-red-500">*</span>
                </label>
                <input 
                  v-model="form.recipient"
                  type="text"
                  required
                  class="input-field"
                  placeholder="Имя получателя"
                />
                <p v-if="errors.recipient" class="text-red-500 text-sm mt-1">{{ errors.recipient }}</p>
              </div>

              <!-- Адрес (обязательно) -->
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">
                  Адрес доставки <span class="text-red-500">*</span>
                </label>
                <div class="space-y-3">
                  <input 
                    v-model="form.address"
                    type="text"
                    required
                    class="input-field"
                    placeholder="Введите адрес доставки"
                  />
                  <button
                    type="button"
                    @click="openMapModal"
                    class="btn-secondary w-full"
                  >
                    <span class="flex items-center justify-center gap-2">
                      <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
                      </svg>
                      Выбрать на карте (опционально)
                    </span>
                  </button>
                </div>
                <p v-if="errors.address" class="text-red-500 text-sm mt-1">{{ errors.address }}</p>
              </div>

              <!-- Комментарий (опционально) -->
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">
                  Комментарий к заказу <span class="text-brown-400 text-xs">(необязательно)</span>
                </label>
                <textarea 
                  v-model="form.comment"
                  class="input-field h-24 resize-none"
                  placeholder="Оставьте комментарий, если нужно..."
                ></textarea>
              </div>

              <!-- Промокод (опционально) -->
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-2">
                  Промокод <span class="text-brown-400 text-xs">(необязательно)</span>
                </label>
                <div class="flex gap-3">
                  <input 
                    v-model="form.promoCode"
                    type="text"
                    class="input-field flex-1"
                    placeholder="Введите промокод"
                  />
                  <button
                    type="button"
                    @click="applyPromoCode"
                    class="btn-secondary whitespace-nowrap"
                  >
                    Применить
                  </button>
                </div>
                <!-- Показываем сообщение о промокоде только если нет ошибки в props.error (чтобы не дублировать) -->
                <p v-if="promoMessage && !props.error" :class="promoMessage.includes('неверный') || promoMessage.includes('не найден') || promoMessage.includes('истек') || promoMessage.includes('неактивен') ? 'text-red-500' : promoMessage.includes('применен') || promoMessage.includes('будет проверен') ? 'text-green-500' : 'text-gray-500'" class="text-sm mt-1">{{ promoMessage }}</p>
              </div>

              <!-- Способ оплаты -->
              <div>
                <label class="block text-sm font-semibold text-brown-700 mb-3">
                  Способ оплаты <span class="text-red-500">*</span>
                </label>
                <div class="space-y-3">
                  <label class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all duration-300"
                    :class="form.paymentMethod === 'CASH' 
                      ? 'border-cookie-500 bg-cookie-50 shadow-md' 
                      : 'border-beige-300 hover:border-cookie-300 hover:bg-cookie-50/50'">
                    <input 
                      v-model="form.paymentMethod"
                      type="radio"
                      value="CASH"
                      required
                      class="mr-3 w-5 h-5 text-cookie-600 focus:ring-cookie-500"
                    />
                    <span class="font-medium text-brown-800">Наличными</span>
                  </label>
                  <label class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all duration-300"
                    :class="form.paymentMethod === 'CARD_ONLINE' 
                      ? 'border-cookie-500 bg-cookie-50 shadow-md' 
                      : 'border-beige-300 hover:border-cookie-300 hover:bg-cookie-50/50'">
                    <input 
                      v-model="form.paymentMethod"
                      type="radio"
                      value="CARD_ONLINE"
                      required
                      class="mr-3 w-5 h-5 text-cookie-600 focus:ring-cookie-500"
                    />
                    <span class="font-medium text-brown-800">Картой сразу</span>
                  </label>
                  <label class="flex items-center p-4 border-2 rounded-xl cursor-pointer transition-all duration-300"
                    :class="form.paymentMethod === 'CARD_ON_DELIVERY' 
                      ? 'border-cookie-500 bg-cookie-50 shadow-md' 
                      : 'border-beige-300 hover:border-cookie-300 hover:bg-cookie-50/50'">
                    <input 
                      v-model="form.paymentMethod"
                      type="radio"
                      value="CARD_ON_DELIVERY"
                      required
                      class="mr-3 w-5 h-5 text-cookie-600 focus:ring-cookie-500"
                    />
                    <span class="font-medium text-brown-800">Картой при получении</span>
                  </label>
                </div>
                <p v-if="errors.paymentMethod" class="text-red-500 text-sm mt-1">{{ errors.paymentMethod }}</p>
              </div>

              <!-- Чаевые (только для оплаты картой) -->
              <div v-if="showTipOptions">
                <label class="block text-sm font-semibold text-brown-700 mb-3">
                  Чаевые доставщику <span class="text-brown-400 text-xs">(необязательно)</span>
                </label>
                <div class="grid grid-cols-5 gap-2">
                  <button
                    v-for="tip in tipOptions"
                    :key="tip.value"
                    type="button"
                    @click="form.tip = tip.value"
                    :class="[
                      'px-4 py-2 rounded-xl border-2 font-semibold transition-all duration-300 transform hover:scale-105',
                      form.tip === tip.value 
                        ? 'bg-gradient-to-r from-cookie-500 to-cookie-600 text-white border-cookie-500 shadow-lg' 
                        : 'bg-white text-brown-700 border-beige-300 hover:border-cookie-300 hover:bg-cookie-50'
                    ]"
                  >
                    {{ tip.label }}
                  </button>
                </div>
                <p v-if="form.tip > 0" class="text-sm text-gray-600 mt-2">
                  Чаевые: {{ calculateTip }} BYN
                </p>
              </div>

              <!-- Итого -->
              <div class="border-t-2 border-beige-200 pt-6 bg-gradient-to-br from-cookie-50 to-beige-50 rounded-xl p-6">
                <div class="space-y-3">
                  <div class="flex justify-between text-brown-700">
                    <span class="font-medium">Товары:</span>
                    <span class="font-semibold">{{ totalPrice }} BYN</span>
                  </div>
                  <div v-if="discount > 0" class="flex justify-between text-green-600">
                    <span class="font-medium">Скидка:</span>
                    <span class="font-bold">-{{ discount }} BYN</span>
                  </div>
                  <div v-if="form.tip > 0 && showTipOptions" class="flex justify-between text-brown-700">
                    <span class="font-medium">Чаевые:</span>
                    <span class="font-semibold">{{ calculateTip }} BYN</span>
                  </div>
                  <div class="flex justify-between items-center pt-4 border-t-2 border-cookie-200">
                    <span class="text-xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
                      Итого:
                    </span>
                    <span class="text-2xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
                      {{ finalTotal }} BYN
                    </span>
                  </div>
                </div>
              </div>

              <!-- Кнопки -->
              <div class="flex gap-4 pt-6">
                <button
                  type="button"
                  @click="handleClose"
                  class="btn-secondary flex-1"
                >
                  Отмена
                </button>
                <button
                  type="submit"
                  :disabled="isSubmitting || !isFormValid"
                  class="btn-primary flex-1"
                >
                  <span v-if="!isSubmitting" class="flex items-center justify-center gap-2">
                    <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                    </svg>
                    Оформить заказ
                  </span>
                  <span v-else class="flex items-center justify-center gap-2">
                    <svg class="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Оформление...
                  </span>
                </button>
              </div>
            </form>
          </div>
        </div>
        </transition>
      </div>
    </transition>

    <!-- Модальное окно карты -->
    <transition name="modal-fade">
      <div 
        v-if="showMapModal" 
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4"
        @click.self="closeMapModal"
      >
        <div class="bg-white rounded-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden shadow-2xl border-2 border-beige-200">
          <div class="p-6">
            <div class="flex justify-between items-center mb-6 pb-4 border-b-2 border-beige-200">
              <h3 class="text-2xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
                Выберите адрес на карте
              </h3>
              <button 
                @click="closeMapModal"
                class="p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95"
              >
                <svg class="w-6 h-6 text-brown-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
              </button>
            </div>
            
            <div class="mb-4">
              <input
                v-model="mapSearchQuery"
                type="text"
                class="input-field"
                placeholder="Поиск адреса..."
                @input="searchAddress"
              />
            </div>

            <div id="map" class="w-full h-96 rounded-lg border border-gray-300"></div>

            <div class="mt-4 flex gap-4">
              <button
                @click="closeMapModal"
                class="btn-secondary flex-1"
              >
                Отмена
              </button>
              <button
                @click="confirmAddress"
                :disabled="!selectedAddress"
                class="btn-primary flex-1"
              >
                Выбрать адрес
              </button>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  totalPrice: {
    type: Number,
    required: true
  },
  error: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close', 'submit'])

const form = ref({
  recipient: '',
  address: '',
  comment: '',
  promoCode: '',
  paymentMethod: '',
  tip: 0
})

const errors = ref({})
const promoMessage = ref('')
const discount = ref(0)
const isSubmitting = ref(false)
const showMapModal = ref(false)
const mapSearchQuery = ref('')
const selectedAddress = ref('')
let yandexMap = null
let mapInstance = null
let mapMarker = null
let geocoder = null

// Промокоды (в реальном приложении это должно приходить с сервера)
const promoCodes = {
  'DISCOUNT10': { type: 'discount', value: 10 }, // 10% скидка
  'DISCOUNT20': { type: 'discount', value: 20 }, // 20% скидка
  'BUY2GET1': { type: 'bonus', value: 'buy2get1' }, // 2 печеньки по цене 1
  'WELCOME': { type: 'discount', value: 15 } // 15% скидка для новых
}

const tipOptions = [
  { label: 'Нет', value: 0 },
  { label: '5%', value: 5 },
  { label: '10%', value: 10 },
  { label: '15%', value: 15 },
  { label: '20%', value: 20 }
]

const showTipOptions = computed(() => {
  return form.value.paymentMethod === 'CARD_ONLINE' || form.value.paymentMethod === 'CARD_ON_DELIVERY'
})

const calculateTip = computed(() => {
  if (form.value.tip === 0 || !showTipOptions.value) return 0
  const subtotal = props.totalPrice - discount.value
  return Math.round(subtotal * (form.value.tip / 100))
})

const finalTotal = computed(() => {
  // Если промокод указан, но еще не проверен, не применяем скидку
  // Скидка будет рассчитана на сервере после проверки промокода
  if (form.value.promoCode && form.value.promoCode.trim() && discount.value === 0) {
    return props.totalPrice + calculateTip.value
  }
  return props.totalPrice - discount.value + calculateTip.value
})

const isFormValid = computed(() => {
  return form.value.recipient.trim() !== '' && 
         form.value.address.trim() !== '' && 
         form.value.paymentMethod !== ''
})

const applyPromoCode = async () => {
  const code = form.value.promoCode.trim().toUpperCase()
  if (!code) {
    promoMessage.value = 'Введите промокод'
    discount.value = 0
    return
  }

  // Валидация промокода будет проверяться на сервере при оформлении заказа
  // Здесь просто сбрасываем сообщение, чтобы пользователь мог ввести промокод
  // и увидеть ошибку при попытке оформить заказ
  promoMessage.value = 'Промокод будет проверен при оформлении заказа'
  discount.value = 0
}

const openMapModal = () => {
  showMapModal.value = true
  nextTick(() => {
    initMap()
  })
}

const closeMapModal = () => {
  showMapModal.value = false
}

const confirmAddress = () => {
  // Используем выбранный адрес или текст из поискового поля
  const address = selectedAddress.value || mapSearchQuery.value.trim()
  if (address) {
    form.value.address = address
    closeMapModal()
  }
}

const initMap = () => {
  // Проверяем, загружена ли Яндекс.Карты API
  if (typeof ymaps === 'undefined') {
    // Если API не загружен, загружаем его
    // Используем более новую версию API без Suggest (который убран)
    const script = document.createElement('script')
    script.src = 'https://api-maps.yandex.ru/2.1/?lang=ru_RU&apikey='
    script.async = true
    script.onload = () => {
      try {
        if (typeof ymaps !== 'undefined') {
          ymaps.ready(() => {
            createMap()
          })
        }
      } catch (error) {
        console.warn('Ошибка инициализации Яндекс.Карт:', error)
        // Показываем простое текстовое поле для ввода адреса
        const mapContainer = document.getElementById('map')
        if (mapContainer) {
          mapContainer.innerHTML = '<p class="p-4 text-gray-600">Карта недоступна. Пожалуйста, введите адрес вручную.</p>'
        }
      }
    }
    script.onerror = () => {
      console.warn('Не удалось загрузить Яндекс.Карты API')
      const mapContainer = document.getElementById('map')
      if (mapContainer) {
        mapContainer.innerHTML = '<p class="p-4 text-gray-600">Карта недоступна. Пожалуйста, введите адрес вручную.</p>'
      }
    }
    document.head.appendChild(script)
  } else {
    try {
      ymaps.ready(() => {
        createMap()
      })
    } catch (error) {
      console.warn('Ошибка инициализации Яндекс.Карт:', error)
      const mapContainer = document.getElementById('map')
      if (mapContainer) {
        mapContainer.innerHTML = '<p class="p-4 text-gray-600">Карта недоступна. Пожалуйста, введите адрес вручную.</p>'
      }
    }
  }
}

const createMap = () => {
  const mapContainer = document.getElementById('map')
  if (!mapContainer) return

  try {
    // Удаляем старую карту, если есть
    if (mapInstance) {
      mapInstance.destroy()
      mapInstance = null
    }

    // Проверяем наличие ymaps
    if (typeof ymaps === 'undefined') {
      throw new Error('Яндекс.Карты API не загружен')
    }

    // Создаем карту с центром в Минске
    mapInstance = new ymaps.Map('map', {
      center: [53.9045, 27.5615], // Минск - центр города
      zoom: 12
    })

    // Создаем геокодер для поиска
    geocoder = ymaps.geocode

    // Добавляем маркер
    mapMarker = new ymaps.Placemark(
      mapInstance.getCenter(),
      { balloonContent: 'Выберите адрес' },
      { draggable: true }
    )
    mapInstance.geoObjects.add(mapMarker)

    // При клике на карту перемещаем маркер
    try {
      mapInstance.events.add('click', (e) => {
        try {
          const coords = e.get('coords')
          if (mapMarker) {
            mapMarker.geometry.setCoordinates(coords)
            reverseGeocode(coords)
          }
        } catch (error) {
          // Игнорируем ошибки клика
        }
      })

      // При перемещении маркера обновляем адрес
      if (mapMarker) {
        mapMarker.events.add('dragend', () => {
          try {
            const coords = mapMarker.geometry.getCoordinates()
            reverseGeocode(coords)
          } catch (error) {
            // Игнорируем ошибки перемещения маркера
          }
        })
      }
    } catch (error) {
      // Игнорируем ошибки при добавлении обработчиков событий
    }
  } catch (error) {
    // Обработка ошибок карты
    console.warn('Ошибка создания карты:', error)
    
    // Показываем информационное сообщение
    mapContainer.innerHTML = `
      <div class="p-4 text-center">
        <svg class="mx-auto h-12 w-12 text-gray-400 mb-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7" />
        </svg>
        <p class="text-gray-600 mb-2 font-medium">Карта временно недоступна</p>
        <p class="text-sm text-gray-500 mb-3">Вы можете ввести адрес вручную в поле поиска выше или продолжить оформление заказа без выбора на карте.</p>
        <p class="text-xs text-gray-400">Адрес можно указать в основном окне оформления заказа.</p>
      </div>
    `
  }
}

const reverseGeocode = (coords) => {
  if (!geocoder || !mapMarker) return
  
  try {
    geocoder(coords).then((res) => {
      const firstGeoObject = res.geoObjects.get(0)
      if (firstGeoObject && mapMarker) {
        const address = firstGeoObject.getAddressLine()
        selectedAddress.value = address || `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`
        try {
          mapMarker.properties.set('balloonContent', selectedAddress.value)
        } catch (e) {
          // Игнорируем ошибки при установке свойств маркера
        }
      } else {
        // Если нет результата, используем координаты
        selectedAddress.value = `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`
      }
    }).catch((error) => {
      // При ошибке геокодирования используем координаты
      if (mapMarker && coords) {
        selectedAddress.value = `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`
      }
      // Не выводим ошибки в консоль для снижения спама
    })
  } catch (error) {
    // При любой ошибке используем координаты
    if (coords) {
      selectedAddress.value = `${coords[0].toFixed(6)}, ${coords[1].toFixed(6)}`
    }
  }
}

const searchAddress = async () => {
  if (!mapSearchQuery.value.trim()) return

  // Если карта недоступна, просто устанавливаем адрес из поискового запроса
  if (!mapInstance) {
    selectedAddress.value = mapSearchQuery.value
    return
  }

  if (!geocoder && typeof ymaps !== 'undefined' && ymaps.geocode) {
    geocoder = ymaps.geocode
  }

  if (!geocoder || !mapInstance || !mapMarker) {
    // Если геокодер недоступен, просто используем текст как адрес
    selectedAddress.value = mapSearchQuery.value
    return
  }

  try {
    geocoder(mapSearchQuery.value).then((res) => {
      const firstGeoObject = res.geoObjects.get(0)
      if (firstGeoObject && mapInstance && mapMarker) {
        const coords = firstGeoObject.geometry.getCoordinates()
        mapInstance.setCenter(coords, 15)
        mapMarker.geometry.setCoordinates(coords)
        selectedAddress.value = firstGeoObject.getAddressLine()
        try {
          mapMarker.properties.set('balloonContent', selectedAddress.value)
        } catch (e) {
          // Игнорируем ошибки
        }
      } else {
        // Если геокодирование не дало результата, используем введенный текст
        selectedAddress.value = mapSearchQuery.value
      }
    }).catch((error) => {
      // При ошибке просто используем введенный текст как адрес
      selectedAddress.value = mapSearchQuery.value
      if (error && error.message !== 'scriptError') {
        console.warn('Ошибка поиска адреса:', error)
      }
    })
  } catch (error) {
    // При любой ошибке используем введенный текст
    selectedAddress.value = mapSearchQuery.value
    if (error && error.message !== 'scriptError') {
      console.warn('Ошибка при поиске адреса:', error)
    }
  }
}

const handleClose = () => {
  if (isSubmitting.value) return
  resetForm()
  emit('close')
}

const resetForm = () => {
  form.value = {
    recipient: '',
    address: '',
    comment: '',
    promoCode: '',
    paymentMethod: '',
    tip: 0
  }
  errors.value = {}
  promoMessage.value = ''
  discount.value = 0
  selectedAddress.value = ''
}

const handleSubmit = async () => {
  // Валидация
  errors.value = {}
  // Очищаем promoMessage при отправке формы, ошибка будет показана через props.error
  promoMessage.value = ''
  
  if (!form.value.recipient.trim()) {
    errors.value.recipient = 'Укажите получателя'
  }
  if (!form.value.address.trim()) {
    errors.value.address = 'Укажите адрес доставки'
  }
  if (!form.value.paymentMethod) {
    errors.value.paymentMethod = 'Выберите способ оплаты'
  }

  // Если указан промокод, очищаем его перед отправкой, чтобы сервер проверил его
  // (сервер проверит промокод и вернет ошибку, если он неверный)
  if (form.value.promoCode && form.value.promoCode.trim()) {
    // Промокод будет проверен на сервере
    discount.value = 0 // Сбрасываем локальную скидку, пусть сервер рассчитывает
  }

  if (Object.keys(errors.value).length > 0) {
    return
  }

  isSubmitting.value = true

  try {
    const orderData = {
      recipient: form.value.recipient,
      address: form.value.address,
      comment: form.value.comment || null,
      promoCode: form.value.promoCode && form.value.promoCode.trim() ? form.value.promoCode.trim() : null,
      paymentMethod: form.value.paymentMethod,
      tip: showTipOptions.value ? calculateTip.value : 0,
      discount: 0, // Не передаем discount, пусть сервер рассчитывает на основе промокода
      finalTotal: finalTotal.value
    }

    emit('submit', orderData)
  } catch (error) {
    console.error('Ошибка при оформлении заказа:', error)
    // Ошибка будет обработана в Drawer.vue и передана через props.error
  } finally {
    isSubmitting.value = false
  }
}

watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    resetForm()
    // Сбрасываем ошибку при открытии модального окна
    errors.value = {}
  }
})

watch(() => props.error, (newError) => {
  if (newError && (newError.includes('промокод') || newError.includes('Промокод'))) {
    // Очищаем promoMessage, чтобы не дублировать сообщение об ошибке
    // Ошибка будет показана только в блоке сверху через props.error
    promoMessage.value = ''
  }
})

onMounted(() => {
  // Загружаем Яндекс.Карты API если еще не загружен
  if (typeof ymaps === 'undefined') {
    const script = document.createElement('script')
    script.src = 'https://api-maps.yandex.ru/2.1/?lang=ru_RU&apikey='
    script.async = true
    document.head.appendChild(script)
  }
})
</script>

<style scoped>
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}

.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}

.modal-content-enter-active,
.modal-content-leave-active {
  transition: all 0.3s ease;
}

.modal-content-enter-from,
.modal-content-leave-to {
  opacity: 0;
  transform: scale(0.9);
}
</style>


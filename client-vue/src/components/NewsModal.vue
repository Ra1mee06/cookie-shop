<script setup>
import { computed } from 'vue'

const props = defineProps({
  isOpen: Boolean,
  newsItem: Object
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}

// Детальная информация для каждой новости
const newsDetails = computed(() => {
  if (!props.newsItem) return null
  
  const details = {
    1: {
      fullDescription: 'Не упустите возможность сэкономить! Скидка 20% действует на все виды печенья в нашем магазине. Акция ограничена по времени - только сегодня! Закажите любимые печеньки прямо сейчас и наслаждайтесь вкусом со скидкой.',
      additionalInfo: 'Для применения скидки используйте промокод SALE20 при оформлении заказа. Скидка не суммируется с другими акциями.',
      date: 'Действует до конца дня',
      actionText: 'Перейти к каталогу'
    },
    2: {
      fullDescription: 'Мы рады представить вам нашу новую коллекцию "Шоколадный рай"! Каждое печенье изготовлено с использованием премиального бельгийского шоколада высшего качества. Это ограниченная серия, которая доступна только в течение месяца.',
      additionalInfo: 'В коллекцию входят 5 уникальных вкусов: классический шоколад, шоколад с орехами, белый шоколад, темный шоколад с вишней и молочный шоколад с карамелью.',
      date: 'Ограниченная серия - только в декабре',
      actionText: 'Посмотреть новинки'
    },
    3: {
      fullDescription: 'Специальное предложение для наших постоянных клиентов! При покупке любых двух печенек третья достается вам абсолютно бесплатно. Выбирайте любые вкусы и комбинируйте их по своему желанию.',
      additionalInfo: 'Акция действует на все позиции в каталоге. Бесплатная печенька выбирается автоматически - самая дешевая из трех. Предложение действительно при заказе от 2 штук.',
      date: 'Действует до конца месяца',
      actionText: 'Выбрать печеньки'
    },
    4: {
      fullDescription: 'Мы открыли новый филиал в самом центре города! Теперь вы можете насладиться нашими печеньками еще ближе. В новом филиале представлен полный ассортимент, включая все новинки и эксклюзивные вкусы.',
      additionalInfo: 'Адрес нового филиала: ул. Центральная, д. 15. Режим работы: ежедневно с 9:00 до 21:00. В день открытия всех посетителей ждут приятные сюрпризы и дегустация новинок!',
      date: 'Открытие состоялось 1 декабря',
      actionText: 'Узнать адрес'
    }
  }
  
  return details[props.newsItem?.id] || {
    fullDescription: props.newsItem?.description || '',
    additionalInfo: '',
    date: '',
    actionText: 'Узнать больше'
  }
})
</script>

<template>
  <Teleport to="body">
    <transition name="modal-fade">
      <div 
        v-if="isOpen && newsItem" 
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[1000] p-4"
        @click="handleClose"
      >
      <transition name="modal-content">
        <div 
          v-if="isOpen && newsItem"
          class="bg-white rounded-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto shadow-2xl border-2 border-beige-200 relative"
          @click.stop
        >
          <!-- Кнопка закрытия -->
          <button 
            @click="handleClose"
            class="absolute top-4 right-4 p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95 z-10"
          >
            <svg class="w-6 h-6 text-brown-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>

          <!-- Заголовок с градиентом -->
          <div 
            class="relative h-64 bg-gradient-to-br overflow-hidden"
            :class="newsItem.gradient"
          >
            <!-- Фоновое изображение -->
            <div class="absolute inset-0 opacity-20">
              <img 
                :src="newsItem.image" 
                :alt="newsItem.title"
                class="w-full h-full object-cover"
              />
            </div>
            
            <!-- Контент заголовка -->
            <div class="relative z-10 h-full flex flex-col justify-center items-start p-8 text-white">
              <span 
                class="inline-block px-4 py-2 mb-4 rounded-full text-sm font-bold text-white shadow-lg"
                :class="newsItem.badgeColor"
              >
                {{ newsItem.badge }}
              </span>
              
              <h2 class="text-3xl md:text-4xl font-extrabold mb-2 leading-tight drop-shadow-lg">
                {{ newsItem.title }}
              </h2>
              
              <p class="text-lg text-white/90 drop-shadow-md">
                {{ newsDetails.date }}
              </p>
            </div>
            
            <!-- Декоративные элементы -->
            <div class="absolute top-10 right-10 w-32 h-32 bg-white/10 rounded-full blur-3xl"></div>
            <div class="absolute bottom-10 left-10 w-40 h-40 bg-white/10 rounded-full blur-3xl"></div>
          </div>

          <!-- Основной контент -->
          <div class="p-8">
            <!-- Краткое описание -->
            <div class="mb-6">
              <p class="text-lg text-brown-700 leading-relaxed">
                {{ newsItem.description }}
              </p>
            </div>

            <!-- Полное описание -->
            <div class="mb-6 bg-cookie-50 rounded-xl p-6 border-2 border-cookie-100">
              <h3 class="text-xl font-bold text-brown-800 mb-3 flex items-center gap-2">
                <svg class="w-6 h-6 text-cookie-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Подробности
              </h3>
              <p class="text-brown-700 leading-relaxed">
                {{ newsDetails.fullDescription }}
              </p>
            </div>

            <!-- Дополнительная информация -->
            <div v-if="newsDetails.additionalInfo" class="mb-6 bg-beige-50 rounded-xl p-6 border-2 border-beige-200">
              <h3 class="text-lg font-bold text-brown-800 mb-3 flex items-center gap-2">
                <svg class="w-5 h-5 text-brown-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                Важная информация
              </h3>
              <p class="text-brown-700 leading-relaxed">
                {{ newsDetails.additionalInfo }}
              </p>
            </div>

            <!-- Кнопка действия -->
            <div class="flex justify-center pt-4">
              <button
                @click="handleClose"
                class="btn-primary px-8 py-4 text-lg"
              >
                <span class="flex items-center gap-2">
                  <svg class="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  Понятно, спасибо!
                </span>
              </button>
            </div>
          </div>
        </div>
      </transition>
    </div>
    </transition>
  </Teleport>
</template>

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

.modal-content-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}

.modal-content-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}
</style>


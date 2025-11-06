<script setup>
defineProps({
  isOpen: Boolean,
  title: {
    type: String,
    default: 'Вы не авторизованы'
  },
  message: {
    type: String,
    default: 'Для выполнения этого действия необходимо авторизоваться. Пожалуйста, войдите в систему.'
  }
})

const emit = defineEmits(['close', 'goToAuth'])

const handleClose = () => {
  emit('close')
}

const handleGoToAuth = () => {
  emit('goToAuth')
}
</script>

<template>
  <transition name="modal-fade">
    <div 
      v-if="isOpen" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
      @click="handleClose"
    >
      <transition name="modal-content">
        <div 
          v-if="isOpen"
          class="bg-white p-8 rounded-2xl w-full max-w-md shadow-2xl border-2 border-beige-200 relative"
          @click.stop
        >
          <button 
            @click="handleClose"
            class="absolute top-4 right-4 p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95"
          >
            <svg class="w-5 h-5 text-brown-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
            </svg>
          </button>
          <div class="text-center">
            <div class="mx-auto flex items-center justify-center h-16 w-16 rounded-full bg-gradient-to-br from-yellow-100 to-orange-100 mb-6 shadow-lg">
              <svg class="h-8 w-8 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
              </svg>
            </div>
            <h3 class="text-2xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-3">
              {{ title }}
            </h3>
            <p class="text-brown-600 mb-8">{{ message }}</p>
            <div class="flex gap-4 justify-center">
              <button 
                @click="handleClose"
                class="btn-secondary"
              >
                Отмена
              </button>
              <button 
                @click="handleGoToAuth"
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
</template>


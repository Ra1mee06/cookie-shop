<template>
  <transition name="modal-fade">
    <div 
      v-if="isOpen" 
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[10000] p-4"
      @click.self="handleClose"
    >
      <transition name="modal-content">
        <div 
          v-if="isOpen"
          class="bg-white rounded-2xl w-full max-w-3xl max-h-[90vh] overflow-y-auto shadow-2xl border-2 border-beige-200 relative"
          @click.stop
        >
          <button 
            @click="handleClose"
            class="absolute top-4 right-4 p-2 rounded-full hover:bg-cookie-100 transition-all duration-300 transform hover:scale-110 active:scale-95 z-10"
          >
            <svg class="w-6 h-6 text-brown-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
          
          <div class="p-8">
            <h2 class="text-3xl md:text-4xl font-extrabold mb-6 bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
              {{ title }}
            </h2>
            
            <div class="prose prose-brown max-w-none" v-html="content"></div>
          </div>
        </div>
      </transition>
    </div>
  </transition>
</template>

<script setup>
const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    required: true
  },
  content: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['close'])

const handleClose = () => {
  emit('close')
}
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

:deep(.prose) {
  color: #5D4037;
  line-height: 1.75;
}

:deep(.prose h3) {
  font-size: 1.5rem;
  font-weight: 700;
  margin-top: 2rem;
  margin-bottom: 1rem;
  color: #8B4513;
}

:deep(.prose p) {
  margin-bottom: 1rem;
}

:deep(.prose ul) {
  list-style-type: disc;
  margin-left: 1.5rem;
  margin-bottom: 1rem;
}

:deep(.prose li) {
  margin-bottom: 0.5rem;
}

:deep(.prose strong) {
  font-weight: 600;
  color: #6D4C41;
}

:deep(.prose a) {
  color: #D2691E;
  text-decoration: underline;
}

:deep(.prose a:hover) {
  color: #CD853F;
}
</style>


<script setup>
const emit = defineEmits(['onClickRemove', 'onClickDelete', 'onClickAdd'])

defineProps({
  id: Number,
  title: String,
  imageUrl: String,
  price: Number,
  quantity: Number
})
</script>

<template>
  <div class="flex items-center border-2 border-beige-200 bg-white p-4 rounded-xl gap-4 hover:border-cookie-300 hover:shadow-md transition-all duration-300">
    <div class="w-20 h-20 rounded-lg overflow-hidden bg-gradient-to-br from-beige-50 to-cookie-50 flex-shrink-0">
      <img class="w-full h-full object-contain p-2" :src="imageUrl" :alt="title" />
    </div>

    <div class="flex flex-col flex-1 min-w-0">
      <p class="font-semibold text-brown-800 truncate mb-2">{{ title }}</p>

      <div class="flex justify-between items-center gap-2">
        <div class="flex items-center gap-3">
          <b class="text-lg font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent">
            {{ price }} BYN
          </b>
          <span v-if="quantity" class="px-2 py-1 bg-cookie-100 text-cookie-700 rounded-lg text-sm font-semibold">
            ×{{ quantity }}
          </span>
        </div>
        
        <div class="flex items-center gap-2">
          <button
            @click="$emit('onClickAdd')"
            class="p-2 bg-gradient-to-r from-cookie-500 to-cookie-600 rounded-full shadow-md hover:shadow-lg transition-all duration-300 transform hover:scale-110 active:scale-95"
          >
            <img src="/plus.svg" alt="+" class="w-4 h-4 filter brightness-0 invert" />
          </button>
          
          <button
            @click="$emit('onClickDelete')"
            class="p-2 bg-beige-300 rounded-full shadow-md hover:bg-beige-400 transition-all duration-300 transform hover:scale-110 active:scale-95"
            :disabled="!quantity || quantity <= 1"
            :class="{'opacity-50 cursor-not-allowed': !quantity || quantity <= 1}"
          >
            <img src="/minus.svg" alt="-" class="w-4 h-4" />
          </button>
          
          <button
            @click="$emit('onClickRemove')"
            class="p-2 bg-red-100 rounded-full border-2 border-red-200 shadow-md hover:bg-red-200 hover:border-red-300 transition-all duration-300 transform hover:scale-110 active:scale-95"
          >
            <img src="/trash.svg" alt="×" class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
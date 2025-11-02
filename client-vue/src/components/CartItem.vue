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
  <div class="flex items-center border border-slate-200 p-4 rounded-xl gap-4">
    <img class="w-16 h-16" :src="imageUrl" :alt="title" />

    <div class="flex flex-col flex-1">
      <p>{{ title }}</p>

      <div class="flex justify-between mt-2 items-center">
        <b class="flex-1">{{ price }} бун</b>
        <span v-if="quantity" class="mx-2">{{ quantity }} шт.</span>
        
        <button
          @click="$emit('onClickAdd')"
          class="p-2 bg-white rounded-full border border-slate-200 shadow-sm hover:bg-green-50 hover:border-green-200 transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-md"
        >
          <img src="/plus.svg" alt="+" class="w-5 h-5 opacity-80 hover:opacity-100" />
        </button>
        
        <button
          @click="$emit('onClickDelete')"
          class="p-2 bg-white rounded-full border border-slate-200 shadow-sm hover:bg-yellow-50 hover:border-yellow-200 transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-md mx-1"
        >
          <img src="/minus.svg" alt="-" class="w-5 h-5 opacity-80 hover:opacity-100" />
        </button>
        
        <button
          @click="$emit('onClickRemove')"
          class="p-2 bg-white rounded-full border border-slate-200 shadow-sm hover:bg-red-50 hover:border-red-200 transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-md"
        >
          <img src="/trash.svg" alt="×" class="w-5 h-5 opacity-80 hover:opacity-100" />
        </button>
      </div>
    </div>
  </div>
</template>
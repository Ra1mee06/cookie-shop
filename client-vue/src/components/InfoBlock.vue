<script setup>
defineProps({
  title: String,
  description: String,
  imageUrl: String
})
</script>

<template>
  <div class="flex flex-col items-center text-center w-72 mx-auto">
    <div class="w-24 h-24 rounded-full bg-gradient-to-br from-cookie-100 to-beige-100 flex items-center justify-center mb-6 shadow-lg">
      <img height="60" width="60" :src="imageUrl" alt="Info image" class="object-contain" />
    </div>
    <h2 class="text-2xl md:text-3xl font-extrabold bg-gradient-to-r from-cookie-600 to-brown-700 bg-clip-text text-transparent mb-3">
      {{ title }}
    </h2>
    <p class="text-brown-600 font-medium">{{ description }}</p>
  </div>
</template>

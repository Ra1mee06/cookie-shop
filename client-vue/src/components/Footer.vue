<template>
  <footer class="bg-gradient-to-br from-brown-800 to-brown-900 text-white mt-16">
    <div class="max-w-[95%] xl:max-w-[1600px] mx-auto px-6 md:px-12 py-12">
      <!-- Основной контент футера -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8 mb-8">
        <!-- Компания -->
        <div>
          <h3 class="text-lg font-bold mb-4 text-cookie-300">Компания</h3>
          <ul class="space-y-2 text-sm">
            <li>
              <router-link to="/" class="hover:text-cookie-300 transition-colors">
                Главная
              </router-link>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                О нас
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Наша история
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Контакты
              </a>
            </li>
          </ul>
        </div>

        <!-- Заказ -->
        <div>
          <h3 class="text-lg font-bold mb-4 text-cookie-300">Заказ</h3>
          <ul class="space-y-2 text-sm">
            <li>
              <router-link to="/" class="hover:text-cookie-300 transition-colors">
                Меню
              </router-link>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Доставка
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Подарочные карты
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Питание и аллергены
              </a>
            </li>
          </ul>
        </div>

        <!-- Присоединяйтесь -->
        <div>
          <h3 class="text-lg font-bold mb-4 text-cookie-300">Присоединяйтесь</h3>
          <ul class="space-y-2 text-sm">
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Пресса
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Сотрудничество
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Франшиза
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Карьера
              </a>
            </li>
          </ul>
        </div>

        <!-- Поддержка -->
        <div>
          <h3 class="text-lg font-bold mb-4 text-cookie-300">Поддержка</h3>
          <ul class="space-y-2 text-sm">
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Помощь
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Баланс подарочной карты
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Политика конфиденциальности
              </a>
            </li>
            <li>
              <a href="#" class="hover:text-cookie-300 transition-colors">
                Условия использования
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Разделитель -->
      <div class="border-t border-brown-700 my-8"></div>

      <!-- Нижняя часть футера -->
      <div class="flex flex-col md:flex-row justify-between items-center gap-4">
        <div class="text-sm text-brown-300">
          <p>© {{ currentYear }} RЯЛЬНА COOKIES. Все права защищены.</p>
        </div>
        
        <div class="flex flex-wrap items-center gap-4 text-sm">
          <a href="#" class="hover:text-cookie-300 transition-colors">
            Политика конфиденциальности
          </a>
          <span class="text-brown-600">|</span>
          <a href="#" class="hover:text-cookie-300 transition-colors">
            Условия и положения
          </a>
          <span class="text-brown-600">|</span>
          <a href="#" class="hover:text-cookie-300 transition-colors">
            Настройки cookie
          </a>
        </div>
      </div>

      <!-- Товарные знаки и маркировки -->
      <div class="mt-6 pt-6 border-t border-brown-700">
        <div class="flex flex-col md:flex-row justify-between items-center gap-4 text-xs text-brown-400">
          <div class="text-center md:text-left">
            <p>RЯЛЬНА COOKIES™ является зарегистрированным товарным знаком.</p>
            <p class="mt-1">Все названия продуктов и логотипы являются товарными знаками их соответствующих владельцев.</p>
          </div>
          <div class="flex items-center gap-2">
            <span>🍪</span>
            <span>Сделано с любовью</span>
          </div>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>


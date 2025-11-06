import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/pages/Home.vue'
import Favorites from '@/pages/Favorites.vue'
import Profile from '@/pages/Profile.vue'
const AdminOrders = () => import('@/pages/AdminOrders.vue').catch(err => {
  console.error('Failed to load AdminOrders component:', err);
  // Возвращаем пустой компонент вместо редиректа
  return { default: { template: '<div>Ошибка загрузки страницы заказов</div>' } };
});
const AdminUsers = () => import('@/pages/AdminUsers.vue')
const AdminPromos = () => import('@/pages/AdminPromos.vue')
const AdminInvites = () => import('@/pages/AdminInvites.vue')
const AdminSuggestions = () => import('@/pages/AdminSuggestions.vue')

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: Favorites
  },
  {
    path: '/profile',
    name: 'Profile',
    component: Profile
  },
  {
    path: '/product/:id',
    name: 'Product',
    component: () => import('@/pages/ProductPage.vue'),
    props: true
  }
  ,
  {
    path: '/admin',
    redirect: '/admin/orders'
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: AdminOrders
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers
  },
  {
    path: '/admin/promocodes',
    name: 'AdminPromos',
    component: AdminPromos
  },
  {
    path: '/admin/invites',
    name: 'AdminInvites',
    component: AdminInvites
  },
  {
    path: '/admin/suggestions',
    name: 'AdminSuggestions',
    component: AdminSuggestions
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  console.log('Router navigation:', { from: from.path, to: to.path });
  
  if (to.path.startsWith('/admin')) {
    try {
      const saved = localStorage.getItem('user')
      if (!saved) {
        console.log('No user found, redirecting to profile')
        return next('/profile')
      }
      const u = JSON.parse(saved)
      console.log('User role:', u?.role);
      if (u?.role !== 'ADMIN') {
        console.log('User is not admin, redirecting to profile')
        return next('/profile')
      }
      console.log('Admin access granted, navigating to:', to.path)
      next()
    } catch (e) {
      console.error('Error checking admin access:', e)
      return next('/profile')
    }
  } else {
    next()
  }
})

export default router
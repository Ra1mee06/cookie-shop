import { createRouter, createWebHistory } from 'vue-router'
import Home from '@/pages/Home.vue'
import Favorites from '@/pages/Favorites.vue'
import Profile from '@/pages/Profile.vue'
const AdminOrders = () => import('@/pages/AdminOrders.vue')
const AdminUsers = () => import('@/pages/AdminUsers.vue')
const AdminPromos = () => import('@/pages/AdminPromos.vue')
const AdminInvites = () => import('@/pages/AdminInvites.vue')

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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  if (to.path.startsWith('/admin')) {
    try {
      const saved = localStorage.getItem('user')
      if (!saved) return next('/profile')
      const u = JSON.parse(saved)
      if (u?.role !== 'ADMIN') return next('/profile')
    } catch (e) {
      return next('/profile')
    }
  }
  next()
})

export default router
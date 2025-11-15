import axios, { AxiosInstance, AxiosRequestConfig } from 'axios'

interface LoginCredentials {
  loginOrEmail?: string
  email?: string
  password: string
}

interface RegisterData {
  username: string
  email: string
  password: string
  fullName: string
  adminInviteCode?: string
}

interface ProfileData {
  email?: string
  fullName?: string
  username?: string
}

interface PasswordData {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

interface OrderData {
  items: Array<{
    productId: number
    quantity: number
    price: number
  }>
  totalPrice: number
  finalTotal?: number
  discount?: number
  promoCode?: string
  recipient?: string
  address?: string
  comment?: string
  paymentMethod?: string
  tip?: number
}

interface SuggestionData {
  author: string
  productName: string
  description: string
}

interface PromoCodeData {
  code: string
  type: string
  value?: number
  maxUses?: number
  expiresAt?: string
  productId?: number
}

interface AdminOrderUpdates {
  recipient?: string
  address?: string
  comment?: string
  paymentMethod?: string
  status?: string
  totalPrice?: number
  discount?: number
}

interface AdminUserUpdates {
  email?: string
  fullName?: string
  username?: string
  avatarUrl?: string
  role?: string
}

const api: AxiosInstance = axios.create({
  baseURL: 'http://localhost:8081/api',
  timeout: 10000
})

api.interceptors.request.use((config: AxiosRequestConfig) => {
  const token = localStorage.getItem('authToken')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  const userId = localStorage.getItem('userId')
  if (userId && config.headers) {
    config.headers['X-User-Id'] = userId
  }
  
  return config
})

export const useApi = () => {
  const products = {
    getAll: (params: Record<string, any> = {}) => api.get('/products', { params }),
    getById: (id: number) => api.get(`/products/${id}`),
    search: (query: string) => api.get('/products', { params: { search: query } })
  }

  const auth = {
    login: (credentials: LoginCredentials) => api.post('/auth/login', credentials),
    register: (userData: RegisterData) => api.post('/auth/register', userData),
    getProfile: () => api.get('/auth/me'),
    updateProfile: (profileData: ProfileData) => api.put('/auth/profile', profileData),
    updatePassword: (passwordData: PasswordData) => api.put('/auth/profile/password', passwordData),
    updateAvatar: (avatarUrl?: string, file?: File) => {
      const formData = new FormData()
      if (avatarUrl) {
        formData.append('avatarUrl', avatarUrl)
      }
      if (file) {
        formData.append('file', file)
      }
      return api.post('/auth/profile/avatar', formData, {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      })
    }
  }

  const favorites = {
    getAll: () => api.get('/favorites'),
    add: (productId: number) => api.post('/favorites', { productId }),
    remove: (favoriteId: number) => api.delete(`/favorites/${favoriteId}`),
    clearAll: () => api.delete('/favorites')
  }

  const orders = {
    create: (orderData: OrderData) => api.post('/orders', orderData),
    getByUser: () => api.get('/orders/user')
  }

  const suggestions = {
    create: (suggestionData: SuggestionData) => api.post('/suggestions', suggestionData),
    getByUser: () => api.get('/suggestions/user')
  }

  const admin = {
    orders: {
      list: () => api.get('/admin/orders'),
      get: (id: number) => api.get(`/admin/orders/${id}`),
      update: (id: number, updates: AdminOrderUpdates) => api.put(`/admin/orders/${id}`, updates),
      changeStatus: (id: number, status: string) => api.post(`/admin/orders/${id}/status`, { status })
    },
    users: {
      list: () => api.get('/admin/users'),
      get: (id: number) => api.get(`/admin/users/${id}`),
      orders: (id: number) => api.get(`/admin/users/${id}/orders`),
      suggestions: (id: number) => api.get(`/admin/users/${id}/suggestions`),
      update: (id: number, updates: AdminUserUpdates) => api.put(`/admin/users/${id}`, updates),
      remove: (id: number) => api.delete(`/admin/users/${id}`)
    },
    promocodes: {
      list: () => api.get('/admin/promos'),
      create: (data: PromoCodeData) => api.post('/admin/promos', data),
      update: (id: number, data: PromoCodeData) => api.put(`/admin/promos/${id}`, data),
      deactivate: (id: number) => api.delete(`/admin/promos/${id}`)
    },
    suggestions: {
      list: (userId?: number) => api.get('/admin/suggestions', { params: userId ? { userId } : {} }),
      remove: (id: number) => api.delete(`/admin/suggestions/${id}`)
    },
    invites: {
      create: (expiresInHours?: number) => {
        const payload = expiresInHours ? { expiresInHours } : {}
        return api.post('/admin/invites', payload)
      },
      list: () => api.get('/admin/invites'),
      remove: (id: number) => api.delete(`/admin/invites/${id}`)
    }
  }

  return {
    products,
    auth,
    favorites,
    orders,
    suggestions,
    admin
  }
}


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
  baseURL: 'http://localhost:18081/api',
  timeout: 10000
})

const toCamelCase = (key: string) =>
  key.replace(/_([a-z])/g, (_, char: string) => char.toUpperCase())

const maybeFixMojibake = (value: string) => {
  if (!value || (!value.includes('Ð') && !value.includes('Ñ'))) {
    return value
  }
  try {
    const bytes = Uint8Array.from(value, (char) => char.charCodeAt(0) & 0xff)
    const decoded = new TextDecoder('utf-8').decode(bytes)
    return decoded.includes('�') ? value : decoded
  } catch {
    return value
  }
}

const normalizeResponseData = (payload: unknown): unknown => {
  if (typeof payload === 'string') {
    return maybeFixMojibake(payload)
  }
  if (Array.isArray(payload)) {
    return payload.map(normalizeResponseData)
  }
  if (payload && typeof payload === 'object') {
    const normalized: Record<string, unknown> = {}
    for (const [key, value] of Object.entries(payload as Record<string, unknown>)) {
      normalized[toCamelCase(key)] = normalizeResponseData(value)
    }
    return normalized
  }
  return payload
}

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

api.interceptors.response.use((response) => {
  response.data = normalizeResponseData(response.data)
  return response
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
      if (file) {
        const formData = new FormData()
        formData.append('file', file)
        if (avatarUrl) {
          formData.append('avatarUrl', avatarUrl)
        }
        return api.post('/auth/profile/avatar', formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        })
      }
      return api.post('/auth/profile/avatar', { avatarUrl: avatarUrl ?? null })
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


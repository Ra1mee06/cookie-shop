import axios from 'axios'

const api = axios.create({
    baseURL: 'http://localhost:8080/api',
    timeout: 10000
})

// Добавляем токен и userId к каждому запросу
api.interceptors.request.use(config => {
    const token = localStorage.getItem('authToken')
    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }
    
    // Добавляем userId для favorites (временное решение, пока нет полноценной аутентификации)
    const userId = localStorage.getItem('userId')
    if (userId) {
        config.headers['X-User-Id'] = userId
    }
    
    return config
})

export const useApi = () => {
    const products = {
        getAll: (params = {}) => api.get('/products', { params }),
        getById: (id) => api.get(`/products/${id}`),
        search: (query) => api.get('/products', { params: { search: query } })
    }

    const auth = {
        login: (credentials) => api.post('/auth/login', credentials),
        register: (userData) => api.post('/auth/register', userData),
        getProfile: () => api.get('/auth/me'),
        updateProfile: (profileData) => api.put('/auth/profile', profileData),
        updatePassword: (passwordData) => api.put('/auth/profile/password', passwordData),
        updateAvatar: (avatarUrl, file) => {
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
        add: (productId) => api.post('/favorites', { productId }),
        remove: (favoriteId) => api.delete(`/favorites/${favoriteId}`),
        clearAll: () => api.delete('/favorites')
    }

    const orders = {
        create: (orderData) => api.post('/orders', orderData),
        getByUser: () => api.get('/orders/user')
    }

    const suggestions = {
        create: (suggestionData) => api.post('/suggestions', suggestionData),
        getByUser: () => api.get('/suggestions/user')
    }

    const admin = {
        orders: {
            list: () => api.get('/admin/orders'),
            get: (id) => api.get(`/admin/orders/${id}`),
            update: (id, updates) => api.put(`/admin/orders/${id}`, updates),
            changeStatus: (id, status) => api.post(`/admin/orders/${id}/status`, { status })
        },
        users: {
            list: () => api.get('/admin/users'),
            get: (id) => api.get(`/admin/users/${id}`),
            orders: (id) => api.get(`/admin/users/${id}/orders`),
            suggestions: (id) => api.get(`/admin/users/${id}/suggestions`),
            update: (id, updates) => api.put(`/admin/users/${id}`, updates),
            remove: (id) => api.delete(`/admin/users/${id}`)
        },
        promocodes: {
            list: () => api.get('/admin/promocodes'),
            create: (data) => api.post('/admin/promocodes', data),
            update: (id, data) => api.put(`/admin/promocodes/${id}`, data),
            deactivate: (id) => api.delete(`/admin/promocodes/${id}`)
        },
        suggestions: {
            list: (userId) => api.get('/admin/suggestions', { params: userId ? { userId } : {} }),
            remove: (id) => api.delete(`/admin/suggestions/${id}`)
        },
        invites: {
            create: (expiresInHours) => api.post('/admin/invites', expiresInHours ? { expiresInHours } : {})
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
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
        getProfile: () => api.get('/auth/me')
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
        create: (suggestionData) => api.post('/suggestions', suggestionData)
    }

    return {
        products,
        auth,
        favorites,
        orders,
        suggestions
    }
}
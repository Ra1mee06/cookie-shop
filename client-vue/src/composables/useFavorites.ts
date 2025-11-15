import { ref, provide } from 'vue';
import { useApi } from './useApi';

interface FavoriteItem {
  id: number;
  product: {
    id: number;
    title: string;
    price: number;
    imageUrl: string;
    description?: string;
  };
}

interface UserItem {
  id: number;
  isFavorite?: boolean;
  favoriteId?: number | null;
  [key: string]: any;
}

interface LocalFavoriteItem {
  productId: number;
  product: {
    id: number;
    title: string;
    price: number;
    imageUrl: string;
    description?: string;
  };
}

const LOCAL_FAVORITES_KEY = 'localFavorites';

const isAuthenticated = (): boolean => {
  const token = localStorage.getItem('authToken');
  const userId = localStorage.getItem('userId');
  return !!(token && userId);
};

const saveLocalFavorites = (items: LocalFavoriteItem[]) => {
  localStorage.setItem(LOCAL_FAVORITES_KEY, JSON.stringify(items));
};

const loadLocalFavorites = (): LocalFavoriteItem[] => {
  try {
    const stored = localStorage.getItem(LOCAL_FAVORITES_KEY);
    return stored ? JSON.parse(stored) : [];
  } catch (e) {
    console.error('Ошибка при загрузке локального избранного:', e);
    return [];
  }
};

const clearLocalFavorites = () => {
  localStorage.removeItem(LOCAL_FAVORITES_KEY);
};

const convertLocalToFavoriteItem = (localItem: LocalFavoriteItem): FavoriteItem => {
  return {
    id: -localItem.productId,
    product: localItem.product
  };
};

export function useFavorites() {
  const favorites = ref<FavoriteItem[]>([]);
  const { favorites: favoritesApi } = useApi();

  const fetchFavorites = async (force = false): Promise<FavoriteItem[]> => {
    try {
      if (!isAuthenticated()) {
        const localFavorites = loadLocalFavorites();
        const convertedFavorites = localFavorites.map(convertLocalToFavoriteItem);
        favorites.value = convertedFavorites;
        return convertedFavorites;
      }

      const userId = localStorage.getItem('userId');
      if (!userId) {
        console.warn("userId не найден в localStorage. Пропускаем загрузку избранного.");
        if (!force && favorites.value.length > 0) {
          return favorites.value;
        }
        if (!force) {
          favorites.value = [];
          return [];
        }
      }

      const response = await favoritesApi.getAll();
      const data = response?.data;
      const favoritesList = Array.isArray(data) ? data : [];
      
      if (favoritesList.length < favorites.value.length && favorites.value.length > 0) {
        const currentUserId = localStorage.getItem('userId');
        if (!currentUserId) {
          console.warn("userId не найден после запроса.");
          if (!force) {
            return favorites.value;
          }
        } else {
          if (favoritesList.length === 0) {
            console.warn("Сервер вернул пустой список.");
          } else {
            console.warn(`Сервер вернул меньше данных (${favoritesList.length}) чем у нас есть (${favorites.value.length}).`);
          }
          if (force && favoritesList.length === 0) {
            favorites.value = [];
          } else if (!force) {
            const mergedFavorites = [...favorites.value];
            favoritesList.forEach(serverFav => {
              const exists = mergedFavorites.some(fav => 
                fav.id === serverFav.id || 
                (fav.product && serverFav.product && fav.product.id === serverFav.product.id)
              );
              if (!exists) {
                mergedFavorites.push(serverFav);
              }
            });
            favorites.value = mergedFavorites;
          }
        }
      } else {
        if (force || JSON.stringify(favorites.value) !== JSON.stringify(favoritesList)) {
          favorites.value = favoritesList;
        }
      }
      
      return favoritesList;
    } catch (err) {
      console.error("Ошибка загрузки избранного:", err);
      if (!force && favorites.value.length > 0) {
        return favorites.value;
      }
      if (force) {
        favorites.value = [];
      }
      return favorites.value;
    }
  };

  const toggleFavorite = async (item: UserItem): Promise<{ needsAuth: boolean } | void> => {
    const wasFavorite = item.isFavorite;
    const previousFavoriteId = item.favoriteId;
    
    if (!isAuthenticated()) {
      return { needsAuth: true };
    }
    
    try {
      if (!item.isFavorite) {
        item.isFavorite = true;
        
        const response = await favoritesApi.add(item.id);
        const favoriteData = response.data;
        
        if (favoriteData && favoriteData.id) {
          item.favoriteId = favoriteData.id;
          
          if (favoriteData.product) {
            const favoriteItem: FavoriteItem = {
              id: favoriteData.id,
              product: favoriteData.product
            };
            const exists = favorites.value.some(fav => fav.id === favoriteItem.id || 
              (fav.product && fav.product.id === favoriteItem.product.id));
            if (!exists) {
              favorites.value = [...favorites.value, favoriteItem];
            }
          } else {
            // Если в ответе нет product, загружаем список заново
            // Используем force=false чтобы не перезаписать данные пустым массивом при ошибке
            await fetchFavorites(false);
          }
        } else {
          // Если ответ не содержит id, откатываем состояние
          item.isFavorite = false;
          throw new Error("Не удалось получить ID избранного элемента от сервера");
        }
      } else {
        if (item.favoriteId) {
          item.isFavorite = false;
          const favoriteIdToRemove = item.favoriteId;
          item.favoriteId = null;
          
          await removeFavorite(favoriteIdToRemove);
        }
      }
    } catch (err) {
      item.isFavorite = wasFavorite;
      item.favoriteId = previousFavoriteId;
      console.error("Ошибка при обновлении избранного:", err);
      throw err;
    }
  };

  const addToLocalFavorites = (product: UserItem) => {
    const localFavorites = loadLocalFavorites();
    const exists = localFavorites.some(fav => fav.productId === product.id);
    if (!exists) {
      const localItem: LocalFavoriteItem = {
        productId: product.id,
        product: {
          id: product.id,
          title: product.title,
          price: product.price,
          imageUrl: product.imageUrl,
          description: product.description
        }
      };
      localFavorites.push(localItem);
      saveLocalFavorites(localFavorites);
      favorites.value = localFavorites.map(convertLocalToFavoriteItem);
    }
  };

  const removeFromLocalFavorites = (productId: number) => {
    const localFavorites = loadLocalFavorites();
    const filtered = localFavorites.filter(fav => fav.productId !== productId);
    saveLocalFavorites(filtered);
    favorites.value = filtered.map(convertLocalToFavoriteItem);
  };

  // Синхронизация избранного
  const syncLocalFavoritesToServer = async (): Promise<void> => {
    if (!isAuthenticated()) {
      return;
    }

    const localFavorites = loadLocalFavorites();
    if (localFavorites.length === 0) {
      return;
    }

    try {
      for (const localItem of localFavorites) {
        try {
          await favoritesApi.add(localItem.productId);
        } catch (err) {
          console.warn(`Не удалось синхронизировать товар ${localItem.productId}:`, err);
        }
      }
      
      clearLocalFavorites();
      await fetchFavorites(true);
    } catch (err) {
      console.error('Ошибка при синхронизации локального избранного:', err);
    }
  };

  const removeFavorite = async (favoriteId: number) => {
    try {
      if (!isAuthenticated()) {
        const favorite = favorites.value.find(fav => fav.id === favoriteId);
        if (favorite && favorite.product) {
          removeFromLocalFavorites(favorite.product.id);
        } else {
          const productId = Math.abs(favoriteId);
          removeFromLocalFavorites(productId);
          favorites.value = favorites.value.filter(fav => fav.id !== favoriteId);
        }
        return;
      }
      
      await favoritesApi.remove(favoriteId);
      favorites.value = favorites.value.filter(fav => fav.id !== favoriteId);
    } catch (err) {
      console.error("Ошибка при удалении из избранного:", err);
      throw err;
    }
  };

  const clearAllFavorites = async () => {
    try {
      if (!isAuthenticated()) {
        clearLocalFavorites();
        favorites.value = [];
        return;
      }
      
      await favoritesApi.clearAll();
      favorites.value = [];
    } catch (err) {
      console.error("Ошибка при очистке избранного:", err);
      await fetchFavorites();
      throw err;
    }
  };

  const clearLocalOnLogout = () => {
    clearLocalFavorites();
    favorites.value = [];
  };

  provide('favorites', {
    favorites,
    fetchFavorites,
    toggleFavorite,
    removeFavorite,
    clearAllFavorites,
    addToLocalFavorites,
    removeFromLocalFavorites,
    syncLocalFavoritesToServer,
    clearLocalOnLogout,
    isAuthenticated,
  });

  return {
    favorites,
    fetchFavorites,
    toggleFavorite,
    removeFavorite,
    clearAllFavorites,
    addToLocalFavorites,
    removeFromLocalFavorites,
    syncLocalFavoritesToServer,
    clearLocalOnLogout,
    isAuthenticated,
  };
}
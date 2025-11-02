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

export function useFavorites() {
  const favorites = ref<FavoriteItem[]>([]);
  const { favorites: favoritesApi } = useApi();

  const fetchFavorites = async (force = false): Promise<FavoriteItem[]> => {
    try {
      // Проверяем userId перед запросом
      const userId = localStorage.getItem('userId');
      if (!userId) {
        console.warn("userId не найден в localStorage. Пропускаем загрузку избранного.");
        // Если у нас уже есть данные и это не принудительная загрузка, не перезаписываем их
        if (!force && favorites.value.length > 0) {
          return favorites.value;
        }
        // Если данных нет, возвращаем пустой массив
        if (!force) {
          favorites.value = [];
          return [];
        }
      }

      const response = await favoritesApi.getAll();
      // Убеждаемся, что response.data существует и является массивом
      const data = response?.data;
      const favoritesList = Array.isArray(data) ? data : [];
      
      // Логируем для отладки
      console.log('Загружено избранных товаров:', favoritesList.length, 'userId:', userId, favoritesList);
      
      // Проверяем, не вернул ли сервер меньше данных, чем у нас уже есть
      // Это может указывать на проблему с userId или задержку сохранения на сервере
      if (favoritesList.length < favorites.value.length && favorites.value.length > 0) {
        // Проверяем, установлен ли userId сейчас (может он был потерян)
        const currentUserId = localStorage.getItem('userId');
        if (!currentUserId) {
          console.warn("userId не найден после запроса. Возможно, он был потерян. Сохраняем текущие данные.");
          if (!force) {
            return favorites.value;
          }
        } else {
          if (favoritesList.length === 0) {
            console.warn("Сервер вернул пустой список, хотя userId установлен. Это может быть ошибка сервера или данные действительно пустые.");
          } else {
            console.warn(`Сервер вернул меньше данных (${favoritesList.length}) чем у нас есть (${favorites.value.length}). Возможно, данные еще не синхронизированы.`);
          }
          // Если это принудительная загрузка и список пуст, доверяем серверу
          if (force && favoritesList.length === 0) {
            favorites.value = [];
          } else if (!force) {
            // Если не принудительная, сохраняем существующие данные
            // Объединяем данные с сервера с существующими, убирая дубликаты
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
        // Обновляем только если это принудительная загрузка или если данные реально изменились
        if (force || JSON.stringify(favorites.value) !== JSON.stringify(favoritesList)) {
          favorites.value = favoritesList;
        }
      }
      
      return favoritesList;
    } catch (err) {
      console.error("Ошибка загрузки избранного:", err);
      // Возвращаем текущий список, чтобы не терять состояние, только если это не принудительная загрузка
      if (!force && favorites.value.length > 0) {
        console.warn("Используем кешированные данные избранного из-за ошибки загрузки");
        return favorites.value;
      }
      // Если данных нет и произошла ошибка, устанавливаем пустой массив
      if (force) {
        favorites.value = [];
      }
      return favorites.value;
    }
  };

  const toggleFavorite = async (item: UserItem) => {
    const wasFavorite = item.isFavorite;
    const previousFavoriteId = item.favoriteId;
    
    try {
      if (!item.isFavorite) {
        // Оптимистичное обновление UI
        item.isFavorite = true;
        
        const response = await favoritesApi.add(item.id);
        // Проверяем структуру ответа: response.data может быть FavoriteDTO напрямую
        const favoriteData = response.data;
        
        if (favoriteData && favoriteData.id) {
          item.favoriteId = favoriteData.id;
          
          // Обновляем локальный список избранного для страницы Favorites
          // Проверяем наличие product в ответе
          if (favoriteData.product) {
            const favoriteItem: FavoriteItem = {
              id: favoriteData.id,
              product: favoriteData.product
            };
            // Проверяем, нет ли уже такого элемента в списке
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
          // Оптимистичное обновление UI
          item.isFavorite = false;
          const favoriteIdToRemove = item.favoriteId;
          item.favoriteId = null;
          
          await removeFavorite(favoriteIdToRemove);
          // removeFavorite уже обновляет favorites.value
        }
      }
    } catch (err) {
      // Откатываем оптимистичное обновление при ошибке
      item.isFavorite = wasFavorite;
      item.favoriteId = previousFavoriteId;
      console.error("Ошибка при обновлении избранного:", err);
      throw err; // Пробрасываем ошибку, чтобы вызывающий код мог обработать её
    }
  };

  const removeFavorite = async (favoriteId: number) => {
    try {
      await favoritesApi.remove(favoriteId);
      // Обновляем локальный список избранного
      favorites.value = favorites.value.filter(fav => fav.id !== favoriteId);
    } catch (err) {
      console.error("Ошибка при удалении из избранного:", err);
      throw err;
    }
  };

  const clearAllFavorites = async () => {
    try {
      await favoritesApi.clearAll();
      favorites.value = [];
    } catch (err) {
      console.error("Ошибка при очистке избранного:", err);
      // При ошибке перезагружаем данные с сервера для синхронизации
      await fetchFavorites();
      throw err;
    }
  };

  // Предоставляем глобальное состояние через provide
  provide('favorites', {
    favorites,
    fetchFavorites,
    toggleFavorite,
    removeFavorite,
    clearAllFavorites,
  });

  return {
    favorites,
    fetchFavorites,
    toggleFavorite,
    removeFavorite,
    clearAllFavorites,
  };
}
import { ref } from 'vue';
import { useApi } from './useApi';

interface Suggestion {
  author: string;
  productName: string;
  description: string;
}

export function useSuggestions() {
  const { suggestions: suggestionsApi } = useApi();
  
  const showSuggestionForm = ref(false);
  const isLoading = ref(false);
  const isSubmitted = ref(false);
  const showAuthModal = ref(false);
  const suggestionId = ref<number | null>(null);
  const suggestion = ref({
    author: '',
    productName: '',
    description: ''
  });

  const isValid = () => {
    return suggestion.value.author.trim() && 
           suggestion.value.productName.trim() && 
           suggestion.value.description.trim();
  };

  const isAuthenticated = () => {
    const userId = localStorage.getItem('userId');
    const authToken = localStorage.getItem('authToken');
    return !!(userId && authToken);
  };

  const resetForm = () => {
    suggestion.value = { author: '', productName: '', description: '' };
    isSubmitted.value = false;
    suggestionId.value = null;
    showAuthModal.value = false;
  };

  const submitSuggestion = async () => {
    if (!isValid()) return;
    
    // Проверка авторизации
    if (!isAuthenticated()) {
      showAuthModal.value = true;
      return;
    }
    
    isLoading.value = true;
    try {
      const response = await suggestionsApi.create({
        author: suggestion.value.author,
        productName: suggestion.value.productName,
        description: suggestion.value.description
      });
      suggestionId.value = response.data?.id || null;
      isSubmitted.value = true;
    } catch (error) {
      console.error('Ошибка отправки:', error);
      if (error.response?.status === 401 || error.response?.status === 403) {
        showAuthModal.value = true;
      } else {
        alert('Ошибка при отправке. Попробуйте позже.');
      }
    } finally {
      isLoading.value = false;
    }
  };

  return {
    showSuggestionForm,
    isLoading,
    isSubmitted,
    showAuthModal,
    suggestionId,
    suggestion,
    isValid,
    submitSuggestion,
    resetForm
  };
}
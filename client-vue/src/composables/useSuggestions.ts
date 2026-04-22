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
  const submitError = ref('');
  const validationErrors = ref({
    author: '',
    productName: '',
    description: ''
  });
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

  const validateFields = () => {
    validationErrors.value = {
      author: '',
      productName: '',
      description: ''
    };

    if (!suggestion.value.author.trim()) {
      validationErrors.value.author = 'Введите ваше имя';
    } else if (suggestion.value.author.trim().length < 2) {
      validationErrors.value.author = 'Имя должно содержать минимум 2 символа';
    }

    if (!suggestion.value.productName.trim()) {
      validationErrors.value.productName = 'Введите название товара';
    } else if (suggestion.value.productName.trim().length < 2) {
      validationErrors.value.productName = 'Название товара должно быть минимум 2 символа';
    }

    if (!suggestion.value.description.trim()) {
      validationErrors.value.description = 'Введите описание предложения';
    } else if (suggestion.value.description.trim().length < 5) {
      validationErrors.value.description = 'Описание должно содержать минимум 5 символов';
    }

    return !validationErrors.value.author && !validationErrors.value.productName && !validationErrors.value.description;
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
    submitError.value = '';
    validationErrors.value = {
      author: '',
      productName: '',
      description: ''
    };
  };

  const submitSuggestion = async () => {
    submitError.value = '';
    if (!validateFields()) return;
    
    if (!isAuthenticated()) {
      showAuthModal.value = true;
      return;
    }
    
    isLoading.value = true;
    try {
      const response = await suggestionsApi.create({
        author: suggestion.value.author.trim(),
        productName: suggestion.value.productName.trim(),
        description: suggestion.value.description.trim()
      });
      suggestionId.value = response.data?.id || null;
      isSubmitted.value = true;
    } catch (error) {
      console.error('Ошибка отправки:', error);
      const fieldErrors = error.response?.data?.errors;
      if (fieldErrors?.author?.length) {
        validationErrors.value.author = fieldErrors.author[0];
      }
      if (fieldErrors?.productName?.length) {
        validationErrors.value.productName = fieldErrors.productName[0];
      }
      if (fieldErrors?.description?.length) {
        validationErrors.value.description = fieldErrors.description[0];
      }
      if (error.response?.status === 401 || error.response?.status === 403) {
        showAuthModal.value = true;
      } else {
        submitError.value = error.response?.data?.message || 'Ошибка при отправке. Попробуйте позже.';
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
    submitError,
    validationErrors,
    suggestionId,
    suggestion,
    isValid,
    submitSuggestion,
    resetForm
  };
}
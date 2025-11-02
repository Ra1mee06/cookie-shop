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

  const resetForm = () => {
    suggestion.value = { author: '', productName: '', description: '' };
    isSubmitted.value = false;
  };

  const submitSuggestion = async () => {
    if (!isValid()) return;
    
    isLoading.value = true;
    try {
      await suggestionsApi.create({
        author: suggestion.value.author,
        productName: suggestion.value.productName,
        description: suggestion.value.description
      });
      isSubmitted.value = true;
    } catch (error) {
      console.error('Ошибка отправки:', error);
      alert('Ошибка при отправке. Попробуйте позже.');
    } finally {
      isLoading.value = false;
    }
  };

  return {
    showSuggestionForm,
    isLoading,
    isSubmitted,
    suggestion,
    isValid,
    submitSuggestion,
    resetForm
  };
}
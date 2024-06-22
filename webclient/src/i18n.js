import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

i18n.use(initReactI18next).init({
  fallbackLng: 'en',
  lng: 'en',
  resources: {
    en: {
      translation: {
        '1D': '1D',
        '1W': '1W',
        '1M': '1M',
        '3M': '3M',
        '1Y': '1Y',
        '5Y': '5Y',
        'MAX': 'MAX',
        'loading': 'Loading...',
        'error': 'Error fetching data.',
      },
    },
    es: {
      translation: {
        '1D': '1D',
        '1W': '1S',
        '1M': '1M',
        '3M': '3M',
        '1Y': '1A',
        '5Y': '5A',
        'MAX': 'MAX',
        'loading': 'Cargando...',
        'error': 'Error al obtener datos.',
      },
    },
  },
});

export default i18n;

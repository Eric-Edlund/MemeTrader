import { useQuery, useQueryClient } from 'react-query';
import getStockData from './api';

const useStockData = (stockId, startDate, endDate) => {
  const queryClient = useQueryClient();

  return useQuery(
    ['stockData', stockId, startDate, endDate],
    () => getStockData(stockId, startDate, endDate),
    {
      staleTime: 1000 * 60 * 5, // Cache for 5 minutes
      cacheTime: 1000 * 60 * 60, // Cache for 1 hour
    }
  );
};

export default useStockData;

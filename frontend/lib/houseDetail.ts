import { RecommendationHouseDetail } from '@/types/api';
import { fetchWithAuth } from './fetchWithAuth';


export async function fetchHouseDetail(houseId: number | string): Promise<RecommendationHouseDetail> {
  

  const response = await fetchWithAuth(`/api/recommend/houses/${houseId}`, {
    method: 'GET',
  });

  const data = await response.json().catch(() => null);

  
  

  if (!response.ok) {
    throw new Error(
      `매물 상세 조회 실패: ${data?.message || response.statusText}`,
    );
  }

  return data as RecommendationHouseDetail;
}

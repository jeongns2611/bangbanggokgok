import type { InfraCategory } from '@/components/features/search/HouseInfraFloatingMenu';
import { fetchWithAuth } from '@/lib/fetchWithAuth';

type HouseInfrastructureApiMarker = {
  id?: unknown;
  type?: unknown;
  name?: unknown;
  latitude?: unknown;
  longitude?: unknown;
};

type HouseInfrastructureApiResponse = {
  markers?: unknown;
};

export type HouseInfraMarker = {
  type: string;
  lat: number;
  lng: number;
};

const mapTypeToDisplay = (type: string) => {
  switch (type) {
    case 'CAFE':
      return '카페';
    case 'CONVENIENCE':
      return '편의점';
    case 'LAUNDRY':
    case 'LANUNDRY':
      return '세탁소';
    case 'HOSPITAL':
      return '병원';
    case 'PHARMACY':
      return '약국';
    case 'CCTV':
      return 'CCTV';
    case 'POLICE':
    case 'POLICE_STATION':
      return '경찰서';
    case 'BUS':
      return '버스';
    case 'SUBWAY':
      return '지하철';
    default:
      return '기타';
  }
};

const isConvenienceType = (type: string) => {
  return ['CONVENIENCE', 'LAUNDRY', 'LANUNDRY', 'CAFE'].includes(type);
};

const isMedicalType = (type: string) => {
  return ['HOSPITAL', 'PHARMACY'].includes(type);
};

const isTransportType = (type: string) => {
  return ['BUS', 'SUBWAY'].includes(type);
};

const matchesCategory = (category: InfraCategory, type: string) => {
  if (category === '편의시설') return isConvenienceType(type);
  if (category === '의료시설') return isMedicalType(type);
  return isTransportType(type);
};

export async function fetchHouseInfraMarkers(
  houseId: number,
  category: InfraCategory,
): Promise<HouseInfraMarker[]> {
  

  const response = await fetchWithAuth(`/api/houses/${houseId}/infrastructure`);
  const body = await response.json().catch(() => null);

  

  if (!response.ok) {
    
    throw new Error(body?.message || '인프라 조회 실패');
  }

  const payload: HouseInfrastructureApiResponse = body?.data ?? body ?? {};
  const markers = Array.isArray(payload.markers) ? payload.markers : [];

  

  const filteredMarkers = markers
    .filter(
      (item): item is HouseInfrastructureApiMarker =>
        typeof item === 'object' && item !== null,
    )
    .map((item) => {
      const type = typeof item.type === 'string' ? item.type.toUpperCase() : '';
      const lat = Number(item.latitude);
      const lng = Number(item.longitude);

      return {
        type,
        lat,
        lng,
      };
    })
    .filter(
      (item) =>
        Number.isFinite(item.lat) &&
        Number.isFinite(item.lng) &&
        matchesCategory(category, item.type),
    )
    .map((item) => ({
      type: mapTypeToDisplay(item.type),
      lat: item.lat,
      lng: item.lng,
    }));

  

  return filteredMarkers;
}

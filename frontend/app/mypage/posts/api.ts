import { fetchWithAuth } from '@/lib/fetchWithAuth';

export type PostedHouseImage = {
  image_url?: string | null;
  imageUrl?: string | null;
  is_thumbnail?: boolean | null;
  isThumbnail?: boolean | null;
};

export type PostedHouseDetail = {
  houseId: number;
  images?: PostedHouseImage[];
  lat?: number | null;
  lng?: number | null;
  sidoName?: string;
  sigunguName?: string;
  dongName?: string;
  address?: string;
  houseType?: string;
  rentType?: string;
  houseStatus?: string;
  floor?: string;
  floorSize?: number;
  buildYear?: number | null;
  deposit?: number;
  monthlyCost?: number;
  managementCost?: number | null;
  managementItems?: string | null;
  description?: string | null;
};

const KOREA_LAT_MIN = 33;
const KOREA_LAT_MAX = 39.8;
const KOREA_LNG_MIN = 124;
const KOREA_LNG_MAX = 132;

const isKoreanCoordinate = (lat: number, lng: number) => {
  return lat >= KOREA_LAT_MIN && lat <= KOREA_LAT_MAX && lng >= KOREA_LNG_MIN && lng <= KOREA_LNG_MAX;
};

const normalizeKoreanCoordinates = (detail: PostedHouseDetail): PostedHouseDetail => {
  if (detail.lat === undefined || detail.lat === null || detail.lng === undefined || detail.lng === null) {
    return detail;
  }

  if (isKoreanCoordinate(detail.lat, detail.lng)) {
    return detail;
  }

  if (isKoreanCoordinate(detail.lng, detail.lat)) {
    return {
      ...detail,
      lat: detail.lng,
      lng: detail.lat,
    };
  }

  return detail;
};

const unwrapHouseDetailResponse = (payload: unknown): PostedHouseDetail | null => {
  if (!payload || typeof payload !== 'object') return null;

  const typedPayload = payload as {
    data?: {
      data?: PostedHouseDetail;
    } | PostedHouseDetail;
  };

  const nestedData =
    typedPayload.data && typeof typedPayload.data === 'object' && 'data' in typedPayload.data
      ? (typedPayload.data as { data?: PostedHouseDetail }).data
      : undefined;

  return nestedData ?? (typedPayload.data as PostedHouseDetail) ?? (payload as PostedHouseDetail);
};

export async function fetchPostedHouseDetail(houseId: number): Promise<PostedHouseDetail> {
  const response = await fetchWithAuth(`/api/houses/${houseId}?t=${Date.now()}`, {
    cache: 'no-store',
  });

  const payload = await response.json().catch(() => null);

  

  if (!response.ok) {
    const errorMessage =
      payload && typeof payload === 'object' && 'message' in payload
        ? String((payload as { message?: string }).message)
        : '매물 정보를 불러오지 못했습니다.';

    throw new Error(errorMessage);
  }

  const detail = unwrapHouseDetailResponse(payload);

  

  if (!detail || !detail.houseId) {
    throw new Error('매물 상세 응답 형식이 올바르지 않습니다.');
  }

  const normalizedDetail = normalizeKoreanCoordinates(detail);

  

  return normalizedDetail;
}
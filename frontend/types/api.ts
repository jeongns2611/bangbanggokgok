
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T | null;
}


export interface TokenRefreshData {
  accessToken: string;
  refreshToken: string;
}

export interface AiChatRequest {
  query: string;
  sidoName?: string;
  sigunguName?: string;
  houseType?: string[];
  rentType?: string[];
  minMonthlyCost?: number;
  maxMonthlyCost?: number;
  minDeposit?: number;
  maxDeposit?: number;
  managementCost?: number;
  minFloorSize?: number;
  maxFloorSize?: number;
  floor?: string[];
}

export interface AiChatHistoryItem {
  id: number;
  userMessage: string;
  aiMessage: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface AiChatRecommendationHouse {
  houseId: number;
  thumbnailUrl?: string | null;
  lat?: number;
  lng?: number;
  sidoName?: string;
  sigunguName?: string;
  dongName?: string;
  houseType?: string;
  rentType?: string;
  houseStatus?: string;
  floor?: string;
  deposit?: number;
  monthlyCost?: number;
  floorSize?: number;
  isLiked?: boolean;
}

export interface AiChatResponseData {
  answer?: string;
  data?: AiChatRecommendationHouse[];
  [key: string]: unknown;
}


export interface PresignedUrlRequest {
  fileName: string;
  contentType: string;
  isThumbnail: boolean;
}


export interface PresignedUrlItem {
  url: string;
  objectKey: string;
  isThumbnail: boolean;
}


export interface HouseImagePayload {
  objectKey: string;
  isThumbnail: boolean;
}


export interface HouseImage {
  image_url: string | null;
  is_thumbnail: boolean | null;
}

export interface CommuteData {
  commuteTime: number;
  commuteDistance: number;
}

export interface InfraCount {
  convenienceStoreCount: number;
  laundryCount: number;
  cafeCount: number;
  hospitalCount: number;
  pharmacyCount: number;
  busStopCount: number;
}

export interface MinDist {
  convenienceDist: number;
  laundryDist: number;
  cafeDist: number;
  hospitalDist: number;
  pharmacyDist: number;
  subwayDist: number;
}

export interface DongStats {
  cctvCount: number;
  streetLightCount: number;
  safetyFacilityCount: number;
  safetyScore: number;
  avgMeatPrice: number;
  avgMealPrice: number;
}


export interface RegionTopCodeItem {
  codeId: number;
  codeName: string;
}

export interface RegionTopAppliedFilter {
  houseType: RegionTopCodeItem;
  rentType: RegionTopCodeItem;
  priceRangePolicy: string;
  areaRangePolicy: string;
  houseTypePolicy: string;
}

export interface RegionTopHousingCost {
  avgDeposit: number;
  avgMonthlyRent: number;
}

export interface RegionTopFacilityCount {
  convenienceStoreCount: number;
  cafeCount: number;
  hospitalCount: number;
  totalCount: number;
}

export interface RegionTopCommute {
  avgCommuteMinutes: number;
  avgCommuteDistanceKm: number;
}

export interface RegionTopLivingCost {
  foodCostIndex: number;
  selectedRegionFoodCost: number;
  seoulAvgFoodCost: number;
}

export interface RegionTopRecommendationItem {
  rank: number;
  sigunguName: string;
  regionCode: string;
  rentType: RegionTopCodeItem;
  hasMatchingHouses: boolean;
  matchingHouseCount: number;
  housingCost: RegionTopHousingCost;
  facilityCount: RegionTopFacilityCount;
  commute: RegionTopCommute;
  livingCost: RegionTopLivingCost;
}

export interface RegionTopMeta {
  topN: number;
  baseCodePolicy: string;
  excludedInactiveCodes: boolean;
}

export interface RegionTopRecommendationResponse {
  userNeedId: number;
  appliedFilter: RegionTopAppliedFilter;
  recommendations: RegionTopRecommendationItem[];
  meta: RegionTopMeta;
}

export interface RecommendationHouseDetail {
  data: {
    images: HouseImage[];
    aiMessage?: string | null;
    houseId: number;
    dong: string;
    isLiked?: boolean;
    houseType: string;
    rentType: string;
    houseStatus: string;
    deposit: number;
    monthlyCost: number;
    managementCost: number | null;
    managementItems: string | null;
    floorSize: number;
    buildYear: number | null;
    description: string;
    viewCount: number;
    floor: string;
    latitude: number;
    longitude: number;
    commuteData: CommuteData;
    infraCount: InfraCount;
    minDist: MinDist;
    dongStats: DongStats;
  };
}

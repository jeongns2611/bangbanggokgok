export type HouseImage = {
  image_url?: string | null;
  is_thumbnail?: boolean | null;
};

export type HouseListItem = {
  houseId: number;
  aiMessage?: string | null;
  houseType: string;
  rentType: string;
  deposit: number;
  monthlyCost: number;
  floor: string;
  floorSize: number;
  dong: string;
  houseStatus: string;
  isLiked: boolean;
  images: HouseImage[];
  lat: number;
  lng: number;
};

export type HouseDetail = {
  images: HouseImage[];
  houseId: number;
  lat?: number | null;
  lng?: number | null;
  sidoName?: string;
  sigunguName?: string;
  dongName?: string;
  dong?: string;
  houseType: string;
  rentType: string;
  houseStatus: string;
  isLiked: boolean;
  address?: string;
  deposit: number;
  monthlyCost: number;
  managementCost?: number | null;
  managementItems?: string | null;
  floorSize: number;
  buildYear?: number | null;
  description?: string | null;
  floor: string;
  commuteData?: {
    commuteTime: number;
    commuteDistance: number;
  } | null;
  dongStats: {
    cctvCount: number;
    streetLightCount: number;
    safetyFacilityCount: number;
    safetyScore: number;
    avgMeatPrice: number;
    avgMealPrice: number;
  };
  infraCount: {
    convenienceStoreCount: number;
    laundryCount: number;
    cafeCount: number;
    hospitalCount: number;
    pharmacyCount: number;
    busStopCount: number;
  };
  minDist: {
    convenienceDist: number;
    laundryDist: number;
    cafeDist: number;
    hospitalDist: number;
    pharmacyDist: number;
    subwayDist: number;
  };
};

export const MOCK_HOUSE_LIST: HouseListItem[] = [
  {
    houseId: 501,
    houseType: '단독/다가구',
    rentType: '월세',
    deposit: 1000,
    monthlyCost: 50,
    floor: '1층',
    floorSize: 35.12,
    dong: '서울특별시 용산구 한남동',
    houseStatus: '거래 가능',
    isLiked: false,
    lat: 37.5349,
    lng: 127.0026,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
  },
  {
    houseId: 502,
    houseType: '오피스텔',
    rentType: '전세',
    deposit: 22000,
    monthlyCost: 0,
    floor: '8층',
    floorSize: 28.5,
    dong: '서울특별시 강남구 역삼동',
    houseStatus: '거래 가능',
    isLiked: false,
    lat: 37.4979,
    lng: 127.0276,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
  },
  {
    houseId: 503,
    houseType: '빌라',
    rentType: '월세',
    deposit: 500,
    monthlyCost: 65,
    floor: '3층',
    floorSize: 22.8,
    dong: '서울특별시 마포구 성산동',
    houseStatus: '거래 가능',
    isLiked: false,
    lat: 37.5636,
    lng: 126.9085,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
  },
];

export const MOCK_HOUSE_DETAILS: HouseDetail[] = [
  {
    houseId: 501,
    dong: '서울특별시 용산구 독서당로 111 (한남동 810)',
    houseType: '단독/다가구',
    rentType: '월세',
    houseStatus: '거래 가능',
    isLiked: false,
    deposit: 1000,
    monthlyCost: 50,
    managementCost: 10,
    managementItems: '수도, 인터넷 포함',
    floorSize: 35.12,
    buildYear: 2022,
    description: '채광 좋고 역세권인 풀옵션 원룸입니다.',
    floor: '1층',
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    dongStats: {
      cctvCount: 145,
      streetLightCount: 145,
      safetyFacilityCount: 22,
      safetyScore: 86,
      avgMeatPrice: 18500,
      avgMealPrice: 9500,
    },
    infraCount: {
      convenienceStoreCount: 8,
      laundryCount: 3,
      cafeCount: 12,
      hospitalCount: 5,
      pharmacyCount: 4,
      busStopCount: 6,
    },
    minDist: {
      convenienceDist: 120,
      laundryDist: 350,
      cafeDist: 80,
      hospitalDist: 450,
      pharmacyDist: 30,
      subwayDist: 600,
    },
  },
  {
    houseId: 502,
    dong: '서울특별시 강남구 테헤란로 101',
    houseType: '오피스텔',
    rentType: '전세',
    houseStatus: '거래 가능',
    isLiked: false,
    deposit: 22000,
    monthlyCost: 0,
    managementCost: 12,
    managementItems: '수도, 인터넷, 청소비 포함',
    floorSize: 28.5,
    buildYear: 2021,
    description: '강남역 도보권, 보안 좋은 신축 오피스텔입니다.',
    floor: '8층',
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    dongStats: {
      cctvCount: 180,
      streetLightCount: 160,
      safetyFacilityCount: 28,
      safetyScore: 91,
      avgMeatPrice: 21000,
      avgMealPrice: 11000,
    },
    infraCount: {
      convenienceStoreCount: 11,
      laundryCount: 2,
      cafeCount: 20,
      hospitalCount: 7,
      pharmacyCount: 6,
      busStopCount: 9,
    },
    minDist: {
      convenienceDist: 50,
      laundryDist: 240,
      cafeDist: 60,
      hospitalDist: 300,
      pharmacyDist: 110,
      subwayDist: 200,
    },
  },
  {
    houseId: 503,
    dong: '서울특별시 마포구 월드컵북로 77',
    houseType: '빌라',
    rentType: '월세',
    houseStatus: '거래 가능',
    isLiked: false,
    deposit: 500,
    monthlyCost: 65,
    managementCost: 7,
    managementItems: '인터넷 포함',
    floorSize: 22.8,
    buildYear: 2019,
    description: '조용한 주택가에 위치한 실속형 원룸입니다.',
    floor: '3층',
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    dongStats: {
      cctvCount: 132,
      streetLightCount: 121,
      safetyFacilityCount: 18,
      safetyScore: 79,
      avgMeatPrice: 17000,
      avgMealPrice: 8900,
    },
    infraCount: {
      convenienceStoreCount: 6,
      laundryCount: 4,
      cafeCount: 9,
      hospitalCount: 3,
      pharmacyCount: 3,
      busStopCount: 5,
    },
    minDist: {
      convenienceDist: 140,
      laundryDist: 210,
      cafeDist: 110,
      hospitalDist: 530,
      pharmacyDist: 260,
      subwayDist: 720,
    },
  },
];

export const getMockHouseDetailById = async (
  houseId: number,
): Promise<HouseDetail | null> => {
  await new Promise((resolve) => setTimeout(resolve, 300));
  return MOCK_HOUSE_DETAILS.find((house) => house.houseId === houseId) ?? null;
};
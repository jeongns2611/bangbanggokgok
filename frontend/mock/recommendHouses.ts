export type RecommendHouseImage = {
  image_url: string;
  is_thumbnail: boolean;
};

type RecommendCommuteData = {
  commuteTime: number;
  commuteDistance: number;
};

type RecommendDongStats = {
  cctvCount: number;
  streetLightCount: number;
  safetyFacilityCount: number;
  safetyScore: number;
  avgMeatPrice: number;
  avgMealPrice: number;
};

type RecommendInfraCount = {
  convenienceStoreCount: number;
  laundryCount: number;
  cafeCount: number;
  hospitalCount: number;
  pharmacyCount: number;
  busStopCount: number;
};

type RecommendMinDist = {
  convenienceDist: number;
  laundryDist: number;
  cafeDist: number;
  hospitalDist: number;
  pharmacyDist: number;
  subwayDist: number;
};

export type RecommendHouseDetail = {
  houseId: number;
  dong: string;
  houseType: string;
  rentType: string;
  houseStatus: string;
  deposit: number;
  monthlyCost: number;
  managementCost: number;
  managementItems: string;
  floorSize: number;
  buildYear: number;
  description: string;
  floor: string;
  viewCount: number;
  lat: number;
  lng: number;
  images: RecommendHouseImage[];
  commuteData: RecommendCommuteData;
  recommendReasons: string[];
  dongStats: RecommendDongStats;
  infraCount: RecommendInfraCount;
  minDist: RecommendMinDist;
};

export type RecommendHouseListItem = {
  houseId: number;
  houseType: string;
  rentType: string;
  deposit: number;
  monthlyCost: number;
  floor: string;
  floorSize: number;
  dong: string;
  houseStatus: string;
  images: RecommendHouseImage[];
  lat: number;
  lng: number;
};

type GetMockRecommendedHousesParams = {
  conditionId: number;
  lastHouseId?: number | null;
  pageSize?: number;
};

type GetMockRecommendedHousesResponse = {
  items: RecommendHouseListItem[];
  hasNext: boolean;
};

const RECOMMEND_HOUSE_DETAILS: RecommendHouseDetail[] = [
  {
    houseId: 901,
    dong: '서울특별시 강남구 역삼동',
    houseType: '오피스텔',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 1000,
    monthlyCost: 72,
    managementCost: 9,
    managementItems: '수도, 인터넷',
    floorSize: 27.8,
    buildYear: 2022,
    description:
      '강남역 접근성이 좋고 보안이 우수한 신축급 오피스텔입니다. 직장인 1인 거주에 적합한 구조예요.',
    floor: '10층',
    viewCount: 321,
    lat: 37.4982,
    lng: 127.0286,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1484154218962-a197022b5858?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 18,
      commuteDistance: 6.1,
    },
    recommendReasons: [
      '강남역 도보권으로 출퇴근 시간이 짧아요.',
      '카페와 편의점, 병원 등 생활 인프라가 풍부해요.',
      '치안 점수와 CCTV 수치가 높아 안심 거주에 유리해요.',
    ],
    dongStats: {
      cctvCount: 186,
      streetLightCount: 169,
      safetyFacilityCount: 31,
      safetyScore: 92,
      avgMeatPrice: 21000,
      avgMealPrice: 11000,
    },
    infraCount: {
      convenienceStoreCount: 12,
      laundryCount: 3,
      cafeCount: 24,
      hospitalCount: 8,
      pharmacyCount: 6,
      busStopCount: 9,
    },
    minDist: {
      convenienceDist: 55,
      laundryDist: 180,
      cafeDist: 45,
      hospitalDist: 220,
      pharmacyDist: 130,
      subwayDist: 210,
    },
  },
  {
    houseId: 902,
    dong: '서울특별시 용산구 한남동',
    houseType: '단독/다가구',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 800,
    monthlyCost: 55,
    managementCost: 7,
    managementItems: '인터넷',
    floorSize: 33.1,
    buildYear: 2021,
    description:
      '주택가 안쪽에 위치해 조용하고 채광이 좋은 원룸형 매물입니다. 생활권 밸런스가 괜찮아요.',
    floor: '2층',
    viewCount: 198,
    lat: 37.5355,
    lng: 127.0008,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 29,
      commuteDistance: 9.4,
    },
    recommendReasons: [
      '월세 부담이 비교적 적고 면적이 넉넉한 편이에요.',
      '약국과 편의점이 가까워 자취 생활 동선이 편리해요.',
      '안전 점수가 높고 가로등 수가 많아 야간 귀가 부담이 적어요.',
    ],
    dongStats: {
      cctvCount: 152,
      streetLightCount: 148,
      safetyFacilityCount: 24,
      safetyScore: 87,
      avgMeatPrice: 18800,
      avgMealPrice: 9700,
    },
    infraCount: {
      convenienceStoreCount: 8,
      laundryCount: 3,
      cafeCount: 11,
      hospitalCount: 5,
      pharmacyCount: 4,
      busStopCount: 6,
    },
    minDist: {
      convenienceDist: 120,
      laundryDist: 260,
      cafeDist: 95,
      hospitalDist: 410,
      pharmacyDist: 85,
      subwayDist: 590,
    },
  },
  {
    houseId: 903,
    dong: '서울특별시 마포구 서교동',
    houseType: '빌라',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 500,
    monthlyCost: 62,
    managementCost: 6,
    managementItems: '인터넷, 공동청소',
    floorSize: 24.4,
    buildYear: 2020,
    description:
      '홍대 생활권을 누릴 수 있는 실속형 빌라 매물입니다. 카페, 편의시설 접근성이 좋아요.',
    floor: '3층',
    viewCount: 276,
    lat: 37.5557,
    lng: 126.9235,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1505693416388-ac5ce068fe85?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 32,
      commuteDistance: 10.7,
    },
    recommendReasons: [
      '카페와 편의점 등 생활 편의시설이 매우 가까워요.',
      '초기 보증금 부담이 낮아 사회초년생에게 적합해요.',
      '지하철 접근성이 나쁘지 않아 이동이 편리해요.',
    ],
    dongStats: {
      cctvCount: 141,
      streetLightCount: 134,
      safetyFacilityCount: 20,
      safetyScore: 82,
      avgMeatPrice: 17900,
      avgMealPrice: 9200,
    },
    infraCount: {
      convenienceStoreCount: 10,
      laundryCount: 5,
      cafeCount: 22,
      hospitalCount: 4,
      pharmacyCount: 3,
      busStopCount: 7,
    },
    minDist: {
      convenienceDist: 80,
      laundryDist: 170,
      cafeDist: 40,
      hospitalDist: 480,
      pharmacyDist: 260,
      subwayDist: 430,
    },
  },
  {
    houseId: 904,
    dong: '서울특별시 성동구 성수동1가',
    houseType: '오피스텔',
    rentType: '전세',
    houseStatus: '거래 가능',
    deposit: 21000,
    monthlyCost: 0,
    managementCost: 11,
    managementItems: '수도, 인터넷, 경비',
    floorSize: 29.6,
    buildYear: 2023,
    description:
      '성수역 인근 신축 오피스텔로, 전세 선호 사용자에게 적합한 안정적인 매물입니다.',
    floor: '12층',
    viewCount: 254,
    lat: 37.5445,
    lng: 127.0557,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 24,
      commuteDistance: 8.3,
    },
    recommendReasons: [
      '전세 매물이라 월 고정지출 부담을 줄일 수 있어요.',
      '성수역과 가까워 대중교통 이동이 편리해요.',
      '신축급 매물이라 주거 만족도가 높을 가능성이 커요.',
    ],
    dongStats: {
      cctvCount: 167,
      streetLightCount: 152,
      safetyFacilityCount: 27,
      safetyScore: 89,
      avgMeatPrice: 19500,
      avgMealPrice: 9800,
    },
    infraCount: {
      convenienceStoreCount: 9,
      laundryCount: 2,
      cafeCount: 19,
      hospitalCount: 6,
      pharmacyCount: 4,
      busStopCount: 8,
    },
    minDist: {
      convenienceDist: 70,
      laundryDist: 240,
      cafeDist: 55,
      hospitalDist: 310,
      pharmacyDist: 180,
      subwayDist: 260,
    },
  },
  {
    houseId: 905,
    dong: '서울특별시 동작구 사당동',
    houseType: '단독/다가구',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 700,
    monthlyCost: 48,
    managementCost: 5,
    managementItems: '인터넷',
    floorSize: 30.8,
    buildYear: 2018,
    description:
      '사당역 생활권에 위치한 가성비 좋은 다가구 매물입니다. 공간 활용도가 높은 편이에요.',
    floor: '2층',
    viewCount: 187,
    lat: 37.4847,
    lng: 126.9816,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1448630360428-65456885c650?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 27,
      commuteDistance: 9.1,
    },
    recommendReasons: [
      '월세가 상대적으로 낮아 예산 관리에 유리해요.',
      '사당역 생활권이라 교통 접근성이 좋아요.',
      '면적이 넉넉해 답답함이 덜한 편이에요.',
    ],
    dongStats: {
      cctvCount: 149,
      streetLightCount: 143,
      safetyFacilityCount: 21,
      safetyScore: 84,
      avgMeatPrice: 17600,
      avgMealPrice: 9000,
    },
    infraCount: {
      convenienceStoreCount: 7,
      laundryCount: 4,
      cafeCount: 10,
      hospitalCount: 4,
      pharmacyCount: 4,
      busStopCount: 7,
    },
    minDist: {
      convenienceDist: 130,
      laundryDist: 220,
      cafeDist: 140,
      hospitalDist: 500,
      pharmacyDist: 170,
      subwayDist: 390,
    },
  },
  {
    houseId: 906,
    dong: '서울특별시 광진구 자양동',
    houseType: '빌라',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 600,
    monthlyCost: 58,
    managementCost: 6,
    managementItems: '수도',
    floorSize: 25.9,
    buildYear: 2020,
    description:
      '건대입구 생활권 접근이 좋은 실속형 빌라입니다. 자취 초반에 무난하게 선택하기 좋아요.',
    floor: '4층',
    viewCount: 163,
    lat: 37.5341,
    lng: 127.0814,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1460317442991-0ec209397118?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 31,
      commuteDistance: 10.2,
    },
    recommendReasons: [
      '보증금과 월세가 과하지 않아 진입 부담이 적어요.',
      '카페, 편의점이 가깝고 생활권이 활발해요.',
      '버스정류장 접근성이 좋아 이동 선택지가 많아요.',
    ],
    dongStats: {
      cctvCount: 138,
      streetLightCount: 129,
      safetyFacilityCount: 19,
      safetyScore: 80,
      avgMeatPrice: 18100,
      avgMealPrice: 9100,
    },
    infraCount: {
      convenienceStoreCount: 8,
      laundryCount: 4,
      cafeCount: 14,
      hospitalCount: 3,
      pharmacyCount: 3,
      busStopCount: 9,
    },
    minDist: {
      convenienceDist: 90,
      laundryDist: 210,
      cafeDist: 85,
      hospitalDist: 540,
      pharmacyDist: 250,
      subwayDist: 620,
    },
  },
  {
    houseId: 907,
    dong: '서울특별시 서대문구 연희동',
    houseType: '단독/다가구',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 1000,
    monthlyCost: 52,
    managementCost: 4,
    managementItems: '인터넷',
    floorSize: 31.5,
    buildYear: 2017,
    description:
      '조용한 주거 환경과 적당한 생활 편의성을 함께 갖춘 매물입니다. 안정감 있는 동네 분위기가 장점이에요.',
    floor: '1.5층',
    viewCount: 111,
    lat: 37.5703,
    lng: 126.9326,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1502005229762-cf1b2da7c5d6?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 36,
      commuteDistance: 12.4,
    },
    recommendReasons: [
      '주거지 분위기가 비교적 조용해 생활 만족도가 높을 수 있어요.',
      '면적이 넉넉하고 월세도 과도하지 않아요.',
      '안전 점수가 안정적인 수준이라 균형 잡힌 선택지예요.',
    ],
    dongStats: {
      cctvCount: 133,
      streetLightCount: 128,
      safetyFacilityCount: 18,
      safetyScore: 81,
      avgMeatPrice: 17400,
      avgMealPrice: 8900,
    },
    infraCount: {
      convenienceStoreCount: 6,
      laundryCount: 3,
      cafeCount: 9,
      hospitalCount: 3,
      pharmacyCount: 2,
      busStopCount: 6,
    },
    minDist: {
      convenienceDist: 150,
      laundryDist: 260,
      cafeDist: 170,
      hospitalDist: 560,
      pharmacyDist: 340,
      subwayDist: 880,
    },
  },
  {
    houseId: 908,
    dong: '서울특별시 송파구 잠실동',
    houseType: '오피스텔',
    rentType: '전세',
    houseStatus: '거래 가능',
    deposit: 24000,
    monthlyCost: 0,
    managementCost: 13,
    managementItems: '수도, 인터넷, 경비, 청소',
    floorSize: 30.4,
    buildYear: 2021,
    description:
      '잠실권 생활 인프라를 누릴 수 있는 전세 오피스텔입니다. 대중교통과 상권 접근성이 좋아요.',
    floor: '15층',
    viewCount: 301,
    lat: 37.5112,
    lng: 127.0847,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1505692952047-1a78307da8f2?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 26,
      commuteDistance: 9.0,
    },
    recommendReasons: [
      '전세 선호 사용자에게 적합한 안정적인 조건이에요.',
      '잠실 생활권으로 인프라 밀도가 높아요.',
      '버스와 지하철 접근성이 모두 괜찮아 이동이 편리해요.',
    ],
    dongStats: {
      cctvCount: 175,
      streetLightCount: 162,
      safetyFacilityCount: 29,
      safetyScore: 90,
      avgMeatPrice: 20800,
      avgMealPrice: 10500,
    },
    infraCount: {
      convenienceStoreCount: 11,
      laundryCount: 2,
      cafeCount: 18,
      hospitalCount: 7,
      pharmacyCount: 5,
      busStopCount: 10,
    },
    minDist: {
      convenienceDist: 60,
      laundryDist: 250,
      cafeDist: 75,
      hospitalDist: 290,
      pharmacyDist: 150,
      subwayDist: 310,
    },
  },
  {
    houseId: 909,
    dong: '서울특별시 관악구 봉천동',
    houseType: '원룸',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 300,
    monthlyCost: 43,
    managementCost: 5,
    managementItems: '인터넷',
    floorSize: 21.7,
    buildYear: 2019,
    description:
      '초기 자금 부담을 줄이고 싶은 사용자에게 적합한 실속형 원룸입니다. 기본 생활 인프라는 무난해요.',
    floor: '3층',
    viewCount: 144,
    lat: 37.4821,
    lng: 126.9527,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 41,
      commuteDistance: 13.8,
    },
    recommendReasons: [
      '보증금이 낮아 초기 자금 부담이 적어요.',
      '기본 생활 인프라가 무난하게 갖춰져 있어요.',
      '사회초년생이 시작하기에 가격 접근성이 좋아요.',
    ],
    dongStats: {
      cctvCount: 128,
      streetLightCount: 122,
      safetyFacilityCount: 17,
      safetyScore: 77,
      avgMeatPrice: 16800,
      avgMealPrice: 8500,
    },
    infraCount: {
      convenienceStoreCount: 7,
      laundryCount: 4,
      cafeCount: 8,
      hospitalCount: 3,
      pharmacyCount: 3,
      busStopCount: 7,
    },
    minDist: {
      convenienceDist: 110,
      laundryDist: 190,
      cafeDist: 130,
      hospitalDist: 620,
      pharmacyDist: 280,
      subwayDist: 760,
    },
  },
  {
    houseId: 910,
    dong: '서울특별시 영등포구 문래동',
    houseType: '오피스텔',
    rentType: '월세',
    houseStatus: '거래 가능',
    deposit: 1200,
    monthlyCost: 68,
    managementCost: 8,
    managementItems: '수도, 인터넷',
    floorSize: 26.8,
    buildYear: 2022,
    description:
      '문래역 접근성이 괜찮고 깔끔한 컨디션을 갖춘 오피스텔입니다. 직주근접 선호 사용자에게 적합해요.',
    floor: '9층',
    viewCount: 223,
    lat: 37.5166,
    lng: 126.8948,
    images: [
      {
        image_url:
          'https://images.unsplash.com/photo-1501183638710-841dd1904471?auto=format&fit=crop&w=1200&q=80',
        is_thumbnail: true,
      },
    ],
    commuteData: {
      commuteTime: 23,
      commuteDistance: 7.9,
    },
    recommendReasons: [
      '출퇴근 시간이 짧고 지하철 접근성이 좋아요.',
      '오피스텔이라 관리와 보안 측면에서 안정감이 있어요.',
      '생활 편의시설이 적절히 갖춰져 있어 균형이 좋아요.',
    ],
    dongStats: {
      cctvCount: 158,
      streetLightCount: 149,
      safetyFacilityCount: 25,
      safetyScore: 86,
      avgMeatPrice: 18400,
      avgMealPrice: 9300,
    },
    infraCount: {
      convenienceStoreCount: 9,
      laundryCount: 3,
      cafeCount: 13,
      hospitalCount: 5,
      pharmacyCount: 4,
      busStopCount: 8,
    },
    minDist: {
      convenienceDist: 75,
      laundryDist: 210,
      cafeDist: 90,
      hospitalDist: 360,
      pharmacyDist: 170,
      subwayDist: 340,
    },
  },
];

export const RECOMMEND_HOUSE_LIST: RecommendHouseListItem[] =
  RECOMMEND_HOUSE_DETAILS.map((house) => ({
    houseId: house.houseId,
    houseType: house.houseType,
    rentType: house.rentType,
    deposit: house.deposit,
    monthlyCost: house.monthlyCost,
    floor: house.floor,
    floorSize: house.floorSize,
    dong: house.dong,
    houseStatus: house.houseStatus,
    images: house.images,
    lat: house.lat,
    lng: house.lng,
  }));

const getOrderedRecommendHouses = (conditionId: number) => {
  const houses = [...RECOMMEND_HOUSE_LIST];

  switch (conditionId) {
    case 2:
      return houses.sort((a, b) => {
        if (a.monthlyCost !== b.monthlyCost) {
          return a.monthlyCost - b.monthlyCost;
        }
        return a.houseId - b.houseId;
      });

    case 3:
      return houses.sort((a, b) => {
        if (a.deposit !== b.deposit) {
          return a.deposit - b.deposit;
        }
        return a.houseId - b.houseId;
      });

    case 4:
      return houses.sort((a, b) => {
        if (a.floorSize !== b.floorSize) {
          return b.floorSize - a.floorSize;
        }
        return a.houseId - b.houseId;
      });

    case 1:
    default:
      return houses.sort((a, b) => a.houseId - b.houseId);
  }
};

export const getMockRecommendedHouses = ({
  conditionId,
  lastHouseId = null,
  pageSize = 10,
}: GetMockRecommendedHousesParams): GetMockRecommendedHousesResponse => {
  const ordered = getOrderedRecommendHouses(conditionId);

  const startIndex =
    lastHouseId == null
      ? 0
      : ordered.findIndex((house) => house.houseId === lastHouseId) + 1;

  const safeStartIndex = startIndex < 0 ? 0 : startIndex;
  const items = ordered.slice(safeStartIndex, safeStartIndex + pageSize);
  const hasNext = safeStartIndex + pageSize < ordered.length;

  return {
    items,
    hasNext,
  };
};

export const getMockRecommendedHouseDetailById = async (
  houseId: number,
): Promise<RecommendHouseDetail | null> => {
  await new Promise((resolve) => setTimeout(resolve, 250));

  return (
    RECOMMEND_HOUSE_DETAILS.find((house) => house.houseId === houseId) ?? null
  );
};
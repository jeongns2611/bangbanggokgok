import type { InfraCategory } from '@/components/features/search/HouseInfraFloatingMenu';

export type MockInfraItemType =
  | '카페'
  | '세탁소'
  | '편의점'
  | '병원'
  | '약국'
  | '버스'
  | '지하철';

export type MockInfraItem = {
  type: MockInfraItemType;
  lat: string;
  lng: string;
};

export type MockHouseInfraResponse = {
  infraType: InfraCategory;
  infras: MockInfraItem[];
};

export const MOCK_HOUSE_INFRA: Record<
  number,
  Record<InfraCategory, MockHouseInfraResponse>
> = {
  501: {
    편의시설: {
      infraType: '편의시설',
      infras: [
        { type: '카페', lat: '37.4986', lng: '127.0267' },
        { type: '편의점', lat: '37.4971', lng: '127.0288' },
        { type: '세탁소', lat: '37.4968', lng: '127.0263' },
      ],
    },
    의료시설: {
      infraType: '의료시설',
      infras: [
        { type: '병원', lat: '37.4982', lng: '127.0291' },
        { type: '약국', lat: '37.4969', lng: '127.0281' },
      ],
    },
    교통시설: {
      infraType: '교통시설',
      infras: [
        { type: '버스', lat: '37.4989', lng: '127.0284' },
        { type: '지하철', lat: '37.4967', lng: '127.0294' },
      ],
    },
  },
  502: {
    편의시설: {
      infraType: '편의시설',
      infras: [
        { type: '카페', lat: '37.5001', lng: '127.0294' },
        { type: '편의점', lat: '37.4990', lng: '127.0314' },
        { type: '세탁소', lat: '37.4987', lng: '127.0301' },
      ],
    },
    의료시설: {
      infraType: '의료시설',
      infras: [
        { type: '병원', lat: '37.5002', lng: '127.0311' },
        { type: '약국', lat: '37.4992', lng: '127.0296' },
      ],
    },
    교통시설: {
      infraType: '교통시설',
      infras: [
        { type: '버스', lat: '37.5004', lng: '127.0302' },
        { type: '지하철', lat: '37.4988', lng: '127.0321' },
      ],
    },
  },
  503: {
    편의시설: {
      infraType: '편의시설',
      infras: [
        { type: '카페', lat: '37.4962', lng: '127.0314' },
        { type: '편의점', lat: '37.4952', lng: '127.0329' },
        { type: '세탁소', lat: '37.4956', lng: '127.0309' },
      ],
    },
    의료시설: {
      infraType: '의료시설',
      infras: [
        { type: '병원', lat: '37.4964', lng: '127.0331' },
        { type: '약국', lat: '37.4951', lng: '127.0318' },
      ],
    },
    교통시설: {
      infraType: '교통시설',
      infras: [
        { type: '버스', lat: '37.4961', lng: '127.0325' },
        { type: '지하철', lat: '37.4949', lng: '127.0336' },
      ],
    },
  },
};

export function getMockHouseInfra(
  houseId: number,
  infraType: InfraCategory,
): MockHouseInfraResponse | null {
  return MOCK_HOUSE_INFRA[houseId]?.[infraType] ?? null;
}
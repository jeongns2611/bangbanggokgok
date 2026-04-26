import { NextRequest, NextResponse } from 'next/server';

type UnknownRecord = Record<string, unknown>;

interface UserNeedCreatePayload {
  needName: string;
  lat: number;
  lng: number;
  maxCommuteTime: number;
  rentType: string;
  minDeposit: number;
  minMonthlyRent: number;
  housingTypes: string[];
  minSize: number;
  lifestyleTags: string[];
  floors: string[];
  sidoName?: string;
  sigunguName?: string;
  maxDeposit?: number | null;
  maxMonthlyRent?: number | null;
}

function toFiniteNumber(value: unknown): number | null {
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

function toTrimmedStringArray(value: unknown): string[] {
  if (!Array.isArray(value)) {
    return [];
  }

  return value
    .map((item) => (typeof item === 'string' ? item.trim() : ''))
    .filter((item) => item.length > 0);
}

function normalizeUserNeedCreatePayload(body: unknown): UserNeedCreatePayload {
  if (typeof body !== 'object' || body === null) {
    throw new Error('요청 본문이 올바르지 않습니다.');
  }

  const input = body as UnknownRecord;

  const needName = typeof input.needName === 'string' ? input.needName.trim() : '';
  const lat = toFiniteNumber(input.lat);
  const lng = toFiniteNumber(input.lng);
  const maxCommuteTime = toFiniteNumber(input.maxCommuteTime);
  const rentType = typeof input.rentType === 'string' ? input.rentType.trim() : '';
  const minDeposit = toFiniteNumber(input.minDeposit);
  const minMonthlyRent = toFiniteNumber(input.minMonthlyRent);
  const minSize = toFiniteNumber(input.minSize);

  const housingTypes = toTrimmedStringArray(input.housingTypes);
  const lifestyleTags = toTrimmedStringArray(input.lifestyleTags);
  const floors = toTrimmedStringArray(input.floors);

  const sidoName = typeof input.sidoName === 'string' ? input.sidoName.trim() : '';
  const sigunguName = typeof input.sigunguName === 'string' ? input.sigunguName.trim() : '';

  if (!needName) {
    throw new Error('needName은 필수입니다.');
  }
  if (lat === null || lng === null) {
    throw new Error('lat, lng는 숫자여야 합니다.');
  }
  if (maxCommuteTime === null) {
    throw new Error('maxCommuteTime은 숫자여야 합니다.');
  }
  if (!rentType) {
    throw new Error('rentType은 필수입니다.');
  }
  if (minDeposit === null || minMonthlyRent === null) {
    throw new Error('minDeposit, minMonthlyRent는 숫자여야 합니다.');
  }
  if (minSize === null || minSize <= 0) {
    throw new Error('minSize는 0보다 큰 숫자여야 합니다.');
  }
  if (housingTypes.length === 0) {
    throw new Error('housingTypes는 1개 이상이어야 합니다.');
  }
  if (lifestyleTags.length === 0) {
    throw new Error('lifestyleTags는 1개 이상이어야 합니다.');
  }
  if (floors.length === 0) {
    throw new Error('floors는 1개 이상이어야 합니다.');
  }

  return {
    needName,
    lat,
    lng,
    ...(sidoName ? { sidoName } : {}),
    ...(sigunguName ? { sigunguName } : {}),
    maxCommuteTime,
    rentType,
    minDeposit,
    minMonthlyRent,
    ...(toFiniteNumber(input.maxDeposit) !== null
      ? { maxDeposit: toFiniteNumber(input.maxDeposit) }
      : {}),
    ...(toFiniteNumber(input.maxMonthlyRent) !== null
      ? { maxMonthlyRent: toFiniteNumber(input.maxMonthlyRent) }
      : {}),
    housingTypes,
    minSize,
    lifestyleTags,
    floors,
  };
}

export async function POST(req: NextRequest) {
  try {
    const rawBody = await req.json();
    const body = normalizeUserNeedCreatePayload(rawBody);
    const authHeader = req.headers.get('authorization');
    
    const backendUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs`;
    
    
    
    
    
    
    
    
    
    
    
    
    

    const backendRes = await fetch(backendUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(authHeader ? { 'Authorization': authHeader } : {}),
      },
      body: JSON.stringify(body),
    });

    
    
    
    
    
    

    let data;
    let responseText = '';
    try {
      responseText = await backendRes.text();
      data = JSON.parse(responseText);
      
      
    } catch (parseError) {
      
      
      
      
      data = { message: responseText || '조건 추가 실패' };
    }
    
    

    if (!backendRes.ok) {
      return NextResponse.json(data ?? { message: '조건 추가 실패' }, { status: backendRes.status });
    }

    return NextResponse.json(data, { status: backendRes.status });
  } catch (error) {
    
    
    
    
    const errorMessage = error instanceof Error ? error.message : '알 수 없는 에러';
    
    
    
    
    return NextResponse.json({ message: `잘못된 요청: ${errorMessage}` }, { status: 400 });
  }
}

import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

type RawCondition = Record<string, unknown>;
type UnknownRecord = Record<string, unknown>;

interface UserNeedCreatePayload {
  needName: string;
  lat: number;
  lng: number;
  targetAddress?: string;
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

function maskAuthorizationHeader(value: string | null) {
  if (!value) {
    return null;
  }

  if (value.length <= 20) {
    return value;
  }

  return `${value.slice(0, 16)}...${value.slice(-8)}`;
}

function normalizeCondition(condition: RawCondition) {
  const userNeed =
    typeof condition.userNeed === 'object' && condition.userNeed !== null
      ? condition.userNeed
      : typeof condition.uesrNeed === 'object' && condition.uesrNeed !== null
        ? condition.uesrNeed
        : null;

  const isDefaultNeed =
    typeof condition.isDefaultNeed === 'boolean'
      ? condition.isDefaultNeed
      : typeof condition.isDefualtNeed === 'boolean'
        ? condition.isDefualtNeed
        : false;

  return {
    ...condition,
    userNeed,
    uesrNeed: userNeed,
    isDefaultNeed,
    isDefualtNeed: isDefaultNeed,
  };
}

function normalizeConditionsResponse(data: unknown) {
  if (typeof data !== 'object' || data === null) {
    return data;
  }

  const response = data as Record<string, unknown>;

  if (typeof response.data !== 'object' || response.data === null) {
    return data;
  }

  const responseData = response.data as Record<string, unknown>;

  if (!Array.isArray(responseData.conditions)) {
    return data;
  }

  return {
    ...response,
    data: {
      ...responseData,
      conditions: responseData.conditions.map((condition) =>
        typeof condition === 'object' && condition !== null
          ? normalizeCondition(condition as RawCondition)
          : condition,
      ),
    },
  };
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
  const targetAddress =
    typeof input.targetAddress === 'string' ? input.targetAddress.trim() : '';

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

  const maxDeposit = toFiniteNumber(input.maxDeposit);
  const maxMonthlyRent = toFiniteNumber(input.maxMonthlyRent);

  return {
    needName,
    lat,
    lng,
    ...(targetAddress ? { targetAddress } : {}),
    ...(sidoName ? { sidoName } : {}),
    ...(sigunguName ? { sigunguName } : {}),
    maxCommuteTime,
    rentType,
    minDeposit,
    minMonthlyRent,
    ...(maxDeposit !== null ? { maxDeposit } : {}),
    ...(maxMonthlyRent !== null ? { maxMonthlyRent } : {}),
    housingTypes,
    minSize,
    lifestyleTags,
    floors,
  };
}

export async function GET(req: NextRequest) {
  try {
    const backendUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs`;
    const authorizationHeader = req.headers.get('authorization');
    const requestLog = {
      method: 'GET',
      url: backendUrl,
      hasAuthorization: Boolean(authorizationHeader),
      authorization: maskAuthorizationHeader(authorizationHeader),
      userAgent: req.headers.get('user-agent'),
    };

    
    

    
    const backendRes = await fetch(backendUrl, {
      method: 'GET',
      headers: {
        ...(authorizationHeader ? { 'Authorization': authorizationHeader } : {}),
      },
      cache: 'no-store',
    });
    const rawText = await backendRes.text();
    const contentType = backendRes.headers.get('content-type') || 'application/json';

    
    
    
    

    if (!backendRes.ok) {
      return new NextResponse(rawText, {
        status: backendRes.status,
        headers: { 'Content-Type': contentType },
      });
    }

    const rawData = rawText ? JSON.parse(rawText) : null;
    const data = normalizeConditionsResponse(rawData);
    
    
    
    return new NextResponse(JSON.stringify(data), {
      status: backendRes.status,
      headers: { 'Content-Type': contentType },
    });
  } catch (e) {
    
    
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

export async function POST(req: NextRequest) {
  try {
    const backendUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs`;
    const authorizationHeader = req.headers.get('authorization');
    const rawBody = await req.json();

    

    const body = normalizeUserNeedCreatePayload(rawBody);

    const requiredFieldAudit = {
      needName: Boolean(body.needName),
      targetAddress: typeof body.targetAddress === 'string' && body.targetAddress.trim().length > 0,
      lat: Number.isFinite(body.lat),
      lng: Number.isFinite(body.lng),
      maxCommuteTime: Number.isFinite(body.maxCommuteTime),
      rentType: Boolean(body.rentType),
      minDeposit: Number.isFinite(body.minDeposit),
      minMonthlyRent: Number.isFinite(body.minMonthlyRent),
      housingTypes: Array.isArray(body.housingTypes) && body.housingTypes.length > 0,
      minSize: Number.isFinite(body.minSize) && body.minSize > 0,
      lifestyleTags: Array.isArray(body.lifestyleTags) && body.lifestyleTags.length > 0,
      floors: Array.isArray(body.floors) && body.floors.length > 0,
    };

    
    

    const requestLog = {
      method: 'POST',
      url: backendUrl,
      hasAuthorization: Boolean(authorizationHeader),
      authorization: maskAuthorizationHeader(authorizationHeader),
      body,
    };

    
    

    const backendRes = await fetch(backendUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(authorizationHeader ? { Authorization: authorizationHeader } : {}),
      },
      body: JSON.stringify(body),
    });

    const rawText = await backendRes.text();
    const contentType = backendRes.headers.get('content-type') || 'application/json';

    
    
    
    

    return new NextResponse(rawText, {
      status: backendRes.status,
      headers: { 'Content-Type': contentType },
    });
  } catch (e) {
    
    
    const message = e instanceof Error ? e.message : '잘못된 요청';
    return NextResponse.json({ message }, { status: 400 });
  }
}

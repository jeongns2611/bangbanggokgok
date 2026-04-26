import { NextRequest, NextResponse } from 'next/server';

const getNonEmptyString = (value: unknown): string | null => {
  if (typeof value !== 'string') {
    return null;
  }

  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
};

const getAiMessageFromObject = (target: Record<string, unknown>): string | null => {
  const directMessage =
    getNonEmptyString(target.aiMessage) ??
    getNonEmptyString(target.ai_message) ??
    getNonEmptyString(target.recommendationReason) ??
    getNonEmptyString(target.recommendation_reason) ??
    getNonEmptyString(target.recommendReason);

  if (directMessage) {
    return directMessage;
  }

  const reasonArray =
    (Array.isArray(target.recommendReasons) ? target.recommendReasons : null) ??
    (Array.isArray(target.recommendationReasons) ? target.recommendationReasons : null);

  if (!reasonArray || reasonArray.length === 0) {
    return null;
  }

  const merged = reasonArray
    .filter((item): item is string => typeof item === 'string')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
    .join('\n');

  return merged.length > 0 ? merged : null;
};

const normalizeHouseDetailResponse = (payload: unknown) => {
  if (typeof payload !== 'object' || payload === null) {
    return payload;
  }

  const root = payload as Record<string, unknown>;
  const data =
    typeof root.data === 'object' && root.data !== null
      ? (root.data as Record<string, unknown>)
      : null;

  if (!data) {
    return payload;
  }

  const aiMessage = getAiMessageFromObject(data) ?? getAiMessageFromObject(root);

  if (!aiMessage) {
    return payload;
  }

  return {
    ...root,
    data: {
      ...data,
      aiMessage,
    },
  };
};

type RecommendHouseDetailParams = {
  params: Promise<{
    houseId: string;
  }>;
};

export async function GET(req: NextRequest, { params }: RecommendHouseDetailParams) {
  try {
    const { houseId } = await params;

    if (!houseId || !/^\d+$/.test(houseId)) {
      return NextResponse.json(
        { message: 'houseId는 올바른 숫자여야 합니다.' },
        { status: 400 },
      );
    }

    const backendUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/recommendations/houses/${houseId}`;
    const authHeader = req.headers.get('authorization');

    
    
    
    
    
    
    

    const backendRes = await fetch(backendUrl, {
      method: 'GET',
      headers: {
        ...(authHeader ? { Authorization: authHeader } : {}),
      },
    });

    

    const data = await backendRes.json().catch(() => ({}));
    

    const normalizedData = normalizeHouseDetailResponse(data);
    

    return NextResponse.json(normalizedData, { status: backendRes.status });
  } catch (error) {
    
    return NextResponse.json(
      { message: error instanceof Error ? error.message : '잘못된 요청' },
      { status: 400 },
    );
  }
}

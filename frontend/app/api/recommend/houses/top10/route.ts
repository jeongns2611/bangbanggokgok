import { NextRequest, NextResponse } from 'next/server';

type RecommendTop10Request = {
  id?: unknown;
  houseId?: unknown;
};

export async function POST(req: NextRequest) {
  try {
    const traceId = `top10-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    const startedAt = Date.now();
    const body = (await req.json().catch(() => null)) as RecommendTop10Request | null;
    const id = String(body?.id ?? '1');
    const houseId = String(body?.houseId ?? '0');

    

    if (!/^\d+$/.test(id) || !/^\d+$/.test(houseId)) {
      return NextResponse.json(
        { message: 'id, houseId는 숫자여야 합니다.' },
        { status: 400 },
      );
    }

    

    const searchParams = new URLSearchParams({ id, houseId });
    const backendUrl = `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/recommendations/houses/top10?${searchParams.toString()}`;
    const authHeader = req.headers.get('authorization');

    
    
    
    
    
    
    
    

    const backendRes = await fetch(backendUrl, {
      method: 'GET',
      headers: {
        ...(authHeader ? { Authorization: authHeader } : {}),
      },
    });

    

    const data = await backendRes.json().catch(() => ({}));
    
    
    return NextResponse.json(data, { status: backendRes.status });
  } catch (error) {
    
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

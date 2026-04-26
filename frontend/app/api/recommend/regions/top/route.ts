import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

export async function GET(req: NextRequest) {
  try {
    const authHeader = req.headers.get('authorization');
    const searchParams = req.nextUrl.searchParams;

    const backendUrl = new URL(
      `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/recommendations/regions/top`,
    );

    
    for (const [key, value] of searchParams.entries()) {
      backendUrl.searchParams.append(key, value);
    }

    
    
    
    

    const backendRes = await fetch(backendUrl.toString(), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(authHeader ? { Authorization: authHeader } : {}),
      },
    });

    let data;
    let responseText = '';
    try {
      responseText = await backendRes.text();
      data = JSON.parse(responseText);
      
    } catch (parseError) {
      
      data = { message: responseText || '지역구 추천 조회 실패' };
    }

    if (!backendRes.ok) {
      return NextResponse.json(data ?? { message: '지역구 추천 조회 실패' }, {
        status: backendRes.status,
      });
    }

    return NextResponse.json(data, { status: backendRes.status });
  } catch (error) {
    
    const errorMessage = error instanceof Error ? error.message : '알 수 없는 에러';
    return NextResponse.json(
      { message: `지역구 추천 조회 실패: ${errorMessage}` },
      { status: 500 },
    );
  }
}

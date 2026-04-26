import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const authorization = request.headers.get('authorization');
  const { id } = await params;
  const searchParams = request.nextUrl.searchParams.toString();
  const endpoint = `${backendUrl}/api/v1/house/${id}/infrastructures${searchParams ? `?${searchParams}` : ''}`;

  

  if (!/^\d+$/.test(id)) {
    
    return NextResponse.json(
      { code: 400, message: 'houseId는 숫자여야 합니다.' },
      { status: 400 },
    );
  }

  const response = await fetch(endpoint, {
    method: 'GET',
    headers: {
      ...(authorization ? { Authorization: authorization } : {}),
    },
    cache: 'no-store',
  });

  const text = await response.text();

  

  return new NextResponse(text, {
    status: response.status,
    headers: {
      'Content-Type': response.headers.get('content-type') || 'application/json',
      'Cache-Control': 'no-store, no-cache, must-revalidate',
    },
  });
}

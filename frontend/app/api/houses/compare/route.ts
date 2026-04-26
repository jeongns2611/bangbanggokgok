import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function GET(request: NextRequest) {
  const authorization = request.headers.get('authorization');
  if (!authorization) {
    return NextResponse.json(
      { code: 401, message: 'Authorization header is required.' },
      { status: 401 },
    );
  }

  const ids = request.nextUrl.searchParams.get('ids')?.trim();
  if (!ids) {
    return NextResponse.json(
      { code: 400, message: 'ids query parameter is required.' },
      { status: 400 },
    );
  }

  const response = await fetch(
    `${backendUrl}/api/v1/houses/compare?ids=${encodeURIComponent(ids)}`,
    {
      method: 'GET',
      headers: { Authorization: authorization },
      cache: 'no-store',
    },
  );

  const text = await response.text();
  
  

  try {
    const parsed = JSON.parse(text);
    
    
  } catch {
    
  }

  return new NextResponse(text, {
    status: response.status,
    headers: {
      'Content-Type': response.headers.get('content-type') || 'application/json',
    },
  });
}

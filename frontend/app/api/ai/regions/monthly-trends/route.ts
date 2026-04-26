import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

export async function GET(request: NextRequest) {
  if (!backendUrl) {
    return NextResponse.json(
      { code: 500, message: 'NEXT_PUBLIC_BACKEND_URL is not configured.' },
      { status: 500 },
    );
  }

  const sigunguName = request.nextUrl.searchParams.get('sigunguName')?.trim();

  if (!sigunguName) {
    return NextResponse.json(
      { code: 400, message: 'sigunguName is required.' },
      { status: 400 },
    );
  }

  const authorization = request.headers.get('authorization');

  const apiUrl = new URL(`${backendUrl}/api/v1/ai/regions/monthly-trends`);
  apiUrl.searchParams.set('sigunguName', sigunguName);

  

  try {
    const backendResponse = await fetch(apiUrl.toString(), {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(authorization ? { Authorization: authorization } : {}),
      },
      cache: 'no-store',
    });

    const text = await backendResponse.text();

    

    return new NextResponse(text, {
      status: backendResponse.status,
      headers: {
        'Content-Type': backendResponse.headers.get('content-type') || 'application/json',
      },
    });
  } catch {
    return NextResponse.json(
      { code: 502, message: 'Failed to reach backend AI API.' },
      { status: 502 },
    );
  }
}

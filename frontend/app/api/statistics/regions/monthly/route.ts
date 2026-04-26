import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function GET(request: NextRequest) {
  try {
    if (!backendUrl) {
      return NextResponse.json(
        { code: 500, message: 'NEXT_PUBLIC_BACKEND_URL is not configured.' },
        { status: 500 },
      );
    }

    const searchParams = request.nextUrl.searchParams;
    const sidoName = searchParams.get('sidoName')?.trim();
    const sigunguName = searchParams.get('sigunguName')?.trim();

    if (!sidoName || !sigunguName) {
      return NextResponse.json(
        { code: 400, message: 'sidoName and sigunguName are required.' },
        { status: 400 },
      );
    }

    const apiUrl = new URL(`${backendUrl}/api/v1/statistics/regions/monthly`);
    apiUrl.searchParams.set('sidoName', sidoName);
    apiUrl.searchParams.set('sigunguName', sigunguName);

    const authorization = request.headers.get('authorization');

    

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
      { code: 502, message: 'Failed to reach backend API.' },
      { status: 502 },
    );
  }
}

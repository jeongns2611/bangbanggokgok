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

  const response = await fetch(`${backendUrl}/api/v1/houses/wish`, {
    method: 'GET',
    headers: { Authorization: authorization },
    cache: 'no-store',
  });

  const text = await response.text();
  return new NextResponse(text, {
    status: response.status,
    headers: {
      'Content-Type': response.headers.get('content-type') || 'application/json',
    },
  });
}

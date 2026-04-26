import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  const authorization = request.headers.get('authorization');
  if (!authorization) {
    return NextResponse.json({ code: 401, message: 'Authorization header is required.' }, { status: 401 });
  }
  try {
    const response = await fetch(`${backendUrl}/api/v1/houses/${id}/sold`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: authorization,
      },
    });
    const text = await response.text();
    return new NextResponse(text, {
      status: response.status,
      headers: { 'Content-Type': response.headers.get('content-type') || 'application/json' },
    });
  } catch {
    return NextResponse.json({ code: 502, message: 'Failed to reach backend API.' }, { status: 502 });
  }
}

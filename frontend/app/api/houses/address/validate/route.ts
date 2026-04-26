import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function POST(request: NextRequest) {
  const body = await request.json();

  try {
    const response = await fetch(`${backendUrl}/api/v1/houses/address/validate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
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

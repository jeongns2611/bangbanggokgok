import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

type AiChatPayload = {
  query?: unknown;
  sidoName?: unknown;
  sigunguName?: unknown;
  houseType?: unknown;
  rentType?: unknown;
  minMonthlyCost?: unknown;
  maxMonthlyCost?: unknown;
  minDeposit?: unknown;
  maxDeposit?: unknown;
  managementCost?: unknown;
  minFloorSize?: unknown;
  maxFloorSize?: unknown;
  floor?: unknown;
};

export async function GET(request: NextRequest) {
  if (!backendUrl) {
    return NextResponse.json(
      { code: 500, message: 'NEXT_PUBLIC_BACKEND_URL is not configured.' },
      { status: 500 },
    );
  }

  const authorization = request.headers.get('authorization');
  const requestUrl = `${backendUrl}/api/v1/ai/chat${request.nextUrl.search}`;

  

  try {
    const response = await fetch(requestUrl, {
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
      },
    });
  } catch (error) {
    
    return NextResponse.json(
      { code: 502, message: 'Failed to reach backend AI chat API.' },
      { status: 502 },
    );
  }
}

export async function POST(request: NextRequest) {
  if (!backendUrl) {
    return NextResponse.json(
      { code: 500, message: 'NEXT_PUBLIC_BACKEND_URL is not configured.' },
      { status: 500 },
    );
  }

  const authorization = request.headers.get('authorization');
  const body = (await request.json().catch(() => null)) as AiChatPayload | null;
  const requestUrl = `${backendUrl}/api/v1/ai/chat${request.nextUrl.search}`;

  if (!body || typeof body.query !== 'string' || !body.query.trim()) {
    return NextResponse.json(
      { code: 400, message: 'query is required in request body.' },
      { status: 400 },
    );
  }

  

  try {
    const response = await fetch(requestUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        ...(authorization ? { Authorization: authorization } : {}),
      },
      body: JSON.stringify(body),
      cache: 'no-store',
    });

    const text = await response.text();

    

    return new NextResponse(text, {
      status: response.status,
      headers: {
        'Content-Type': response.headers.get('content-type') || 'application/json',
      },
    });
  } catch (error) {
    
    return NextResponse.json(
      { code: 502, message: 'Failed to reach backend AI chat API.' },
      { status: 502 },
    );
  }
}

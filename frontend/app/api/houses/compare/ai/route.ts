import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function POST(request: NextRequest) {
  const authorization = request.headers.get('authorization');
  if (!authorization) {
    return NextResponse.json(
      { code: 401, message: 'Authorization header is required.' },
      { status: 401 },
    );
  }

  const body = await request.json().catch(() => null) as {
    comparisonData?: unknown;
  } | null;

  if (!Array.isArray(body?.comparisonData) || body.comparisonData.length === 0) {
    return NextResponse.json(
      { code: 400, message: 'comparisonData is required in request body.' },
      { status: 400 },
    );
  }

  const requestPayload = { comparisonData: body.comparisonData };
  
  
  
  

  const response = await fetch(`${backendUrl}/api/v1/houses/compare/ai`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: authorization,
    },
    body: JSON.stringify(requestPayload),
    cache: 'no-store',
  });

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

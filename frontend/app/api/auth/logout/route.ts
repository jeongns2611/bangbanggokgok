import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';
  const authorization = request.headers.get('authorization');

  
  
  

  if (!authorization) {
    
    return NextResponse.json(
      {
        code: 401,
        message: 'Authorization header is required',
        data: null,
      },
      { status: 401 },
    );
  }

  
  const response = await fetch(`${backendUrl}/api/v1/auth/logout`, {
    method: 'POST',
    headers: { Authorization: authorization },
  }).catch((error) => {
    
    return null;
  });

  if (!response) {
    
    return NextResponse.json(
      {
        code: 502,
        message: 'Backend service unavailable',
        data: null,
      },
      { status: 502 },
    );
  }

  const text = await response.text();
  const contentType = response.headers.get('content-type');

  
  

  return new NextResponse(text, {
    status: response.status,
    headers: {
      'Content-Type': contentType || 'application/json',
    },
  });
}
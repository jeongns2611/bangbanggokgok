import { NextRequest, NextResponse } from 'next/server';

export async function POST(request: NextRequest) {
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';
  
  
  const body = await request.json().catch(() => null);
  const refreshToken = typeof body?.refreshToken === 'string' ? body.refreshToken : null;

  
  
  

  if (!refreshToken) {
    
    return NextResponse.json(
      { 
        code: 400,
        message: 'refreshToken is required in request body',
        data: null 
      },
      { status: 400 },
    );
  }

  
  
  const response = await fetch(`${backendUrl}/api/v1/auth/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken }),
  }).catch((error) => {
    
    return null;
  });

  if (!response) {
    
    return NextResponse.json(
      { 
        code: 502,
        message: 'Backend service unavailable',
        data: null 
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

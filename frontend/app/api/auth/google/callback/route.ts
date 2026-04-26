import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  const code = request.nextUrl.searchParams.get('code');
  const error = request.nextUrl.searchParams.get('error');
  const origin = process.env.APP_URL || request.nextUrl.origin;

  
  

  if (error || !code) {
    
    return NextResponse.redirect(new URL('/?login=failed', origin));
  }

  const redirectUri = `${origin}/api/auth/google/callback`;
  

  
  const tokenRes = await fetch('https://oauth2.googleapis.com/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      code,
      client_id: process.env.GOOGLE_CLIENT_ID!,
      client_secret: process.env.GOOGLE_CLIENT_SECRET!,
      redirect_uri: redirectUri,
      grant_type: 'authorization_code',
    }),
  });

  

  if (!tokenRes.ok) {
    const errBody = await tokenRes.text();
    
    return NextResponse.redirect(new URL('/?login=failed', origin));
  }

  const tokenData = await tokenRes.json();
  const idToken: string = tokenData.id_token;
  

  if (!idToken) {
    
    return NextResponse.redirect(new URL('/?login=failed', origin));
  }

  
  const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:18081';
  

  const backendRes = await fetch(`${backendUrl}/api/v1/login/auth`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ googleToken: idToken }),
  });

  

  if (!backendRes.ok) {
    const errBody = await backendRes.text();
    
    return NextResponse.redirect(new URL('/?login=failed', origin));
  }

  const backendData = await backendRes.json();
  const authData = backendData.data;
  

  
  const callbackUrl = new URL('/auth/callback', origin);
  callbackUrl.searchParams.set('data', encodeURIComponent(JSON.stringify(authData)));

  
  return NextResponse.redirect(callbackUrl);
}

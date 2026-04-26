import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function POST(request: NextRequest) {
  const authorization = request.headers.get('authorization');
  const body = await request.json();
  const requestUrl = `${backendUrl}/api/v1/houses/search`;

  

  try {
    const response = await axios.request({
      url: requestUrl,
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        ...(authorization ? { Authorization: authorization } : {}),
      },
      data: body,
      validateStatus: () => true,
    });

    

    return NextResponse.json(response.data, { status: response.status });
  } catch (error) {
    
    return NextResponse.json({ code: 502, message: 'Failed to reach backend API.' }, { status: 502 });
  }
}


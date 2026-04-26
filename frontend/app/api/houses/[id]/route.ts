export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const authorization = request.headers.get('authorization');
  const { id } = await params;

  if (!authorization) {
    return NextResponse.json({ code: 401, message: 'Authorization header is required.' }, { status: 401 });
  }

  const response = await fetch(`${backendUrl}/api/v1/houses/${id}`, {
    method: 'DELETE',
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
}
import { NextRequest, NextResponse } from 'next/server';

export const dynamic = 'force-dynamic';
export const revalidate = 0;

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;

  const response = await fetch(`${backendUrl}/api/v1/houses/${id}`, {
    method: 'GET',
    cache: 'no-store',
  });

  const text = await response.text();
  return new NextResponse(text, {
    status: response.status,
    headers: {
      'Content-Type': response.headers.get('content-type') || 'application/json',
      'Cache-Control': 'no-store, no-cache, must-revalidate',
    },
  });
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> },
) {
  const authorization = request.headers.get('authorization');
  const { id } = await params;

  if (!authorization) {
    return NextResponse.json({ code: 401, message: 'Authorization header is required.' }, { status: 401 });
  }

  const body = await request.json();

  const response = await fetch(`${backendUrl}/api/v1/houses/${id}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Authorization: authorization,
    },
    body: JSON.stringify({ ...body, houseId: Number(id) }),
  });

  const text = await response.text();
  return new NextResponse(text, {
    status: response.status,
    headers: { 'Content-Type': response.headers.get('content-type') || 'application/json' },
  });
}

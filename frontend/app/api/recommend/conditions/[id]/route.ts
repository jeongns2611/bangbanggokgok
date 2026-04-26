import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const backendRes = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs/detail/${id}`, {
      method: 'GET',
      headers: {
        ...(req.headers.get('authorization') ? { 'Authorization': req.headers.get('authorization')! } : {}),
      },
    });
    const data = await backendRes.json();
    return NextResponse.json(data, { status: backendRes.status });
  } catch (e) {
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

export async function PUT(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await req.json();
    const backendRes = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs/${id}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        ...(req.headers.get('authorization') ? { 'Authorization': req.headers.get('authorization')! } : {}),
      },
      body: JSON.stringify(body),
    });
    const data = await backendRes.json();
    return NextResponse.json(data, { status: backendRes.status });
  } catch (e) {
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

export async function DELETE(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;

    if (!/^\d+$/.test(id)) {
      return NextResponse.json({ message: 'needsId는 숫자여야 합니다.' }, { status: 400 });
    }

    const backendRes = await fetch(`${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs/${id}`, {
      method: 'DELETE',
      headers: {
        ...(req.headers.get('authorization') ? { Authorization: req.headers.get('authorization')! } : {}),
      },
    });

    const data = await backendRes.json().catch(() => null);

    if (!backendRes.ok) {
      return NextResponse.json(data ?? { message: '조건 삭제 실패' }, { status: backendRes.status });
    }

    return NextResponse.json(data, { status: backendRes.status });
  } catch {
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

import { NextRequest, NextResponse } from 'next/server';

export async function PUT(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;

    if (!/^\d+$/.test(id)) {
      return NextResponse.json({ message: 'needsId는 숫자여야 합니다.' }, { status: 400 });
    }

    const backendRes = await fetch(
      `${process.env.NEXT_PUBLIC_BACKEND_URL}/api/v1/user-needs/set-default/${id}`,
      {
        method: 'PUT',
        headers: {
          ...(req.headers.get('authorization')
            ? { Authorization: req.headers.get('authorization')! }
            : {}),
        },
      },
    );

    const data = await backendRes.json().catch(() => null);

    if (!backendRes.ok) {
      return NextResponse.json(data ?? { message: '기본 조건 설정 실패' }, { status: backendRes.status });
    }

    return NextResponse.json(data, { status: backendRes.status });
  } catch {
    return NextResponse.json({ message: '잘못된 요청' }, { status: 400 });
  }
}

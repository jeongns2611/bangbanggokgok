import { NextRequest, NextResponse } from 'next/server';

const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || '';

export async function POST(req: NextRequest, { params }: { params: Promise<{ houseId: string }> }) {
  try {
    const { houseId } = await params;
    const authHeader = req.headers.get('authorization');
    
    
    
    
    if (authHeader) {
      
    }

    if (!/^\d+$/.test(houseId)) {
      
      return NextResponse.json({ message: 'houseId는 숫자여야 합니다.' }, { status: 400 });
    }

    const url = `${backendUrl}/api/v1/houses/wish/${houseId}`;
    
    
    const fetchHeaders: Record<string, string> = {};
    if (authHeader) {
      fetchHeaders['Authorization'] = authHeader;
      
    } else {
      
    }
    
    

    const backendRes = await fetch(
      url,
      {
        method: 'POST',
        headers: fetchHeaders,
      },
    );

    
    
    
    let data;
    let responseText = '';
    try {
      responseText = await backendRes.text();
      
      data = JSON.parse(responseText);
      
    } catch (parseError) {
      
      
      data = { message: responseText || '찜하기 토글 실패' };
    }

    if (!backendRes.ok) {
      
      
      return NextResponse.json(data ?? { message: '찜하기 토글 실패' }, { status: backendRes.status });
    }

    
    return NextResponse.json({
      result: data.data?.result,
      message: data.data?.message,
    }, { status: 200 });
  } catch (error) {
    
    const errorMessage = error instanceof Error ? error.message : '알 수 없는 에러';
    
    return NextResponse.json({ message: `잘못된 요청: ${errorMessage}` }, { status: 400 });
  }
}

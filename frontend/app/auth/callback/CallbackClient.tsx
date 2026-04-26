'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/useAuthStore';

export default function CallbackClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const login = useAuthStore((state) => state.login);

  useEffect(() => {
    const data = searchParams.get('data');
    

    if (!data) {
      
      router.replace('/');
      return;
    }

    try {
      const parsed = JSON.parse(decodeURIComponent(data));
      
      
      
      
      

      login(
        {
          userId: parsed.userId,
          email: parsed.googleEmail,
          name: parsed.name,
          profileUrl: parsed.profileUrl,
        },
        parsed.accessToken,
        parsed.refreshToken,
      );

      
      

      
      setTimeout(() => {
        
        router.replace('/');
      }, 500);
    } catch (e) {
      
      router.replace('/');
    }
  }, [searchParams, login, router]);

  return (
    <div className="flex h-screen items-center justify-center">
      <p className="text-gray-500">로그인 처리 중...</p>
    </div>
  );
}

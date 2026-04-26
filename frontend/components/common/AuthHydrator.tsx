'use client';

import { useEffect } from 'react';
import { useAuthStore } from '@/store/useAuthStore';

export default function AuthHydrator() {
  const login = useAuthStore((state) => state.login);

  useEffect(() => {
    const cookie = document.cookie
      .split('; ')
      .find((c) => c.startsWith('auth-callback='));

    if (!cookie) return;

    try {
      const raw = decodeURIComponent(cookie.split('=').slice(1).join('='));
      const data = JSON.parse(raw);

      login(
        {
          userId: data.userId,
          email: data.googleEmail,
          name: data.name,
          profileUrl: data.profileUrl,
        },
        data.accessToken,
        data.refreshToken,
      );
    } catch {
      
    }

    
    document.cookie = 'auth-callback=; path=/; max-age=0';
  }, [login]);

  return null;
}

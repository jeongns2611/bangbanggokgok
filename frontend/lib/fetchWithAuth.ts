import { useAuthStore } from '@/store/useAuthStore';
import type { ApiResponse, TokenRefreshData } from '@/types/api';

let refreshPromise: Promise<boolean> | null = null;
let hasShownRefreshFailAlert = false;

function maskAuthorizationHeader(value: string | null) {
  if (!value) {
    return null;
  }

  if (value.length <= 20) {
    return value;
  }

  return `${value.slice(0, 16)}...${value.slice(-8)}`;
}

function handleRefreshFailure(logout: () => void): false {
  
  logout();
  return false;
}


function extractTokenFromResponse(json: any): TokenRefreshData | null {
  if (!json) {
    
    return null;
  }

  
  const tokenPayload = json.data ?? json;
  
  if (!tokenPayload) {
    
    return null;
  }

  const { accessToken, refreshToken } = tokenPayload;

  if (typeof accessToken !== 'string' || typeof refreshToken !== 'string') {
    
    return null;
  }

  if (!accessToken.trim() || !refreshToken.trim()) {
    
    return null;
  }

  return { accessToken, refreshToken };
}

async function refreshAccessToken(): Promise<boolean> {
  const { refreshToken, logout, setTokens } = useAuthStore.getState();
  

  if (!refreshToken) {
    
    return handleRefreshFailure(logout);
  }

  try {
    
    const res = await fetch('/api/auth/refresh', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ refreshToken }),
    });

    

    
    if (!res.ok) {
      const errorJson = await res.json().catch(() => null);
      const errorMessage = errorJson?.message || '토큰 갱신 실패';
      
      return handleRefreshFailure(logout);
    }

    
    const json: ApiResponse<TokenRefreshData> = await res.json().catch(() => null);
    

    const tokenData = extractTokenFromResponse(json);
    if (!tokenData) {
      
      return handleRefreshFailure(logout);
    }

    const { accessToken: newAccessToken, refreshToken: newRefreshToken } = tokenData;
    setTokens(newAccessToken, newRefreshToken);
    hasShownRefreshFailAlert = false;
    
    return true;
  } catch (e) {
    
    return handleRefreshFailure(logout);
  }
}

export async function fetchWithAuth(
  url: string,
  options: RequestInit = {},
): Promise<Response> {
  const { accessToken } = useAuthStore.getState();
  const method = options.method ?? 'GET';

  const headers = new Headers(options.headers);
  if (accessToken) {
    headers.set('Authorization', `Bearer ${accessToken}`);
  }

  

  let response = await fetch(url, { ...options, headers });
  

  
  if (!response.ok) {
    
    
    if (!refreshPromise) {
      refreshPromise = refreshAccessToken().finally(() => {
        refreshPromise = null;
      });
    }

    const refreshed = await refreshPromise;
    

    if (refreshed) {
      
      const { accessToken: newToken } = useAuthStore.getState();
      const retryHeaders = new Headers(options.headers);
      if (newToken) {
        retryHeaders.set('Authorization', `Bearer ${newToken}`);
      }
      
      response = await fetch(url, { ...options, headers: retryHeaders });
      
    }
  }

  return response;
}

"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuthStore } from "@/store/useAuthStore";
import type { ApiResponse, TokenRefreshData } from "@/types/api";


function extractTokenFromResponse(json: any): TokenRefreshData | null {
  if (!json) return null;

  
  const tokenPayload = json.data ?? json;
  
  if (!tokenPayload) return null;

  const { accessToken, refreshToken } = tokenPayload;

  
  if (typeof accessToken !== 'string' || typeof refreshToken !== 'string') {
    
    return null;
  }

  
  if (!accessToken.trim() || !refreshToken.trim()) {
    
    return null;
  }

  return { accessToken, refreshToken };
}

export default function Footer() {
  const { refreshToken, setTokens } = useAuthStore();
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [refreshResult, setRefreshResult] = useState<string | null>(null);

  const email = "juseongyu56@gmail.com";

  const subject = encodeURIComponent("[방방곡곡 문의]");
  const body = encodeURIComponent(
`안녕하세요. 방방곡곡 서비스 문의드립니다.

문의 유형:
- 오류 제보
- 서비스 문의
- 기타

문의 내용:


-------------------------
현재 페이지:
브라우저:
`
  );

  const gmailLink =
    `https://mail.google.com/mail/?view=cm&fs=1&to=${email}&su=${subject}&body=${body}`;

  const handleManualRefresh = async () => {
    

    if (!refreshToken) {
      
      setRefreshResult("로그인 상태가 아닙니다.");
      return;
    }

    setIsRefreshing(true);
    setRefreshResult(null);

    try {
      
      const res = await fetch("/api/auth/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken }),
      });

      

      
      const json: ApiResponse<TokenRefreshData> = await res.json().catch(() => null);
      

      
      if (!res.ok) {
        const errorMessage = json?.message || `재발급 실패 (HTTP ${res.status})`;
        
        setRefreshResult(errorMessage);
        return;
      }

      
      const tokenData = extractTokenFromResponse(json);
      if (!tokenData) {
        
        setRefreshResult("응답에 유효한 토큰이 없습니다.");
        return;
      }

      const { accessToken: newAccessToken, refreshToken: newRefreshToken } = tokenData;
      setTokens(newAccessToken, newRefreshToken);
      
      setRefreshResult("✓ 토큰 재발급 성공");
    } catch (error) {
      
      const errorMessage = error instanceof Error ? error.message : '알 수 없는 오류';
      setRefreshResult(`요청 오류: ${errorMessage}`);
    } finally {
      setIsRefreshing(false);
    }
  };

  return (
    <footer className="w-full border-t border-gray-200 bg-gray-50">
      <div className="max-w-[1200px] mx-auto px-6 py-12">

        <div className="grid grid-cols-1 md:grid-cols-4 gap-10">

          {}
          <div>
            <h3 className="text-lg font-semibold text-gray-900">
              방방곡곡
            </h3>
            <p className="mt-3 text-sm text-gray-600 leading-relaxed">
              데이터를 기반으로 나에게 맞는 지역과
              매물을 찾을 수 있도록 돕는 서비스입니다.
            </p>
          </div>

          {}
          <div>
            <h4 className="font-medium text-gray-900 mb-3">
              서비스
            </h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>
                <Link href="/recommend">실매물 추천</Link>
              </li>
              <li>
                <Link href="/search">실매물 검색</Link>
              </li>
              <li>
                <Link href="/statistics">시각화 통계</Link>
              </li>
              <li>
                <Link href="/comparison">매물 비교</Link>
              </li>
            </ul>
          </div>

          {}
          <div>
            <h4 className="font-medium text-gray-900 mb-3">
              데이터
            </h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>서울 전월세 시세</li>
              <li>생활 인프라 데이터</li>
              <li>안전 데이터</li>
            </ul>
          </div>

          {}
          <div>
            <h4 className="font-medium text-gray-900 mb-3">
              기타
            </h4>
            <ul className="space-y-2 text-sm text-gray-600">
              <li>
                <a
                  href={gmailLink}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="hover:text-primary-100"
                >
                  문의하기
                </a>
              </li>
              <li>
                <button
                  type="button"
                  onClick={handleManualRefresh}
                  disabled={isRefreshing}
                  className="text-left hover:text-primary-100 disabled:opacity-50"
                >
                  {isRefreshing ? "토큰 재발급 중..." : "토큰 재발급(임시)"}
                </button>
              </li>
              {refreshResult && (
                <li className="text-xs text-gray-500">{refreshResult}</li>
              )}
            </ul>
          </div>

        </div>

        <div className="mt-10 pt-6 border-t border-gray-200 text-sm text-gray-500">
          © 2026 방방곡곡. All rights reserved.
        </div>

      </div>
    </footer>
  );
}
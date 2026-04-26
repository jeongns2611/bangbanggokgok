import { Suspense } from 'react';
import CallbackClient from './CallbackClient';

function CallbackFallback() {
  return (
    <div className="flex h-screen items-center justify-center">
      <p className="text-gray-500">로그인 처리 중...</p>
    </div>
  );
}

export default function AuthCallbackPage() {
  return (
    <Suspense fallback={<CallbackFallback />}>
      <CallbackClient />
    </Suspense>
  );
}

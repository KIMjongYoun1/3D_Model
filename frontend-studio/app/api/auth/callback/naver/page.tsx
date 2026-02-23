"use client";

import { useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { authApi } from '@/lib/authApi';

export default function NaverCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const requestSent = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const storedState = localStorage.getItem('naver_auth_state');

    if (code && state && !requestSent.current) {
      if (state !== storedState) {
        console.error('Invalid state');
        router.push('/studio?error=invalid_state');
        return;
      }

      requestSent.current = true;

      authApi.get(`/api/v1/auth/naver/callback?code=${code}&state=${state}`)
        .then(response => {
          const { needsAgreement, agreementToken, name } = response.data;

          if (needsAgreement && agreementToken) {
            sessionStorage.setItem('terms_agree_user_name', name || '');
            router.push(`/auth/agree?token=${encodeURIComponent(agreementToken)}`);
            return;
          }

          router.push('/studio');
        })
        .catch(error => {
          console.error('Naver login failed:', error);
          router.push('/studio?error=login_failed');
        });
    }
  }, [searchParams, router]);

  return (
    <div className="flex h-screen w-screen items-center justify-center bg-white">
      <div className="flex flex-col items-center gap-4">
        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
        <p className="font-bold text-slate-600">네이버 로그인 처리 중...</p>
      </div>
    </div>
  );
}

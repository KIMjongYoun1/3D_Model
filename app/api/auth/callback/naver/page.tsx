"use client";

import { useEffect, useRef } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import axios from 'axios';

export default function NaverCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const requestSent = useRef(false);

  useEffect(() => {
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const storedState = localStorage.getItem('naver_auth_state');

    if (code && state && !requestSent.current) {
      // CSRF 검증
      if (state !== storedState) {
        console.error('Invalid state');
        router.push('/studio?error=invalid_state');
        return;
      }

      requestSent.current = true;

      // Java 백엔드에 인증 코드 전달
      axios.get(`http://localhost:8080/api/v1/auth/naver/callback?code=${code}&state=${state}`)
        .then(response => {
          const { accessToken, userId, email, name } = response.data;
          
          // JWT 토큰 및 사용자 정보 저장
          localStorage.setItem('accessToken', accessToken);
          localStorage.setItem('userId', userId);
          localStorage.setItem('userEmail', email);
          localStorage.setItem('userName', name);
          
          // 로그인 성공 후 스튜디오로 이동
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

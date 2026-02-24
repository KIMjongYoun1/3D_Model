'use client';

import React from 'react';
import { useSearchParams } from 'next/navigation';
import { Button } from "@/components/ui/Button";
import { getSafeRedirect } from "@/lib/authRedirect";

export default function LoginPage() {
  const searchParams = useSearchParams();

  const handleNaverLogin = () => {
    const redirect = getSafeRedirect(searchParams.get('redirect'), '/studio');
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('auth_redirect', redirect);
    }
    const clientId = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID;
    const redirectUri = encodeURIComponent('http://localhost:3000/api/auth/callback/naver');
    const state = Math.random().toString(36).substring(7);
    const naverAuthUrl = `https://nid.naver.com/oauth2.0/authorize?response_type=code&client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
    if (typeof window !== 'undefined') {
      localStorage.setItem('naver_auth_state', state);
      window.location.href = naverAuthUrl;
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Quantum Studio</h1>
          <p className="text-gray-600">소셜 계정으로 간편하게 로그인하세요</p>
        </div>

        <div className="bg-white rounded-lg shadow-md p-8">
          <div className="space-y-4">
            <Button
              variant="naver"
              size="lg"
              className="w-full py-4 flex items-center justify-center gap-3"
              onClick={handleNaverLogin}
            >
              <span className="w-5 h-5 bg-white text-[#03C75A] rounded-md flex items-center justify-center font-black">N</span>
              네이버로 로그인
            </Button>
            <Button
              variant="kakao"
              size="lg"
              className="w-full py-4 flex items-center justify-center gap-3"
              onClick={() => alert('카카오 로그인은 준비 중입니다.')}
            >
              <span className="w-5 h-5 bg-[#3C1E1E] text-[#FEE500] rounded-md flex items-center justify-center font-black">K</span>
              카카오로 로그인 (준비 중)
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}

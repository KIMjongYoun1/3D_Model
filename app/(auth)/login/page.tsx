'use client';

import React, { useState } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // TODO: API 호출
    console.log('Login:', { email, password });
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-md">
        {/* 로고/브랜드 영역 */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Virtual Try-On</h1>
          <p className="text-gray-600">로그인하여 서비스를 시작하세요</p>
        </div>

        {/* 로그인 카드 */}
        <div className="bg-white rounded-lg shadow-md p-8">
          <form onSubmit={handleSubmit} className="space-y-6">
            {/* 이메일 입력 */}
            <Input
              type="email"
              label="이메일"
              placeholder="your@email.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              error={errors.email}
              required
            />

            {/* 비밀번호 입력 */}
            <Input
              type="password"
              label="비밀번호"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              error={errors.password}
              required
            />

            {/* 로그인 버튼 */}
            <Button
              type="submit"
              variant="primary"
              size="lg"
              className="w-full"
            >
              로그인
            </Button>
          </form>

          {/* 회원가입 링크 */}
          <div className="mt-6 text-center">
            <p className="text-sm text-gray-600">
              계정이 없으신가요?{' '}
              <a href="/register" className="text-primary-500 hover:text-primary-600 font-medium">
                회원가입
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}



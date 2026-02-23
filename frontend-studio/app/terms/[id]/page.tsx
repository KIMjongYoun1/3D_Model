'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import axios from 'axios';

const API_URL = process.env.NEXT_PUBLIC_SERVICE_API_URL || 'http://localhost:8080';

export default function TermsDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const [title, setTitle] = useState<string>('');
  const [content, setContent] = useState<string>('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    axios.get(`${API_URL}/api/v1/terms/${id}`)
      .then(res => {
        setTitle(res.data.title || '');
        setContent(res.data.content || '');
      })
      .catch(() => router.push('/studio'))
      .finally(() => setLoading(false));
  }, [id, router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <header className="sticky top-0 z-10 border-b border-slate-200 bg-white/95 backdrop-blur px-6 py-4">
        <div className="max-w-3xl mx-auto flex items-center justify-between">
          <h1 className="text-lg font-bold text-slate-900">{title}</h1>
          <button type="button" onClick={() => window.close()} className="text-blue-600 font-bold hover:underline">
            닫기
          </button>
        </div>
      </header>
      <main className="max-w-3xl mx-auto px-6 py-8">
        <div
          className="text-slate-800 [&_h2]:mt-8 [&_h2]:mb-4 [&_h2]:text-xl [&_h2]:font-bold [&_p]:mb-4 [&_p]:leading-relaxed"
          dangerouslySetInnerHTML={{ __html: content }}
        />
      </main>
    </div>
  );
}

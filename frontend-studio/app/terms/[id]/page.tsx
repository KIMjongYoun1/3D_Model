'use client';

import React, { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { authApi } from '@/lib/authApi';

interface TermVersion {
  id: string;
  version: string;
  title: string;
  effectiveAt?: string;
}

export default function TermsDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = params.id as string;
  const [title, setTitle] = useState<string>('');
  const [content, setContent] = useState<string>('');
  const [allVersions, setAllVersions] = useState<TermVersion[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    authApi.get(`/api/v1/terms/${id}`)
      .then(res => {
        setTitle(res.data.title || '');
        setContent(res.data.content || '');
        setAllVersions(res.data.allVersions || []);
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
        <div className="max-w-3xl mx-auto flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3">
          <div>
            <h1 className="text-lg font-bold text-slate-900">{title}</h1>
            {allVersions.length > 1 && (
              <div className="flex flex-wrap gap-x-2 gap-y-1 mt-2">
                {allVersions.map((v) => (
                  <Link
                    key={v.id}
                    href={`/terms/${v.id}`}
                    className={`text-xs font-bold px-2 py-0.5 rounded ${
                      v.id === id ? 'bg-blue-600 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                    }`}
                  >
                    v{v.version}
                  </Link>
                ))}
              </div>
            )}
          </div>
          <button type="button" onClick={() => window.close()} className="text-blue-600 font-bold hover:underline self-start sm:self-auto">
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

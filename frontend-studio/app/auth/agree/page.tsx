'use client';

import React, { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { authApi } from '@/lib/authApi';
import { Button } from '@/components/ui/Button';
import { getSafeRedirect } from '@/lib/authRedirect';
import { TermsViewerModal, type TermVersionItem } from '@/components/terms/TermsViewerModal';

interface TermVersion {
  id: string;
  version: string;
  title: string;
  effectiveAt?: string;
}

interface TermSummary {
  id: string;
  type: string;
  title: string;
  version: string;
  content?: string;
  allVersions?: TermVersion[];
}

export default function TermsAgreePage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get('token');
  const [terms, setTerms] = useState<TermSummary[]>([]);
  const [agreedIds, setAgreedIds] = useState<Set<string>>(new Set());
  const [name, setName] = useState<string>('');
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [termsModalOpen, setTermsModalOpen] = useState(false);
  const [termsModalVersions, setTermsModalVersions] = useState<TermVersionItem[]>([]);
  const [termsModalInitialId, setTermsModalInitialId] = useState<string>("");

  useEffect(() => {
    const storedName = typeof window !== 'undefined' ? sessionStorage.getItem('terms_agree_user_name') : null;
    if (storedName) setName(storedName);
  }, []);

  useEffect(() => {
    if (!token) return;
    setLoading(true);
    authApi.get('/api/v1/terms')
      .then(res => {
        const list = res.data as TermSummary[];
        setTerms(list);
        if (list.length > 0) setAgreedIds(new Set(list.map(t => t.id)));
      })
      .catch(() => setError('약관 목록을 불러올 수 없습니다.'))
      .finally(() => setLoading(false));
  }, [token]);

  const toggleTerm = (id: string) => {
    setAgreedIds(prev => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const allAgreed = terms.length > 0 && terms.every(t => agreedIds.has(t.id));

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token || !allAgreed) return;
    setSubmitting(true);
    setError(null);
    try {
      await authApi.post('/api/v1/auth/agree', {
        agreementToken: token,
        agreedTermIds: Array.from(agreedIds),
      });
      sessionStorage.removeItem('terms_agree_user_name');
      const stored = typeof window !== 'undefined' ? sessionStorage.getItem('auth_redirect') : null;
      const redirect = getSafeRedirect(stored, '/studio');
      if (typeof window !== 'undefined') sessionStorage.removeItem('auth_redirect');
      window.dispatchEvent(new Event('storage'));
      router.push(redirect);
    } catch (err: unknown) {
      const ax = err as { response?: { status?: number; data?: unknown } };
      if (ax.response?.status === 400) setError('필수 약관에 모두 동의해 주세요.');
      else if (ax.response?.status === 401) setError('동의 유효시간이 만료되었습니다. 다시 로그인해 주세요.');
      else setError('처리 중 오류가 발생했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  if (!token) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4">
        <div className="text-center">
          <p className="text-slate-600 mb-4">유효하지 않은 접근입니다.</p>
          <Link href="/studio" className="text-blue-600 font-bold hover:underline">스튜디오로 이동</Link>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-50 flex items-center justify-center">
        <div className="w-12 h-12 border-4 border-blue-600 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center px-4 py-12">
      <div className="w-full max-w-2xl bg-white rounded-2xl shadow-xl p-8 max-h-[90vh] overflow-y-auto">
        <h1 className="text-2xl font-black text-slate-900 mb-2">약관 동의</h1>
        {name && <p className="text-slate-600 mb-6">{name}님, 서비스 이용을 위해 아래 약관에 동의해 주세요.</p>}
        <form onSubmit={handleSubmit} className="space-y-4">
          {terms.map(t => (
            <div key={t.id} className="flex items-start gap-3 p-4 rounded-xl bg-slate-50 border border-slate-200">
              <input
                type="checkbox"
                id={t.id}
                checked={agreedIds.has(t.id)}
                onChange={() => toggleTerm(t.id)}
                className="mt-1 w-5 h-5 rounded border-slate-300 text-blue-600 focus:ring-blue-500 flex-shrink-0"
              />
              <div className="flex-1 min-w-0">
                <label htmlFor={t.id} className="font-bold text-slate-900 cursor-pointer block">
                  {t.title} (필수)
                </label>
                {t.content && (
                  <div
                    className="mt-2 max-h-32 overflow-y-auto rounded-lg border border-slate-200 bg-white/80 p-3 text-xs text-slate-600 [&_h2]:mt-2 [&_h2]:mb-1 [&_h2]:font-bold [&_h2]:text-slate-700 [&_h2]:first:mt-0 [&_p]:leading-relaxed"
                    dangerouslySetInnerHTML={{ __html: t.content }}
                  />
                )}
                <div className="flex flex-wrap items-center gap-x-3 gap-y-1 mt-1">
                  {(t.allVersions && t.allVersions.length > 0 ? t.allVersions : [{ id: t.id, version: t.version, title: t.title }]).map((v) => (
                    <button
                      key={v.id}
                      type="button"
                      onClick={() => {
                        setTermsModalVersions(t.allVersions && t.allVersions.length > 0 ? t.allVersions : [{ id: t.id, version: t.version, title: t.title }]);
                        setTermsModalInitialId(v.id);
                        setTermsModalOpen(true);
                      }}
                      className="text-sm text-blue-600 hover:underline"
                    >
                      v{v.version} 전문 보기
                    </button>
                  ))}
                </div>
              </div>
            </div>
          ))}
          {error && <p className="text-red-600 text-sm">{error}</p>}
          <Button
            type="submit"
            variant="primary"
            size="lg"
            className="w-full"
            disabled={!allAgreed || submitting}
          >
            {submitting ? '처리 중...' : '동의하고 계속'}
          </Button>
        </form>
        <p className="mt-6 text-center text-slate-500 text-sm">
          동의하지 않으시면 서비스를 이용하실 수 없습니다.
        </p>
      </div>

      <TermsViewerModal
        isOpen={termsModalOpen}
        onClose={() => setTermsModalOpen(false)}
        versions={termsModalVersions}
        initialVersionId={termsModalInitialId}
      />
    </div>
  );
}

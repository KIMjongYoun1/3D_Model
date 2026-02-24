"use client";

import React, { useEffect, useState } from "react";
import { Modal } from "@/components/ui/Modal";
import { authApi } from "@/lib/authApi";

export interface TermVersionItem {
  id: string;
  version: string;
  title: string;
  effectiveAt?: string;
}

interface TermsViewerModalProps {
  isOpen: boolean;
  onClose: () => void;
  versions: TermVersionItem[];
  initialVersionId?: string;
}

export function TermsViewerModal({
  isOpen,
  onClose,
  versions,
  initialVersionId,
}: TermsViewerModalProps) {
  const [selectedId, setSelectedId] = useState<string>(initialVersionId || versions[0]?.id || "");
  const [content, setContent] = useState<string>("");
  const [title, setTitle] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const effectiveVersions = versions.length > 0 ? versions : [];

  useEffect(() => {
    if (initialVersionId && effectiveVersions.some((v) => v.id === initialVersionId)) {
      setSelectedId(initialVersionId);
    } else if (effectiveVersions.length > 0 && !selectedId) {
      setSelectedId(effectiveVersions[0].id);
    }
  }, [initialVersionId, effectiveVersions, selectedId]);

  useEffect(() => {
    if (!isOpen || !selectedId) return;
    setLoading(true);
    authApi
      .get(`/api/v1/terms/${selectedId}`)
      .then((res) => {
        setTitle(res.data.title || "");
        setContent(res.data.content || "");
      })
      .catch(() => setContent(""))
      .finally(() => setLoading(false));
  }, [isOpen, selectedId]);

  if (!isOpen) return null;

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={title || "약관 전문"}
      size="xl"
      footer={
        <button
          type="button"
          onClick={onClose}
          className="px-6 py-2.5 rounded-xl bg-slate-900 text-white font-bold text-sm hover:bg-slate-800 transition-colors"
        >
          닫기
        </button>
      }
    >
      <div className="space-y-4">
        {effectiveVersions.length > 1 && (
          <div className="flex flex-wrap gap-2">
            {effectiveVersions.map((v) => (
              <button
                key={v.id}
                type="button"
                onClick={() => setSelectedId(v.id)}
                className={`px-3 py-1.5 rounded-lg text-sm font-bold transition-colors ${
                  v.id === selectedId
                    ? "bg-blue-600 text-white"
                    : "bg-slate-100 text-slate-600 hover:bg-slate-200"
                }`}
              >
                v{v.version}
              </button>
            ))}
          </div>
        )}
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : (
          <div
            className="max-h-[60vh] overflow-y-auto rounded-xl border border-slate-200 bg-slate-50/50 p-6 text-sm text-slate-700 [&_h2]:mt-6 [&_h2]:mb-2 [&_h2]:text-base [&_h2]:font-bold [&_h2]:first:mt-0 [&_p]:leading-relaxed"
            dangerouslySetInnerHTML={{ __html: content }}
          />
        )}
      </div>
    </Modal>
  );
}

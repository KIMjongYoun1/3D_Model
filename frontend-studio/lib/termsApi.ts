/**
 * 약관 API (가입/결제용)
 */
import { authApi } from "./authApi";

export interface TermsVersionItem {
  id: string;
  version: string;
  title: string;
  effectiveAt?: string;
}

export interface PaymentTermsItem {
  id: string;
  type: string;
  title: string;
  version: string;
  content?: string;
  category?: string;
  required?: boolean;
  allVersions?: TermsVersionItem[];
}

export async function fetchPaymentTerms(): Promise<PaymentTermsItem[]> {
  const { data } = await authApi.get<PaymentTermsItem[]>("/api/v1/terms/payment");
  return data ?? [];
}

export async function fetchTermsDetail(id: string): Promise<{ id: string; title: string; content: string; allVersions?: TermsVersionItem[] }> {
  const { data } = await authApi.get(`/api/v1/terms/${id}`);
  return data;
}

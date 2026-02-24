/**
 * 결제 API 클라이언트 (HttpOnly 쿠키 사용)
 * - request: 주문 생성, orderId·paymentKey 반환
 * - confirm: PG 승인 시뮬레이션
 */
import { authApi } from './authApi';

export interface PaymentRequestPayload {
  planId: string;
  amount: number;
  paymentMethod?: string;
  agreedTermIds?: string[];
}

export interface PaymentRequestResponse {
  orderId: string;
  paymentKey: string;
  amount: number;
  planId: string;
}

export interface PaymentConfirmPayload {
  orderId: string;
  paymentKey: string;
  amount: number;
}

export interface PaymentConfirmResponse {
  id: string;
  status: string;
  amount: number;
  planId?: string;
}

export async function createPaymentRequest(
  payload: PaymentRequestPayload
): Promise<PaymentRequestResponse> {
  const { data } = await authApi.post<PaymentRequestResponse>(
    '/api/v1/payments/request',
    {
      planId: payload.planId,
      amount: payload.amount,
      paymentMethod: payload.paymentMethod || 'card',
      agreedTermIds: payload.agreedTermIds ?? [],
    }
  );
  return data;
}

export async function confirmPayment(
  payload: PaymentConfirmPayload
): Promise<PaymentConfirmResponse> {
  const { data } = await authApi.post<PaymentConfirmResponse>(
    '/api/v1/payments/confirm',
    payload
  );
  return data;
}

export interface PaymentHistoryItem {
  id: string;
  status: string;
  amount: number;
  planId?: string;
  createdAt?: string;
  completedAt?: string;
}

/** 본인 결제 이력 조회 (인증 필요) */
export async function getMyPayments(): Promise<PaymentHistoryItem[]> {
  const { data } = await authApi.get<PaymentHistoryItem[]>('/api/v1/payments/me');
  return data;
}

/** 본인 활성 구독 해지 신청 (인증 필요) */
export async function cancelMySubscription(): Promise<{ message: string }> {
  const { data } = await authApi.post<{ message: string }>('/api/v1/subscriptions/me/cancel');
  return data;
}

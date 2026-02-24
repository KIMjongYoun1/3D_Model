/**
 * 플랜/요금제 API (plan_config 기반)
 */
import axios from "axios";

const SERVICE_API = process.env.NEXT_PUBLIC_SERVICE_API_URL || "http://localhost:8080";

export interface Plan {
  id: string;
  planCode: string;
  planName: string;
  priceMonthly: number;
  tokenLimit: number | null;
  description: string | null;
  features: string[];
  sortOrder: number;
}

export async function fetchPlans(): Promise<Plan[]> {
  const { data } = await axios.get<Plan[]>(`${SERVICE_API}/api/v1/plans`);
  return data;
}

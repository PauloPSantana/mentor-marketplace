import { api } from "@/lib/api";

export type PaymentStatus = "PENDING" | "PAID" | "FAILED" | "REFUNDED" | "CANCELLED";

export type Payment = {
  id: string;
  mentorshipId: string;
  payerId: string;
  amount: number;
  currency: string;
  provider: string;
  providerTransactionId: string;
  status: PaymentStatus;
  idempotencyKey: string;
  paidAt: string | null;
  createdAt: string;
  updatedAt: string;
};

export function paymentStatusLabel(status: PaymentStatus): string {
  switch (status) {
    case "PENDING":
      return "Pendente";
    case "PAID":
      return "Pago";
    case "FAILED":
      return "Falhou";
    case "REFUNDED":
      return "Reembolsado";
    case "CANCELLED":
      return "Cancelado";
    default:
      return status;
  }
}

export function formatMoney(amount: number, currency = "BRL"): string {
  return new Intl.NumberFormat("pt-BR", { style: "currency", currency }).format(amount);
}

export function listMentorshipPayments(mentorshipId: string): Promise<Payment[]> {
  return api<Payment[]>(`/api/v1/mentorships/relationships/${mentorshipId}/payments`);
}

export function createMentorshipPayment(mentorshipId: string): Promise<Payment> {
  return api<Payment>(`/api/v1/mentorships/relationships/${mentorshipId}/payments`, {
    method: "POST",
    headers: { "Idempotency-Key": `m:${mentorshipId}` }
  });
}

export function getPayment(paymentId: string): Promise<Payment> {
  return api<Payment>(`/api/v1/payments/${paymentId}`);
}

export function confirmPayment(paymentId: string): Promise<Payment> {
  return api<Payment>(`/api/v1/payments/${paymentId}/confirm`, { method: "POST" });
}

// Pluggable payment provider abstraction.
// MVP ships a MockProvider (instant success, no credentials) so the whole
// purchase flow works locally. SSLCommerzProvider (bKash/Nagad/cards) is wired
// for production — switch via PAYMENT_PROVIDER env. Stripe/PayPal can be added
// later by implementing the same interface.

export type CreateSessionInput = {
  orderId: string;
  amountCents: number;
  currency: string;
  customerEmail: string;
  successUrl: string;
  cancelUrl: string;
  ipnUrl: string;
};

export type CreateSessionResult = {
  /** Where to send the buyer to pay. For mock, this is the success URL. */
  redirectUrl: string;
  providerRef: string;
};

export type WebhookVerifyResult = {
  ok: boolean;
  orderId?: string;
  providerRef?: string;
  amountCents?: number;
  reason?: string;
};

export interface PaymentProvider {
  readonly id: "MOCK" | "SSLCOMMERZ";
  createSession(input: CreateSessionInput): Promise<CreateSessionResult>;
  /** Verify an incoming IPN/webhook. Never trust client redirects. */
  verifyWebhook(payload: Record<string, string>): Promise<WebhookVerifyResult>;
}

export { MockProvider } from "./mock";
export { SSLCommerzProvider } from "./sslcommerz";

import { MockProvider } from "./mock";
import { SSLCommerzProvider } from "./sslcommerz";
import type { PaymentProvider as PP } from "./index";

export function getPaymentProvider(): PP {
  const id = (process.env.PAYMENT_PROVIDER ?? "mock").toLowerCase();
  if (id === "sslcommerz") {
    return new SSLCommerzProvider({
      storeId: process.env.SSLCOMMERZ_STORE_ID ?? "",
      storePassword: process.env.SSLCOMMERZ_STORE_PASSWORD ?? "",
      sandbox: process.env.SSLCOMMERZ_SANDBOX !== "false",
    });
  }
  return new MockProvider();
}

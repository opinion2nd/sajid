import { randomUUID } from "crypto";
import type {
  PaymentProvider,
  CreateSessionInput,
  CreateSessionResult,
  WebhookVerifyResult,
} from "./index";

/**
 * Dev/demo provider — no credentials required. "Payment" succeeds instantly:
 * the buyer is redirected straight to the success URL, which carries the
 * providerRef the success handler uses to fulfil the order.
 */
export class MockProvider implements PaymentProvider {
  readonly id = "MOCK" as const;

  async createSession(input: CreateSessionInput): Promise<CreateSessionResult> {
    const providerRef = `mock_${randomUUID()}`;
    const url = new URL(input.successUrl);
    url.searchParams.set("orderId", input.orderId);
    url.searchParams.set("providerRef", providerRef);
    url.searchParams.set("provider", "mock");
    return { redirectUrl: url.toString(), providerRef };
  }

  async verifyWebhook(
    payload: Record<string, string>
  ): Promise<WebhookVerifyResult> {
    return {
      ok: true,
      orderId: payload.orderId,
      providerRef: payload.providerRef,
    };
  }
}

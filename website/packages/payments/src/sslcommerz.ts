import type {
  PaymentProvider,
  CreateSessionInput,
  CreateSessionResult,
  WebhookVerifyResult,
} from "./index";

type SSLConfig = {
  storeId: string;
  storePassword: string;
  sandbox: boolean;
};

/**
 * SSLCommerz hosted-checkout provider — covers bKash, Nagad and cards for a
 * Bangladesh audience. Creates a payment session and verifies the IPN
 * server-side (val_id validation) before an order is ever marked paid.
 *
 * Endpoints:
 *   sandbox: https://sandbox.sslcommerz.com
 *   live:    https://securepay.sslcommerz.com
 */
export class SSLCommerzProvider implements PaymentProvider {
  readonly id = "SSLCOMMERZ" as const;
  private base: string;

  constructor(private config: SSLConfig) {
    this.base = config.sandbox
      ? "https://sandbox.sslcommerz.com"
      : "https://securepay.sslcommerz.com";
  }

  async createSession(input: CreateSessionInput): Promise<CreateSessionResult> {
    const tranId = input.orderId;
    const body = new URLSearchParams({
      store_id: this.config.storeId,
      store_passwd: this.config.storePassword,
      total_amount: (input.amountCents / 100).toFixed(2),
      currency: input.currency,
      tran_id: tranId,
      success_url: input.successUrl,
      fail_url: input.cancelUrl,
      cancel_url: input.cancelUrl,
      ipn_url: input.ipnUrl,
      cus_email: input.customerEmail,
      cus_name: "Brother Craft Customer",
      shipping_method: "NO",
      product_name: "Digital goods",
      product_category: "digital",
      product_profile: "non-physical-goods",
    });

    const res = await fetch(`${this.base}/gwprocess/v4/api.php`, {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body,
    });
    const data = (await res.json()) as {
      status: string;
      GatewayPageURL?: string;
      sessionkey?: string;
    };
    if (data.status !== "SUCCESS" || !data.GatewayPageURL) {
      throw new Error("SSLCommerz session creation failed");
    }
    return {
      redirectUrl: data.GatewayPageURL,
      providerRef: data.sessionkey ?? tranId,
    };
  }

  async verifyWebhook(
    payload: Record<string, string>
  ): Promise<WebhookVerifyResult> {
    // Server-side validation: confirm the transaction with SSLCommerz directly
    // using val_id. Do not trust the posted status alone.
    const valId = payload.val_id;
    if (!valId) return { ok: false, reason: "missing val_id" };

    const url = new URL(`${this.base}/validator/api/validationserverAPI.php`);
    url.searchParams.set("val_id", valId);
    url.searchParams.set("store_id", this.config.storeId);
    url.searchParams.set("store_passwd", this.config.storePassword);
    url.searchParams.set("format", "json");

    const res = await fetch(url);
    const data = (await res.json()) as {
      status: string;
      tran_id: string;
      amount: string;
    };
    const ok = data.status === "VALID" || data.status === "VALIDATED";
    return {
      ok,
      orderId: data.tran_id,
      providerRef: valId,
      amountCents: Math.round(parseFloat(data.amount) * 100),
      reason: ok ? undefined : `status ${data.status}`,
    };
  }
}

import { describe, it, expect } from "vitest";
import { generateKey } from "./keygen";

describe("generateKey", () => {
  it("formats as PREFIX-XXXX-XXXX-XXXX-XXXX", () => {
    const key = generateKey("BC");
    expect(key).toMatch(/^BC-[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}-[A-Z2-9]{4}$/);
  });

  it("preserves the original AFC prefix for backwards compatibility", () => {
    const key = generateKey("AFC");
    expect(key.startsWith("AFC-")).toBe(true);
  });

  it("never emits ambiguous characters (0, O, 1, I)", () => {
    for (let i = 0; i < 200; i++) {
      const body = generateKey("BC").slice(3);
      expect(body).not.toMatch(/[01OI]/);
    }
  });

  it("is effectively unique across many calls", () => {
    const keys = new Set(Array.from({ length: 1000 }, () => generateKey()));
    expect(keys.size).toBe(1000);
  });
});

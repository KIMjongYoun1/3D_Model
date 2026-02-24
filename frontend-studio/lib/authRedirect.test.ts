import { describe, it, expect } from "vitest";
import { isValidRedirect, getSafeRedirect } from "./authRedirect";

describe("authRedirect", () => {
  describe("isValidRedirect", () => {
    it("절대 경로 허용", () => {
      expect(isValidRedirect("/")).toBe(true);
      expect(isValidRedirect("/studio")).toBe(true);
      expect(isValidRedirect("/payment")).toBe(true);
    });

    it("javascript: URL 거부", () => {
      expect(isValidRedirect("javascript:alert(1)")).toBe(false);
    });

    it("외부 URL 거부", () => {
      expect(isValidRedirect("https://evil.com")).toBe(false);
    });
  });

  describe("getSafeRedirect", () => {
    it("유효한 경로는 그대로 반환", () => {
      expect(getSafeRedirect("/payment", "/")).toBe("/payment");
    });

    it("유효하지 않으면 fallback 반환", () => {
      expect(getSafeRedirect("javascript:alert(1)", "/")).toBe("/");
    });
  });
});

import { describe, it, expect } from "vitest";
import { isValidRedirect, getSafeRedirect } from "./authRedirect";

describe("authRedirect", () => {
  describe("isValidRedirect", () => {
    it("절대 경로 허용", () => {
      expect(isValidRedirect("/")).toBe(true);
      expect(isValidRedirect("/knowledge")).toBe(true);
      expect(isValidRedirect("/members")).toBe(true);
      expect(isValidRedirect("/dashboard")).toBe(true);
    });

    it("null/undefined/빈 문자열 거부", () => {
      expect(isValidRedirect(null)).toBe(false);
      expect(isValidRedirect(undefined)).toBe(false);
      expect(isValidRedirect("")).toBe(false);
      expect(isValidRedirect("   ")).toBe(false);
    });

    it("javascript: URL 거부", () => {
      expect(isValidRedirect("javascript:alert(1)")).toBe(false);
      expect(isValidRedirect("javascript:void(0)")).toBe(false);
      expect(isValidRedirect("  javascript:evil()  ")).toBe(false);
    });

    it("data: URL 거부", () => {
      expect(isValidRedirect("data:text/html,<script>alert(1)</script>")).toBe(false);
    });

    it("외부 URL 거부", () => {
      expect(isValidRedirect("https://evil.com")).toBe(false);
      expect(isValidRedirect("http://phishing.com")).toBe(false);
      expect(isValidRedirect("//evil.com")).toBe(false);
    });

    it("경로 탐색(..) 거부", () => {
      expect(isValidRedirect("/../etc/passwd")).toBe(false);
      expect(isValidRedirect("/knowledge/../admin")).toBe(false);
    });

    it("위험 문자 거부", () => {
      expect(isValidRedirect("/path with space")).toBe(false);
      expect(isValidRedirect("/path<script>")).toBe(false);
    });

    it("최대 길이 초과 거부", () => {
      const long = "/" + "a".repeat(300);
      expect(isValidRedirect(long)).toBe(false);
    });
  });

  describe("getSafeRedirect", () => {
    it("유효한 경로는 그대로 반환", () => {
      expect(getSafeRedirect("/knowledge", "/")).toBe("/knowledge");
      expect(getSafeRedirect("/members", "/")).toBe("/members");
    });

    it("유효하지 않으면 fallback 반환", () => {
      expect(getSafeRedirect("javascript:alert(1)", "/")).toBe("/");
      expect(getSafeRedirect("https://evil.com", "/login")).toBe("/login");
      expect(getSafeRedirect(null, "/dashboard")).toBe("/dashboard");
      expect(getSafeRedirect("", "/")).toBe("/");
    });
  });
});

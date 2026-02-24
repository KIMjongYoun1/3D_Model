import { describe, it, expect } from "vitest";
import { isSafeExternalUrl, getSafeExternalUrl } from "./safeUrl";

describe("safeUrl", () => {
  describe("isSafeExternalUrl", () => {
    it("http/https URL 허용", () => {
      expect(isSafeExternalUrl("https://example.com")).toBe(true);
      expect(isSafeExternalUrl("http://example.com")).toBe(true);
    });

    it("javascript: URL 거부", () => {
      expect(isSafeExternalUrl("javascript:alert(1)")).toBe(false);
    });

    it("data: URL 거부", () => {
      expect(isSafeExternalUrl("data:text/html,<script>")).toBe(false);
    });

    it("file: URL 거부", () => {
      expect(isSafeExternalUrl("file:///etc/passwd")).toBe(false);
    });

    it("null/undefined/빈 문자열 거부", () => {
      expect(isSafeExternalUrl(null)).toBe(false);
      expect(isSafeExternalUrl(undefined)).toBe(false);
      expect(isSafeExternalUrl("")).toBe(false);
    });
  });

  describe("getSafeExternalUrl", () => {
    it("유효한 URL은 그대로 반환", () => {
      expect(getSafeExternalUrl("https://example.com")).toBe("https://example.com");
    });

    it("유효하지 않으면 null 반환", () => {
      expect(getSafeExternalUrl("javascript:alert(1)")).toBe(null);
      expect(getSafeExternalUrl(null)).toBe(null);
    });
  });
});

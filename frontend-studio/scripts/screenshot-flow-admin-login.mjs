#!/usr/bin/env node
/**
 * Admin 로그인 세션 저장 (스크린샷 캡처 전)
 * ADMIN_EMAIL, ADMIN_PASSWORD 환경변수 필요
 */

import { chromium } from 'playwright';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const BASE_URL = process.env.BASE_URL_ADMIN || 'http://localhost:3001';
const AUTH_STATE_PATH = join(__dirname, '../../.auth-state-admin.json');

async function main() {
  const email = process.env.ADMIN_EMAIL;
  const password = process.env.ADMIN_PASSWORD;

  if (!email || !password) {
    console.log('\n⚠️  ADMIN_EMAIL, ADMIN_PASSWORD 환경변수를 설정하세요.');
    console.log('   예: ADMIN_EMAIL=admin@example.com ADMIN_PASSWORD=xxx npm run screenshot-flow:admin-login\n');
    process.exit(1);
  }

  console.log('\n🔐 Admin 로그인 세션 저장 중...\n');

  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: { width: 1280, height: 720 } });
  const page = await context.newPage();

  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });
  await page.fill('input[type="email"]', email);
  await page.fill('input[type="password"]', password);
  await page.click('button[type="submit"]');

  try {
    await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 10000 });
    await context.storageState({ path: AUTH_STATE_PATH });
    console.log('   ✅ 로그인 완료, 세션 저장:', AUTH_STATE_PATH);
  } catch (e) {
    console.log('   ❌ 로그인 실패. 이메일/비밀번호 확인.');
  } finally {
    await browser.close();
  }
}

main().catch(console.error);

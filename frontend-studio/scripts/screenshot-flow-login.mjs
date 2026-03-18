#!/usr/bin/env node
/**
 * 로그인 세션 저장 (스크린샷 캡처 전 1회 실행)
 *
 * 브라우저가 열리면 네이버로 로그인 후 /studio 또는 /mypage에 도착하면
 * 세션이 자동 저장됩니다. 이후 screenshot-flow는 이 세션을 사용합니다.
 */

import { chromium } from 'playwright';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const AUTH_STATE_PATH = join(__dirname, '../../.auth-state-studio.json');

async function main() {
  console.log('\n🔐 로그인 세션 저장 모드');
  console.log('   브라우저에서 네이버 로그인 후 /studio 또는 /mypage에 도착하세요.');
  console.log('   도착 시 자동으로 세션이 저장되고 브라우저가 종료됩니다.\n');

  const browser = await chromium.launch({
    headless: false,
    slowMo: 100,
  });

  const context = await browser.newContext({
    viewport: { width: 1280, height: 720 },
  });

  const page = await context.newPage();
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle' });

  // /studio 또는 /mypage 도착 시 세션 저장
  try {
    await page.waitForURL(
      (url) => url.pathname === '/studio' || url.pathname === '/mypage',
      { timeout: 120000 }
    );
    await context.storageState({ path: AUTH_STATE_PATH });
    console.log('   ✅ 로그인 완료, 세션 저장됨:', AUTH_STATE_PATH);
  } catch (e) {
    console.log('   ⏱ 2분 내 로그인 미완료. 수동으로 /studio 접속 후 다시 실행하세요.');
  } finally {
    await browser.close();
  }
}

main().catch(console.error);

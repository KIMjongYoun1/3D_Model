#!/usr/bin/env node
/**
 * UI 플로우용 스크린샷 자동 캡처
 *
 * 사용법:
 * 1. (최초 1회) npm run screenshot-flow:login  → 브라우저 열림, 네이버 로그인 후 /studio 도착 시 자동 저장
 * 2. 터미널 1: npm run dev
 * 3. 터미널 2: npm run screenshot-flow
 */

import { chromium } from 'playwright';
import { mkdirSync, existsSync, readdirSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));

const APP_DIR = join(__dirname, '../app');
const BASE_URL = process.env.BASE_URL || 'http://localhost:3000';
const OUTPUT_DIR = join(__dirname, '../../docs/portfolio/screenshots/studio');
const AUTH_STATE_PATH = join(__dirname, '../../.auth-state-studio.json');

/** app/ 폴더에서 page.tsx 기반 라우트 자동 추출 */
function discoverRoutes() {
  const routes = [];

  function scan(dir, basePath = '') {
    const entries = readdirSync(dir, { withFileTypes: true });
    for (const e of entries) {
      const fullPath = join(dir, e.name);
      const segment = e.name;

      if (e.isDirectory()) {
        // (auth) 같은 route group은 경로에 포함 안 함
        const pathPart = segment.startsWith('(') && segment.endsWith(')')
          ? ''
          : `/${segment}`;
        scan(fullPath, basePath + pathPart);
      } else if (e.name === 'page.tsx' || e.name === 'page.js') {
        let path = basePath || '/';
        // [id], [slug] 등 동적 세그먼트 → 샘플 값 사용
        path = path.replace(/\[[^\]]+\]/g, (m) => {
          if (m === '[id]') return '1';
          if (m === '[slug]') return 'sample';
          return '1';
        });
        const name = path === '/' ? 'home' : path.slice(1).replace(/\//g, '-').replace(/\[|\]/g, '');
        // api 라우트 제외
        if (!path.startsWith('/api')) {
          routes.push({ path: path || '/', name: name || 'home' });
        }
      }
    }
  }

  if (existsSync(APP_DIR)) {
    scan(APP_DIR);
  }

  // 중복 제거 (같은 path)
  const seen = new Set();
  return routes.filter((r) => {
    const key = r.path;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

async function main() {
  const routes = discoverRoutes();

  if (!existsSync(OUTPUT_DIR)) {
    mkdirSync(OUTPUT_DIR, { recursive: true });
  }

  const useAuth = existsSync(AUTH_STATE_PATH);
  console.log(`\n📸 스크린샷 캡처 시작 (${BASE_URL})`);
  console.log(`   라우트 ${routes.length}개, 로그인 세션: ${useAuth ? '사용' : '미사용'}\n`);

  const launchOpts = { headless: true };
  const contextOpts = {
    viewport: { width: 1280, height: 720 },
    ...(useAuth && { storageState: AUTH_STATE_PATH }),
  };

  const browser = await chromium.launch({
    ...launchOpts,
    args: [
      '--use-gl=angle',
      '--use-angle=swiftshader',
      '--ignore-gpu-blocklist',
    ],
  });
  const context = await browser.newContext(contextOpts);

  for (const { path, name } of routes) {
    const url = `${BASE_URL}${path}`;
    try {
      const page = await context.newPage();
      await page.goto(url, { waitUntil: 'networkidle', timeout: 15000 });
      await page.waitForTimeout(1500);

      const filePath = join(OUTPUT_DIR, `${name}.png`);
      await page.screenshot({ path: filePath, fullPage: true });
      await page.close();

      console.log(`  ✅ ${path} → ${name}.png`);
    } catch (err) {
      console.log(`  ❌ ${path} 실패: ${err.message}`);
    }
  }

  await browser.close();
  console.log(`\n📁 저장 위치: ${OUTPUT_DIR}\n`);
}

main().catch(console.error);

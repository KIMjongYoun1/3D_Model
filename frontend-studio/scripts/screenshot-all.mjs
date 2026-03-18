#!/usr/bin/env node
/**
 * 프로젝트 전체 UI 스크린샷 상세 캡처 (Studio + Admin)
 *
 * 사용법:
 * 1. ./start.sh 로 전체 프로젝트 기동
 * 2. npm run screenshot-all              → 세션 있으면 사용, 없으면 비로그인 캡처
 * 3. npm run screenshot-all:login        → 브라우저 열림, 로그인 후 세션 저장 + 캡처
 */

import { chromium } from 'playwright';
import { mkdirSync, existsSync, readdirSync, readFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = join(__dirname, '../..');

// .screenshot.env에서 ADMIN_EMAIL, ADMIN_PASSWORD 로드 (있으면)
const envPath = join(ROOT, '.screenshot.env');
if (existsSync(envPath)) {
  readFileSync(envPath, 'utf8').split('\n').forEach((line) => {
    const m = line.match(/^\s*([A-Z_]+)=(.*)$/);
    if (m && !process.env[m[1]]) process.env[m[1]] = m[2].trim();
  });
}

const CONFIG = {
  studio: {
    appDir: join(ROOT, 'frontend-studio/app'),
    baseUrl: 'http://localhost:3000',
    outputDir: join(ROOT, 'docs/portfolio/screenshots/studio'),
    authState: join(ROOT, '.auth-state-studio.json'),
  },
  admin: {
    appDir: join(ROOT, 'frontend-admin/app'),
    baseUrl: 'http://localhost:3001',
    outputDir: join(ROOT, 'docs/portfolio/screenshots/admin'),
    authState: join(ROOT, '.auth-state-admin.json'),
  },
};

// 상세 캡처 설정
const VIEWPORT = { width: 1920, height: 1080 };
const WAIT_AFTER_LOAD = 2500;
const TIMEOUT = 30000;

const DO_LOGIN = process.argv.includes('--login');

/** Studio 세션 저장 (브라우저 열고 네이버 로그인 대기) */
async function saveStudioSession() {
  console.log('\n🔐 [Studio] 브라우저에서 네이버 로그인 후 /studio 또는 /mypage에 도착하세요.\n');
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: VIEWPORT });
  const page = await context.newPage();
  await page.goto(`${CONFIG.studio.baseUrl}/login`, { waitUntil: 'networkidle' });
  try {
    await page.waitForURL((url) => url.pathname === '/studio' || url.pathname === '/mypage', { timeout: 120000 });
    await context.storageState({ path: CONFIG.studio.authState });
    console.log('   ✅ Studio 세션 저장됨\n');
  } catch (e) {
    console.log('   ⏱ 2분 내 로그인 미완료. 비로그인 상태로 캡처합니다.\n');
  } finally {
    await browser.close();
  }
}

/** Admin 세션 저장 (이메일/비밀번호 또는 수동 로그인) */
async function saveAdminSession() {
  const email = process.env.ADMIN_EMAIL;
  const password = process.env.ADMIN_PASSWORD;

  if (email && password) {
    console.log('\n🔐 [Admin] 자동 로그인 시도...\n');
    const browser = await chromium.launch({ headless: false });
    const context = await browser.newContext({ viewport: VIEWPORT });
    const page = await context.newPage();
    await page.goto(`${CONFIG.admin.baseUrl}/login`, { waitUntil: 'networkidle' });
    await page.fill('input[type="email"]', email);
    await page.fill('input[type="password"]', password);
    await page.click('button[type="submit"]');
    try {
      await page.waitForURL((url) => !url.pathname.includes('/login'), { timeout: 10000 });
      await context.storageState({ path: CONFIG.admin.authState });
      console.log('   ✅ Admin 세션 저장됨\n');
    } catch (e) {
      console.log('   ❌ Admin 로그인 실패. 수동 로그인 모드로 전환.\n');
      await browser.close();
      await saveAdminSessionManual();
      return;
    }
    await browser.close();
  } else {
    await saveAdminSessionManual();
  }
}

async function saveAdminSessionManual() {
  console.log('\n🔐 [Admin] 브라우저에서 로그인 후 대시보드에 도착하세요.\n');
  const browser = await chromium.launch({ headless: false });
  const context = await browser.newContext({ viewport: VIEWPORT });
  const page = await context.newPage();
  await page.goto(`${CONFIG.admin.baseUrl}/login`, { waitUntil: 'networkidle' });
  try {
    await page.waitForURL((url) => url.pathname !== '/login' && url.pathname !== '/register', { timeout: 120000 });
    await context.storageState({ path: CONFIG.admin.authState });
    console.log('   ✅ Admin 세션 저장됨\n');
  } catch (e) {
    console.log('   ⏱ 2분 내 로그인 미완료. 비로그인 상태로 캡처합니다.\n');
  } finally {
    await browser.close();
  }
}

/** app/ 폴더에서 page.tsx 기반 라우트 자동 추출 */
function discoverRoutes(appDir) {
  const routes = [];

  function scan(dir, basePath = '') {
    if (!existsSync(dir)) return;
    const entries = readdirSync(dir, { withFileTypes: true });
    for (const e of entries) {
      const segment = e.name;

      if (e.isDirectory()) {
        const pathPart = segment.startsWith('(') && segment.endsWith(')')
          ? ''
          : `/${segment}`;
        scan(join(dir, e.name), basePath + pathPart);
      } else if (e.name === 'page.tsx' || e.name === 'page.js') {
        let path = basePath || '/';
        path = path.replace(/\[[^\]]+\]/g, (m) => (m === '[id]' ? '1' : m === '[slug]' ? 'sample' : '1'));
        const name = path === '/' ? 'home' : path.slice(1).replace(/\//g, '-').replace(/\[|\]/g, '');
        if (!path.startsWith('/api')) {
          routes.push({ path: path || '/', name: name || 'home' });
        }
      }
    }
  }

  scan(appDir);
  const seen = new Set();
  return routes.filter((r) => {
    if (seen.has(r.path)) return false;
    seen.add(r.path);
    return true;
  });
}

/** API에서 첫 번째 약관 UUID 조회 (terms 페이지 캡처용) */
async function fetchFirstTermsId() {
  try {
    const res = await fetch('http://localhost:8080/api/v1/terms');
    if (!res.ok) return null;
    const data = await res.json();
    if (Array.isArray(data) && data.length > 0 && data[0].id) return data[0].id;
    return null;
  } catch (_) {
    return null;
  }
}

async function captureApp(appName) {
  const cfg = CONFIG[appName];
  let routes = discoverRoutes(cfg.appDir);

  // Studio: terms/1 → API에서 조회한 실제 UUID로 교체 (없으면 terms 제외)
  if (appName === 'studio') {
    const termsId = await fetchFirstTermsId();
    if (termsId) {
      routes = routes.map((r) =>
        r.path === '/terms/1' ? { ...r, path: `/terms/${termsId}` } : r
      );
      console.log('   📋 약관 ID 조회됨 → /terms/' + termsId.slice(0, 8) + '...\n');
    } else {
      routes = routes.filter((r) => r.path !== '/terms/1');
      console.log('   ⚠️  약관 없음. Admin에서 약관 등록 후 다시 캡처하세요.\n');
    }
  }

  mkdirSync(cfg.outputDir, { recursive: true });
  const useAuth = existsSync(cfg.authState);

  console.log(`\n📸 [${appName.toUpperCase()}] ${cfg.baseUrl} — ${routes.length}개 라우트, 로그인: ${useAuth ? 'O' : 'X'}\n`);

  const browser = await chromium.launch({
    headless: true,
    args: [
      '--use-gl=angle',
      '--use-angle=swiftshader',
      '--ignore-gpu-blocklist',
    ],
  });
  const context = await browser.newContext({
    viewport: VIEWPORT,
    deviceScaleFactor: 1,
    ...(useAuth && { storageState: cfg.authState }),
  });

  for (const { path, name } of routes) {
    const url = `${cfg.baseUrl}${path}`;
    try {
      const page = await context.newPage();
      await page.goto(url, { waitUntil: 'networkidle', timeout: TIMEOUT });

      // Studio 약관 보기(/terms/uuid): API 로드 후 "닫기" 버튼 등 실제 콘텐츠 로드 대기 (실패 시 /studio로 리다이렉트됨)
      if (appName === 'studio' && path.match(/^\/terms\/[a-f0-9-]+$/)) {
        const contentLoaded = await page.getByRole('button', { name: '닫기' }).waitFor({ timeout: 10000 }).then(() => true).catch(() => false);
        const onTerms = new URL(page.url()).pathname.startsWith('/terms/');
        if (!contentLoaded || !onTerms) {
          console.log(`  ⚠️ ${path} → 약관 로드 실패 (API 오류 또는 리다이렉트), 스킵`);
          await page.close();
          continue;
        }
      }

      await page.waitForTimeout(WAIT_AFTER_LOAD);

      const filePath = join(cfg.outputDir, `${name}.png`);
      await page.screenshot({ path: filePath, fullPage: true });
      await page.close();

      console.log(`  ✅ ${path} → ${name}.png`);
    } catch (err) {
      console.log(`  ❌ ${path} 실패: ${err.message}`);
    }
  }

  await browser.close();
  return routes;
}

/** Studio 추가 캡처: 비로그인(온보딩) 화면 */
async function captureStudioGuest() {
  const cfg = CONFIG.studio;
  if (!existsSync(cfg.authState)) return;
  console.log('\n📸 [STUDIO] 비로그인(온보딩) 화면 추가 캡처\n');
  const browser = await chromium.launch({
    headless: true,
    args: ['--use-gl=angle', '--use-angle=swiftshader', '--ignore-gpu-blocklist'],
  });
  const context = await browser.newContext({ viewport: VIEWPORT });
  const page = await context.newPage();
  try {
    await page.goto(`${cfg.baseUrl}/studio`, { waitUntil: 'networkidle', timeout: TIMEOUT });
    await page.waitForTimeout(WAIT_AFTER_LOAD);
    await page.screenshot({ path: join(cfg.outputDir, 'studio-guest.png'), fullPage: true });
    console.log('  ✅ /studio (비로그인) → studio-guest.png');
  } catch (err) {
    console.log('  ❌ studio-guest 실패:', err.message);
  }
  await browser.close();
}

async function main() {
  console.log('\n🖼 Quantum Studio 전체 UI 스크린샷 캡처');
  console.log('   Studio(3000) + Admin(3001) 상세 캡처 (1920x1080, fullPage)\n');

  if (DO_LOGIN) {
    console.log('=== 로그인 모드: 세션 저장 후 캡처 ===\n');
    if (!existsSync(CONFIG.studio.authState)) await saveStudioSession();
    else console.log('   [Studio] 기존 세션 사용\n');
    if (!existsSync(CONFIG.admin.authState)) await saveAdminSession();
    else console.log('   [Admin] 기존 세션 사용\n');
  }

  await captureApp('studio');
  if (existsSync(CONFIG.studio.authState)) await captureStudioGuest();
  await captureApp('admin');

  console.log('\n📁 저장 위치:');
  console.log(`   Studio: ${CONFIG.studio.outputDir}`);
  console.log(`   Admin:  ${CONFIG.admin.outputDir}`);
  console.log('\n🔄 플로우차트 생성 중...');
  const { spawnSync } = await import('child_process');
  spawnSync('node', [join(ROOT, 'scripts', 'generate-ui-flow.mjs')], { cwd: ROOT, stdio: 'inherit' });
  console.log('\n✅ 완료. docs/portfolio/ui-flow.html 에서 플로우차트 확인하세요.\n');
}

main().catch(console.error);

#!/usr/bin/env node
/**
 * 스크린샷 → 화면별 1장씩 PDF (제출용 포트폴리오)
 * 각 화면을 페이지로 나눠 하나씩 볼 수 있음
 *
 * npm run export-flow
 */

import { chromium } from 'playwright';
import { join, dirname } from 'path';
import { readdirSync, existsSync, writeFileSync } from 'fs';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = join(__dirname, '../..');
const PORTFOLIO = join(ROOT, 'docs/portfolio');
const STUDIO_DIR = join(PORTFOLIO, 'screenshots/studio');
const ADMIN_DIR = join(PORTFOLIO, 'screenshots/admin');

// 홈(/)은 /studio로 즉시 리다이렉트, auth-agree는 토큰 필요 → 제외
// Admin 상세/편집(id=1)은 UUID 필요 → "잘못된 요청" 나오므로 제외
const EXCLUDE = {
  studio: ['auth-agree', 'home'],
  admin: ['register', 'members-1', 'terms-1-edit', 'plans-1-edit', 'knowledge-1'],
};
const LABELS = {
  studio: { home: '홈', login: '로그인', studio: '스튜디오', mypage: '마이페이지', payment: '결제', 'terms-1': '약관 보기' },
  admin: { login: '관리자 로그인', home: '대시보드', dashboard: '대시보드', members: '회원관리', 'members-1': '회원 상세', transactions: '거래관리', subscriptions: '구독관리', plans: '플랜관리', 'plans-new': '플랜 등록', 'plans-1-edit': '플랜 수정', terms: '약관관리', 'terms-new': '약관 등록', 'terms-1-edit': '약관 수정', knowledge: '지식 베이스', 'knowledge-1': '지식 상세', ai: 'AI 어시스턴트' },
};

function listFiles(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir).filter((f) => f.endsWith('.png')).map((f) => f.replace('.png', ''));
}

function buildPortfolioHtml(app, files, title) {
  const items = files
    .filter((f) => !EXCLUDE[app].includes(f))
    .map((name) => {
      const label = LABELS[app][name] || name.replace(/-/g, ' ');
      return `
    <div class="page">
      <div class="label">${label}</div>
      <img src="screenshots/${app}/${name}.png" alt="${label}" />
    </div>`;
    })
    .join('');
  return `<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>${title}</title>
  <style>
    * { box-sizing: border-box; }
    body { margin: 0; padding: 0; background: #0f172a; color: #e2e8f0; font-family: sans-serif; }
    .page { page-break-after: always; padding: 24px; min-height: 100vh; display: flex; flex-direction: column; align-items: center; justify-content: flex-start; }
    .page:last-child { page-break-after: avoid; }
    .page .label { font-size: 1.25rem; font-weight: 700; margin-bottom: 16px; color: #38bdf8; }
    .page img { max-width: 100%; height: auto; border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.3); }
    @media print { .page { min-height: auto; padding: 16px; } }
  </style>
</head>
<body>${items}
</body>
</html>`;
}

async function main() {
  const studioFiles = listFiles(STUDIO_DIR);
  const adminFiles = listFiles(ADMIN_DIR);

  const studioHtml = buildPortfolioHtml('studio', studioFiles, 'Quantum Studio — 사용자 플로우');
  const adminHtml = buildPortfolioHtml('admin', adminFiles, 'Quantum Studio — 관리자 플로우');

  const studioHtmlPath = join(PORTFOLIO, '_portfolio-studio.html');
  const adminHtmlPath = join(PORTFOLIO, '_portfolio-admin.html');
  writeFileSync(studioHtmlPath, studioHtml);
  writeFileSync(adminHtmlPath, adminHtml);

  console.log('\n📄 화면별 PDF 생성 (한 장씩 볼 수 있음)\n');

  const browser = await chromium.launch({
    headless: true,
    args: ['--use-gl=angle', '--use-angle=swiftshader', '--ignore-gpu-blocklist'],
  });

  const context = await browser.newContext({ viewport: { width: 1200, height: 800 } });

  const page = await context.newPage();

  await page.goto(`file://${studioHtmlPath}`, { waitUntil: 'networkidle', timeout: 10000 });
  await page.waitForTimeout(1000);
  await page.pdf({
    path: join(PORTFOLIO, 'studio-flow.pdf'),
    format: 'A4',
    printBackground: true,
    margin: { top: '20px', right: '20px', bottom: '20px', left: '20px' },
  });
  console.log(`  ✅ studio-flow.pdf (${studioFiles.filter((f) => !EXCLUDE.studio.includes(f)).length}페이지)`);

  await page.goto(`file://${adminHtmlPath}`, { waitUntil: 'networkidle', timeout: 10000 });
  await page.waitForTimeout(1000);
  await page.pdf({
    path: join(PORTFOLIO, 'admin-flow.pdf'),
    format: 'A4',
    printBackground: true,
    margin: { top: '20px', right: '20px', bottom: '20px', left: '20px' },
  });
  console.log(`  ✅ admin-flow.pdf (${adminFiles.filter((f) => !EXCLUDE.admin.includes(f)).length}페이지)`);

  await browser.close();

  // 임시 HTML 삭제
  try {
    const { unlinkSync } = await import('fs');
    unlinkSync(studioHtmlPath);
    unlinkSync(adminHtmlPath);
  } catch (_) {}

  console.log(`\n📁 ${PORTFOLIO}/`);
  console.log('   PDF를 열어 페이지를 넘기면 화면을 하나씩 볼 수 있습니다.\n');
}

main().catch(console.error);

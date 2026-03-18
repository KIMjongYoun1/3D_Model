#!/usr/bin/env node
/**
 * 캡처된 스크린샷 기반 플로우차트 HTML 자동 생성
 * screenshot-all 실행 후 실행
 */

import { readdirSync, existsSync, writeFileSync, readFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
const ROOT = join(__dirname, '..');
const STUDIO_DIR = join(ROOT, 'docs/portfolio/screenshots/studio');

// 메인 URL (.screenshot.env 또는 MAIN_URL 환경변수. 없으면 비표시)
let MAIN_URL = process.env.MAIN_URL || '';
const envPath = join(ROOT, '.screenshot.env');
if (!MAIN_URL && existsSync(envPath)) {
  const m = readFileSync(envPath, 'utf8').match(/MAIN_URL=(.+)/);
  if (m) MAIN_URL = m[1].trim();
}
const ADMIN_DIR = join(ROOT, 'docs/portfolio/screenshots/admin');
const OUTPUT = join(ROOT, 'docs/portfolio/ui-flow.html');

// 유효하지 않은 접근, 잘못된 요청, 에러 등이 포함된 화면 제외
// 홈(/)은 /studio로 즉시 리다이렉트, auth-agree는 토큰 필요 → 제외
// Admin 상세/편집(id=1)은 UUID 필요 → "잘못된 요청" 나오므로 제외
const EXCLUDE = {
  studio: ['auth-agree', 'home'],
  admin: ['register', 'members-1', 'terms-1-edit', 'plans-1-edit', 'knowledge-1'],
};

const LABELS = {
  studio: {
    home: ['홈', '/'],
    login: ['로그인', '/login'],
    studio: ['스튜디오', '/studio'],
    'studio-guest': ['스튜디오 (온보딩)', '/studio'],
    mypage: ['마이페이지', '/mypage'],
    payment: ['결제', '/payment'],
    'terms-1': ['약관 보기', '/terms/1'],
  },
  admin: {
    login: ['관리자 로그인', '/login'],
    home: ['대시보드', '/'],
    dashboard: ['대시보드', '/dashboard'],
    members: ['회원관리', '/members'],
    'members-1': ['회원 상세', '/members/1'],
    transactions: ['거래관리', '/transactions'],
    subscriptions: ['구독관리', '/subscriptions'],
    plans: ['플랜관리', '/plans'],
    'plans-new': ['플랜 등록', '/plans/new'],
    'plans-1-edit': ['플랜 수정', '/plans/1/edit'],
    terms: ['약관관리', '/terms'],
    'terms-new': ['약관 등록', '/terms/new'],
    'terms-1-edit': ['약관 수정', '/terms/1/edit'],
    knowledge: ['지식 베이스', '/knowledge'],
    'knowledge-1': ['지식 상세', '/knowledge/1'],
    ai: ['AI 어시스턴트', '/ai'],
  },
};

function listScreenshots(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir)
    .filter((f) => f.endsWith('.png'))
    .map((f) => f.replace('.png', ''));
}

function nodeHtml(app, name, labels) {
  const fallbackPath = '/' + name.replace(/-/g, '/');
  const [label, path] = labels[name] || [name.replace(/-/g, ' '), fallbackPath];
  return `<div class="node"><img src="screenshots/${app}/${name}.png" alt="${name}" onerror="this.parentElement.innerHTML='<div class=label>${label}</div><p style=padding:40px;color:#64748b>이미지 없음</p>'"><div class="label">${label}<div class="path">${path}</div></div></div>`;
}

function main() {
  let studioFiles = listScreenshots(STUDIO_DIR).filter((f) => !EXCLUDE.studio.includes(f));
  let adminFiles = listScreenshots(ADMIN_DIR).filter((f) => !EXCLUDE.admin.includes(f));

  const studioFlow = ['home', 'login', 'studio', 'mypage', 'payment'].filter((f) => studioFiles.includes(f));
  const studioOthers = studioFiles.filter((f) => !studioFlow.includes(f));

  const adminFlow = ['login', 'home', 'dashboard'].filter((f) => adminFiles.includes(f));
  const adminOthers = adminFiles.filter((f) => !adminFlow.includes(f));

  const studioFlowHtml = studioFlow.map((n) => nodeHtml('studio', n, LABELS.studio)).join('\n      <span class="arrow">→</span>\n      ');
  const studioOthersHtml = studioOthers.map((n) => nodeHtml('studio', n, LABELS.studio)).join('\n      ');
  const adminFlowHtml = adminFlow.map((n) => nodeHtml('admin', n, LABELS.admin)).join('\n      <span class="arrow">→</span>\n      ');
  const adminOthersHtml = adminOthers.map((n) => nodeHtml('admin', n, LABELS.admin)).join('\n      ');

  const html = `<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Quantum Studio UI 플로우</title>
  <style>
    * { box-sizing: border-box; }
    body { font-family: 'Pretendard', -apple-system, sans-serif; margin: 0; padding: 24px; background: #0f172a; color: #e2e8f0; }
    h1 { font-size: 1.5rem; margin-bottom: 8px; }
    .sub { color: #94a3b8; font-size: 0.875rem; margin-bottom: 24px; }
    .section { margin-bottom: 48px; }
    .section h2 { font-size: 1.125rem; color: #38bdf8; margin-bottom: 16px; border-bottom: 1px solid #334155; padding-bottom: 8px; }
    .flow { display: flex; flex-wrap: wrap; gap: 24px; align-items: flex-start; }
    .node { position: relative; background: #1e293b; border-radius: 12px; overflow: hidden; border: 1px solid #334155; max-width: 320px; }
    .node img { display: block; width: 100%; height: auto; max-height: 400px; object-fit: contain; }
    .node .label { padding: 8px 12px; font-size: 0.8125rem; font-weight: 600; background: #0f172a; }
    .node .path { font-size: 0.75rem; color: #64748b; margin-top: 2px; }
    .arrow { color: #64748b; font-size: 1.5rem; align-self: center; }
    .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
    .grid .node { max-width: none; }
    .tabs { display: flex; gap: 8px; margin-bottom: 16px; }
    .tabs button { padding: 8px 16px; border: 1px solid #334155; background: #1e293b; color: #94a3b8; border-radius: 8px; cursor: pointer; }
    .tabs button.active { background: #38bdf8; color: #0f172a; border-color: #38bdf8; }
    .tabs button:hover:not(.active) { background: #334155; }
  </style>
</head>
<body>
  <h1>Quantum Studio UI 플로우</h1>
  <p class="sub">스크린샷 기반 사용자 플로우 (screenshot-all 실행 후 생성)</p>
  ${MAIN_URL ? `<p class="sub"><a href="${MAIN_URL}" target="_blank" rel="noopener" style="color:#38bdf8">메인 → ${MAIN_URL}</a></p>` : ''}

  <div class="tabs">
    <button class="active" data-tab="studio">Studio (사용자)</button>
    <button data-tab="admin">Admin (관리자)</button>
  </div>

  <div id="studio" class="section tab-content">
    <h2>Studio — 사용자 플로우 (localhost:3000)</h2>
    <div class="flow">
      ${studioFlowHtml}
    </div>
    ${studioOthers.length ? `<h3 style="margin-top:24px;font-size:0.9rem;color:#94a3b8">기타</h3><div class="grid">${studioOthersHtml}</div>` : ''}
  </div>

  <div id="admin" class="section tab-content" style="display:none">
    <h2>Admin — 관리자 플로우 (localhost:3001)</h2>
    <div class="flow">
      ${adminFlowHtml}
    </div>
    ${adminOthers.length ? `<h3 style="margin-top:24px;font-size:0.9rem;color:#94a3b8">운영 메뉴</h3><div class="grid">${adminOthersHtml}</div>` : ''}
  </div>

  <script>
    document.querySelectorAll('.tabs button').forEach(btn => {
      btn.addEventListener('click', () => {
        document.querySelectorAll('.tabs button').forEach(b => b.classList.remove('active'));
        document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
        btn.classList.add('active');
        document.getElementById(btn.dataset.tab).style.display = 'block';
      });
    });
  </script>
</body>
</html>
`;

  writeFileSync(OUTPUT, html);
  console.log(`\n✅ 플로우차트 생성: ${OUTPUT}`);
  console.log(`   Studio: ${studioFiles.length}개, Admin: ${adminFiles.length}개\n`);
}

main();

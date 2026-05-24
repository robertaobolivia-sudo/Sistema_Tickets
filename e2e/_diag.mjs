import { chromium } from '@playwright/test';
import fs from 'fs';
const massa = JSON.parse(fs.readFileSync('.massa.json','utf8'));
const browser = await chromium.launch();
const page = await browser.newPage();
await page.goto('http://127.0.0.1:8080/');
await page.getByTestId('login-email').fill(massa.email);
await page.getByTestId('login-password').fill(massa.senha);
await page.getByTestId('login-submit').click();
await page.waitForSelector('#appScreen.screen-active');
await page.locator('[data-page="tickets"]').first().click();
await page.waitForTimeout(500);
const diag = await page.evaluate(() => {
  const el = document.getElementById('page-tickets');
  const header = el?.querySelector('.page-header');
  const cs = el ? getComputedStyle(el) : null;
  const hs = header ? getComputedStyle(header) : null;
  return {
    className: el?.className,
    display: cs?.display,
    opacity: cs?.opacity,
    height: el?.offsetHeight,
    headerDisplay: hs?.display,
    headerOpacity: hs?.opacity,
    headerHeight: header?.offsetHeight,
    headerVisible: header ? !!(header.offsetWidth && header.offsetHeight) : false,
    activePages: [...document.querySelectorAll('.main-content > .page.active')].map(p => p.id),
  };
});
console.log(JSON.stringify(diag, null, 2));
await browser.close();

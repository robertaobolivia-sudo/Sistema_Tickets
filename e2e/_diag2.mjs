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
const diag = await page.evaluate(() => {
  const tickets = document.getElementById('page-tickets');
  const main = document.querySelector('.main-content');
  const chain = [];
  let n = tickets;
  while (n && n !== document.body) {
    chain.push({ tag: n.tagName, id: n.id, class: n.className });
    n = n.parentElement;
  }
  const direct = tickets?.parentElement === main;
  const rules = tickets ? [...document.styleSheets].flatMap((ss, si) => {
    try {
      return [...ss.cssRules].filter(r => r.selectorText && r.style?.display && (r.selectorText.includes('page') || r.selectorText.includes('tickets'))).map(r => ({ sheet: ss.href?.split('/').pop(), sel: r.selectorText, display: r.style.display, important: r.style.getPropertyPriority('display') }));
    } catch { return []; }
  }).filter(x => x.sel && (x.sel.includes('.page') || x.sel.includes('tickets'))).slice(0, 30) : [];
  return { direct, chain: chain.slice(0, 8), display: tickets ? getComputedStyle(tickets).display : null, mainChildPages: main ? [...main.children].map(c => ({ tag: c.tagName, id: c.id, cls: c.className })) : [] };
});
console.log(JSON.stringify(diag, null, 2));
await browser.close();

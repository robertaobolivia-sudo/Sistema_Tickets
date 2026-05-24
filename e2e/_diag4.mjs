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
  const main = document.querySelector('.main-content');
  const kids = main ? [...main.querySelectorAll(':scope > section.page')] : [];
  const last = kids[kids.length-1]?.id;
  const abrir = document.getElementById('page-abrir-ticket');
  const dash = document.getElementById('page-dashboard');
  return {
    pageSectionsInMain: kids.map(k => k.id),
    dashParent: dash?.parentElement?.id || dash?.parentElement?.className,
    abrirParent: abrir?.parentElement?.id || abrir?.parentElement?.tagName,
    ticketsParent: document.getElementById('page-tickets')?.parentElement?.tagName,
    bodyKids: [...document.body.children].map(c => ({ tag: c.tagName, id: c.id })).slice(0, 15),
  };
});
console.log(JSON.stringify(diag, null, 2));
await browser.close();

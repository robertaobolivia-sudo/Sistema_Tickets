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
  const chain = [];
  let n = tickets;
  for (let i = 0; i < 12 && n; i++) {
    chain.push({ tag: n.tagName, id: n.id || '', cls: (n.className || '').toString().slice(0,60) });
    n = n.parentElement;
  }
  return { chain };
});
console.log(JSON.stringify(diag, null, 2));
await browser.close();

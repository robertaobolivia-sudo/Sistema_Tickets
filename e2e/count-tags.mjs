import fs from 'fs';

const html = fs.readFileSync(
    '../src/main/resources/static/index.html',
    'utf8'
);
const start = html.indexOf('<main class="main-content">');
const end = html.indexOf('</main>', start);
const chunk = html.slice(start, end + 7);
const line0 = html.slice(0, start).split('\n').length;

const stack = [];
const re = /<\/?(div|section|main|aside|form)\b[^>]*>/gi;
let m;
let bad = 0;
while ((m = re.exec(chunk))) {
    const tag = m[0];
    const name = m[1].toLowerCase();
    const line = line0 + chunk.slice(0, m.index).split('\n').length - 1;
    const closing = tag.startsWith('</');
    const idMatch = tag.match(/\bid=["']([^"']+)["']/i);
    const id = idMatch ? idMatch[1] : '';
    if (closing) {
        const top = stack[stack.length - 1];
        if (!top || top.name !== name) {
            console.log('line', line, 'BAD close', name, id || '', 'top was', top?.name, top?.id);
            bad++;
        } else {
            stack.pop();
        }
    } else if (!tag.endsWith('/>')) {
        stack.push({ name, id, line });
    }
}
console.log('bad', bad, 'leftover', stack);

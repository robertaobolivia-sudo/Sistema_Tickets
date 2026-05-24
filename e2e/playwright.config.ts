import { defineConfig, devices } from '@playwright/test';
import path from 'path';
import { fileURLToPath } from 'url';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(__dirname, '..');

export default defineConfig({
    testDir: './tests',
    timeout: 120_000,
    expect: { timeout: 15_000 },
    fullyParallel: false,
    workers: 1,
    retries: process.env.CI ? 1 : 0,
    reporter: [['list'], ['html', { open: 'never' }]],
    globalSetup: path.join(__dirname, 'global-setup.ts'),
    use: {
        baseURL: process.env.E2E_BASE_URL || 'http://localhost:8080',
        trace: 'on-first-retry',
        screenshot: 'only-on-failure',
        video: 'retain-on-failure'
    },
    projects: [
        {
            name: 'chromium',
            use: { ...devices['Desktop Chrome'] }
        }
    ],
    webServer:
        process.env.E2E_SKIP_WEB_SERVER === '1' || process.env.E2E_SKIP_WEB_SERVER === 'true'
            ? undefined
            : {
              command:
                  process.platform === 'win32'
                      ? `java -jar "${path.join(projectRoot, 'target', 'suporte-tickets-1.0.0.jar')}"`
                      : `java -jar ${path.join(projectRoot, 'target', 'suporte-tickets-1.0.0.jar')}`,
              cwd: projectRoot,
              url: 'http://localhost:8080/',
              reuseExistingServer: !process.env.CI,
              timeout: process.env.CI ? 300_000 : 180_000,
              env: {
                  SPRING_DATASOURCE_URL:
                      process.env.SPRING_DATASOURCE_URL ||
                      'jdbc:mysql://localhost:3306/suporte_tickets',
                  SPRING_DATASOURCE_USERNAME:
                      process.env.SPRING_DATASOURCE_USERNAME || 'root',
                  SPRING_DATASOURCE_PASSWORD:
                      process.env.SPRING_DATASOURCE_PASSWORD || '123456',
                  SPRING_JPA_HIBERNATE_DDL_AUTO:
                      process.env.SPRING_JPA_HIBERNATE_DDL_AUTO || 'update'
              }
          }
});

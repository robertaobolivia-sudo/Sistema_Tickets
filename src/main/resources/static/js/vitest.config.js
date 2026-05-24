import { defineConfig } from 'vitest/config';
import { fileURLToPath } from 'url';
import path from 'path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
    resolve: {
        alias: {
            '@shared':     path.resolve(__dirname, 'shared'),
            '@features':   path.resolve(__dirname, 'features'),
            '@components': path.resolve(__dirname, 'components')
        }
    },
    test: {
        environment: 'node',
        include: [
            'shared/**/__tests__/*.test.js',
            'features/**/__tests__/*.test.js',
            'components/**/__tests__/*.test.js'
        ]
    }
});

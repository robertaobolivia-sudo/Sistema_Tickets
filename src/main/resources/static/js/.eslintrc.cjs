'use strict';

// npm install -D eslint eslint-plugin-boundaries
// Then: npx eslint features/ shared/ components/ app.js

const boundaries = require('eslint-plugin-boundaries');

module.exports = {
    plugins: { boundaries },
    settings: {
        'boundaries/elements': [
            { type: 'shared',     pattern: ['shared/**'] },
            { type: 'features',   pattern: ['features/**'] },
            { type: 'components', pattern: ['components/**'] }
        ],
        'boundaries/ignore': ['**/__tests__/**']
    },
    rules: {
        'boundaries/element-types': ['error', {
            default: 'disallow',
            rules: [
                { from: 'shared',     allow: ['shared'] },
                { from: 'components', allow: ['shared', 'components'] },
                { from: 'features',   allow: ['shared', 'components', 'features'] }
            ]
        }],
        'no-restricted-imports': ['error', {
            patterns: ['../../../*']
        }]
    }
};

// npm install -D plop
// Usage: npx plop feature
//        npx plop component

export default function (plop) {
    // Helpers
    plop.setHelper('kebabCase', str =>
        str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase()
    );

    // ── Feature generator ────────────────────────────────────────────────────
    plop.setGenerator('feature', {
        description: 'Create a new feature with page, service and test',
        prompts: [
            {
                type: 'input',
                name: 'name',
                message: 'Feature name (kebab-case):',
                validate: v => /^[a-z][a-z0-9-]+$/.test(v) || 'Use kebab-case (e.g. minha-feature)'
            }
        ],
        actions: [
            {
                type: 'add',
                path: 'features/{{name}}/{{name}}-page.js',
                templateFile: 'plop-templates/feature-page.hbs'
            },
            {
                type: 'add',
                path: 'features/{{name}}/{{name}}-service.js',
                templateFile: 'plop-templates/feature-service.hbs'
            },
            {
                type: 'add',
                path: 'features/{{name}}/__tests__/{{name}}-page.test.js',
                templateFile: 'plop-templates/feature-test.hbs'
            }
        ]
    });

    // ── Component generator ──────────────────────────────────────────────────
    plop.setGenerator('component', {
        description: 'Create a new shared component',
        prompts: [
            {
                type: 'input',
                name: 'name',
                message: 'Component name (kebab-case):',
                validate: v => /^[a-z][a-z0-9-]+$/.test(v) || 'Use kebab-case (e.g. meu-modal)'
            }
        ],
        actions: [
            {
                type: 'add',
                path: 'components/{{name}}/{{name}}.js',
                templateFile: 'plop-templates/component.hbs'
            }
        ]
    });
}

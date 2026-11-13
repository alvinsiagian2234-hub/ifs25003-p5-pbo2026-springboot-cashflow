import { UserConfigFn } from 'vite';
import { overrideVaadinConfig } from './vite.generated.ts';

const customConfig: UserConfigFn = (env) => ({
    build: {
        sourcemap: true,
    },
});

export default overrideVaadinConfig(customConfig);
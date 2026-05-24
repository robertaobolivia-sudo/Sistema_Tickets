package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Sprint 260: a cada subida, corrige massa DEV se duplicados/conexão/E2E voltarem (ex.: seed legado).
 */
@Slf4j
@Component
@Order(255)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.dev.clientes-massa-guard", havingValue = "true")
public class Sprint260DevClientesMassaGuardConfig implements ApplicationRunner {

    private final DevClientesMassaSanitizer devClientesMassaSanitizer;

    @Override
    public void run(ApplicationArguments args) {
        devClientesMassaSanitizer.executar("Sprint 260 guard");
    }
}

package com.suporte.tickets.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Remove contratantes legados duplicados na base DEV (Sprint 256).
 * Ativar: {@code app.sprint256.dedup-clientes-dev=true}
 */
@Slf4j
@Component
@Order(256)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sprint256.dedup-clientes-dev", havingValue = "true")
public class Sprint256DevClientesDedupConfig implements ApplicationRunner {

    private final DevClientesMassaSanitizer devClientesMassaSanitizer;

    @Override
    public void run(ApplicationArguments args) {
        devClientesMassaSanitizer.executar("Sprint 256 dedup");
    }
}

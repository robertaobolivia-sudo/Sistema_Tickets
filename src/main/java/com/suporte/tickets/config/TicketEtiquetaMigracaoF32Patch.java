package com.suporte.tickets.config;

import com.suporte.tickets.service.ContatoEtiquetaLegadoBackfillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Sprint F32: backfill contato_etiquetas a partir de tabela legada (removida na F34). */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class TicketEtiquetaMigracaoF32Patch implements ApplicationRunner {

    private final ContatoEtiquetaLegadoBackfillService backfillService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            ContatoEtiquetaLegadoBackfillService.Resultado r = backfillService.executar();
            log.info(
                    "Sprint F32: backfill contato_etiquetas — candidatos={}, inseridos={}, ja_existiam={}, ignorados={}",
                    r.candidatos(),
                    r.inseridos(),
                    r.jaExistiam(),
                    r.ignorados());
        } catch (Exception ex) {
            log.warn("Sprint F32: backfill contato_etiquetas ignorado: {}", ex.getMessage());
        }
    }
}

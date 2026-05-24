package com.suporte.tickets.config;

import com.suporte.tickets.domain.EtiquetaOperacionalCatalog;
import com.suporte.tickets.entity.Etiqueta;
import com.suporte.tickets.repository.EtiquetaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Garante cadastro das etiquetas operacionais sugeridas (idempotente).
 */
@Component
@Order(271)
@RequiredArgsConstructor
@Slf4j
public class Sprint271EtiquetasOperacionaisSeedConfig implements ApplicationRunner {

    private final EtiquetaRepository etiquetaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (EtiquetaOperacionalCatalog.DefinicaoOperacional def : EtiquetaOperacionalCatalog.DEFINICOES) {
            etiquetaRepository
                    .findByNomeIgnoreCase(def.nome())
                    .ifPresentOrElse(
                            existente -> reativarSeNecessario(existente, def),
                            () -> criar(def));
        }
    }

    private void reativarSeNecessario(Etiqueta etiqueta, EtiquetaOperacionalCatalog.DefinicaoOperacional def) {
        boolean alterado = false;
        if (!Boolean.TRUE.equals(etiqueta.getAtivo())) {
            etiqueta.setAtivo(true);
            alterado = true;
        }
        if (etiqueta.getDescricao() == null || etiqueta.getDescricao().isBlank()) {
            etiqueta.setDescricao(def.descricao());
            alterado = true;
        }
        if (etiqueta.getCor() == null || etiqueta.getCor().isBlank()) {
            etiqueta.setCor(def.cor());
            alterado = true;
        }
        if (alterado) {
            etiquetaRepository.save(etiqueta);
            log.info("Sprint 271: etiqueta operacional '{}' atualizada", def.nome());
        }
    }

    private void criar(EtiquetaOperacionalCatalog.DefinicaoOperacional def) {
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setNome(def.nome());
        etiqueta.setDescricao(def.descricao());
        etiqueta.setCor(def.cor());
        etiqueta.setAtivo(true);
        etiquetaRepository.save(etiqueta);
        log.info("Sprint 271: etiqueta operacional '{}' criada", def.nome());
    }
}

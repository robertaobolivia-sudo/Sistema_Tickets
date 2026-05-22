package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.service.AnalistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AnalistaSeedConfig {

    private final AnalistaRepository analistaRepository;

    @Bean
    CommandLineRunner seedAnalistaTeste() {
        return args -> analistaRepository.findByEmailIgnoreCase(AnalistaService.ANALISTA_PADRAO_EMAIL)
                .map(analista -> {
                    boolean alterado = false;
                    if (analista.getNivel() == null
                            || analista.getNivel().isBlank()
                            || "Nivel 1".equals(analista.getNivel())) {
                        analista.setNivel("Nível 1");
                        alterado = true;
                    }
                    if (!Boolean.TRUE.equals(analista.getOnline())) {
                        analista.setOnline(true);
                        alterado = true;
                    }
                    if (!Boolean.TRUE.equals(analista.getAtivo())) {
                        analista.setAtivo(true);
                        alterado = true;
                    }
                    return alterado ? analistaRepository.save(analista) : analista;
                })
                .orElseGet(() -> {
                    Analista analista = new Analista();
                    analista.setNome("Analista Teste");
                    analista.setEmail(AnalistaService.ANALISTA_PADRAO_EMAIL);
                    analista.setNivel("Nível 1");
                    analista.setFotoUrl(null);
                    analista.setOnline(true);
                    analista.setAtivo(true);
                    return analistaRepository.save(analista);
                });
    }
}

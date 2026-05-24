package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.repository.AnalistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;

/**
 * Admin mínimo para E2E no profile {@code ci} (sem seeds DEV pesados).
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ci", havingValue = "true")
public class CiMinimalAdminSeedConfig {

    private static final String ADMIN_EMAIL = "robertaobolivia@gmail.com";
    private static final String ADMIN_CPF = "426.020.518-88";
    private static final String NIVEL_PADRAO = "Nível 1";

    private final AnalistaRepository analistaRepository;

    @Value("${app.ci.admin-password:${SMOKE_ADMIN_SENHA:@Hipcom123789}}")
    private String adminPassword;

    @Bean
    @Order(5)
    CommandLineRunner seedCiMinimalAdmin() {
        return args -> {
            Analista analista = analistaRepository.findByCpf(ADMIN_CPF)
                    .or(() -> analistaRepository.findByEmailIgnoreCase(ADMIN_EMAIL))
                    .orElseGet(Analista::new);
            analista.setNome("João Falcone");
            analista.setNomeCompleto("João Falcone");
            analista.setCpf(ADMIN_CPF);
            analista.setCep("08032-326");
            analista.setRua("Algo Gianini");
            analista.setNumero("734");
            analista.setBairro("Vila Nova Curuçá");
            analista.setCidade("São Paulo");
            analista.setEstado("São Paulo");
            analista.setUf("SP");
            analista.setPais("Brasil");
            analista.setCelular("+55 11 9 70530162");
            analista.setDataNascimento(LocalDate.of(1993, 6, 15));
            analista.setEmail(ADMIN_EMAIL);
            if (analista.getSenha() == null || analista.getSenha().isBlank()) {
                analista.setSenha(adminPassword);
            }
            analista.setNivel(NIVEL_PADRAO);
            analista.setPerfilAcesso(PerfilAcesso.ADMIN);
            analista.setStatusOperador(StatusOperador.ONLINE);
            analista.setOnline(true);
            analista.setAtivo(true);
            analistaRepository.save(analista);
        };
    }
}

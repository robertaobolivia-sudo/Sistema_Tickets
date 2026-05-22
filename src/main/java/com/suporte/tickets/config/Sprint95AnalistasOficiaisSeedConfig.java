package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.service.AnalistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class Sprint95AnalistasOficiaisSeedConfig {

    private static final String NIVEL_PADRAO = "Nível 1";
    private static final String JOAO_EMAIL = "robertaobolivia@gmail.com";
    private static final String JOAO_CPF = "426.020.518-88";

    private final AnalistaRepository analistaRepository;
    private final TicketRepository ticketRepository;

    @Bean
    @Order(200)
    CommandLineRunner seedSprint95AnalistasOficiais() {
        return args -> {
            Analista joao = salvarJoaoFalcone();
            Analista wesley = salvarOuAtualizarAnalista(oficial(
                    "Wesley Silva",
                    "wesley.silva@suporte.local",
                    "111.222.333-44",
                    "Wesley@123",
                    LocalDate.of(1994, 4, 12),
                    "+55 11 91000-1001"
            ));
            Analista gustavo = salvarOuAtualizarAnalista(oficial(
                    "Gustavo Silva",
                    "gustavo.silva@suporte.local",
                    "222.333.444-55",
                    "Gustavo@123",
                    LocalDate.of(1992, 8, 21),
                    "+55 11 91000-1002"
            ));
            Analista michelle = salvarOuAtualizarAnalista(oficial(
                    "Michelle Falcone",
                    "michelle.falcone@suporte.local",
                    "333.444.555-66",
                    "Michelle@123",
                    LocalDate.of(1995, 11, 5),
                    "+55 11 91000-1003"
            ));

        };
    }

    private Analista salvarJoaoFalcone() {
        Analista analista = analistaRepository.findByCpf(JOAO_CPF)
                .or(() -> analistaRepository.findByEmailIgnoreCase(JOAO_EMAIL))
                .orElseGet(Analista::new);
        analista.setNome("João Falcone");
        analista.setNomeCompleto("João Falcone");
        analista.setCpf(JOAO_CPF);
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
        analista.setEmail(JOAO_EMAIL);
        if (analista.getSenha() == null || analista.getSenha().isBlank()) {
            analista.setSenha("@Hipcom123789");
        }
        analista.setNivel(NIVEL_PADRAO);
        analista.setPerfilAcesso(PerfilAcesso.ADMIN);
        analista.setStatusOperador(StatusOperador.ONLINE);
        analista.setOnline(true);
        analista.setAtivo(true);
        return analistaRepository.save(analista);
    }

    private AnalistaSeed oficial(String nome, String email, String cpf, String senha, LocalDate nascimento, String celular) {
        return new AnalistaSeed(
                nome,
                email,
                cpf,
                "01111-000",
                "Rua Oficial do Suporte",
                "100",
                "Centro",
                "São Paulo",
                "São Paulo",
                "SP",
                "Brasil",
                celular,
                nascimento,
                senha,
                StatusOperador.ONLINE
        );
    }

    private Analista salvarOuAtualizarAnalista(AnalistaSeed seed) {
        Analista analista = analistaRepository.findByEmailIgnoreCase(seed.email())
                .or(() -> analistaRepository.findByCpf(seed.cpf()))
                .orElseGet(Analista::new);
        preencherAnalista(analista, seed, true);
        return analistaRepository.save(analista);
    }

    private void completarAnalistasDeTeste() {
        List<AnalistaSeed> analistasTeste = List.of(
                teste("Ana Bugatti", "ana.bugatti@suporte.local", "101.101.101-01", "Ana@123", LocalDate.of(1994, 1, 10)),
                teste("Bruno Cache", "bruno.cache@suporte.local", "202.202.202-02", "Bruno@123", LocalDate.of(1992, 2, 20)),
                teste("Carla Query", "carla.query@suporte.local", "303.303.303-03", "Carla@123", LocalDate.of(1991, 3, 15)),
                teste("Diego Deploy", "diego.deploy@suporte.local", "404.404.404-04", "Diego@123", LocalDate.of(1990, 4, 12)),
                teste("Elisa Firewall", "elisa.firewall@suporte.local", "505.505.505-05", "Elisa@123", LocalDate.of(1995, 5, 25)),
                teste("Fabio Script", "fabio.script@suporte.local", "606.606.606-06", "Fabio@123", LocalDate.of(1993, 6, 6)),
                teste("Giovana Token", "giovana.token@suporte.local", "707.707.707-07", "Giovana@123", LocalDate.of(1996, 7, 17)),
                teste("Heitor Backup", "heitor.backup@suporte.local", "808.808.808-08", "Heitor@123", LocalDate.of(1989, 8, 18)),
                teste("Ingrid Pixel", "ingrid.pixel@suporte.local", "909.909.909-09", "Ingrid@123", LocalDate.of(1997, 9, 9)),
                teste("Analista Teste", AnalistaService.ANALISTA_PADRAO_EMAIL, "000.000.000-00", "Teste@123", LocalDate.of(1990, 1, 1))
        );

        for (AnalistaSeed seed : analistasTeste) {
            Optional<Analista> analistaExistente = analistaRepository.findByEmailIgnoreCase(seed.email());
            if (analistaExistente.isEmpty()) {
                continue;
            }
            Analista analista = analistaExistente.get();
            preencherAnalista(analista, seed, false);
            analistaRepository.save(analista);
        }
    }

    private AnalistaSeed teste(String nome, String email, String cpf, String senha, LocalDate nascimento) {
        return new AnalistaSeed(
                nome,
                email,
                cpf,
                "09999-000",
                "Rua Fictícia do Kanban",
                "99",
                "Bairro Teste",
                "São Paulo",
                "São Paulo",
                "SP",
                "Brasil",
                "+55 11 90000-9999",
                nascimento,
                senha,
                StatusOperador.ONLINE
        );
    }

    private void preencherAnalista(Analista analista, AnalistaSeed seed, boolean sobrescrever) {
        if (sobrescrever || vazio(analista.getNome())) analista.setNome(seed.nomeCompleto());
        if (sobrescrever || vazio(analista.getNomeCompleto())) analista.setNomeCompleto(seed.nomeCompleto());
        if (sobrescrever || vazio(analista.getCpf())) analista.setCpf(seed.cpf());
        if (sobrescrever || vazio(analista.getCep())) analista.setCep(seed.cep());
        if (sobrescrever || vazio(analista.getRua())) analista.setRua(seed.rua());
        if (sobrescrever || vazio(analista.getNumero())) analista.setNumero(seed.numero());
        if (sobrescrever || vazio(analista.getBairro())) analista.setBairro(seed.bairro());
        if (sobrescrever || vazio(analista.getCidade())) analista.setCidade(seed.cidade());
        if (sobrescrever || vazio(analista.getEstado())) analista.setEstado(seed.estado());
        if (sobrescrever || vazio(analista.getUf())) analista.setUf(seed.uf());
        if (sobrescrever || vazio(analista.getPais())) analista.setPais(seed.pais());
        if (sobrescrever || vazio(analista.getCelular())) analista.setCelular(seed.celular());
        if (sobrescrever || analista.getDataNascimento() == null) analista.setDataNascimento(seed.dataNascimento());
        if (sobrescrever || vazio(analista.getEmail())) analista.setEmail(seed.email());
        if (sobrescrever || vazio(analista.getSenha())) analista.setSenha(seed.senha());
        if (vazio(analista.getNivel())) analista.setNivel(NIVEL_PADRAO);
        if (analista.getStatusOperador() == null) analista.setStatusOperador(seed.statusOperador());
        if (analista.getOnline() == null) analista.setOnline(seed.statusOperador() != StatusOperador.OFFLINE);
        if (analista.getAtivo() == null) analista.setAtivo(true);
    }

    private boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private void remanejarTicketsEntreOficiais(List<Analista> oficiais) {
        List<Ticket> tickets = new ArrayList<>(ticketRepository.findAllByOrderByDataAberturaDesc());
        Collections.shuffle(tickets, new Random(95));
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            ticket.setAnalistaResponsavel(oficiais.get(i % oficiais.size()));
            ticketRepository.save(ticket);
        }
    }

    private record AnalistaSeed(
            String nomeCompleto,
            String email,
            String cpf,
            String cep,
            String rua,
            String numero,
            String bairro,
            String cidade,
            String estado,
            String uf,
            String pais,
            String celular,
            LocalDate dataNascimento,
            String senha,
            StatusOperador statusOperador
    ) {
    }
}

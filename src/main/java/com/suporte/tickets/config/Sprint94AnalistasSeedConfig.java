package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Carteira;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.StatusOperador;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.CarteiraRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.service.AnalistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class Sprint94AnalistasSeedConfig {

    private static final String NIVEL_PADRAO = "Nível 1";
    private static final String DUTY_BREAKER_EMAIL_ANTIGO = "robertaobolivia@gmail.com.br";
    private static final String DUTY_BREAKER_EMAIL = "robertaobolivia@gmail.com";
    private static final String[] CONEXOES = {
            "FastComércio", "Fênix", "Rocha Mendes", "Status Automação"
    };
    private static final String[] CANAIS = {
            "WhatsApp", "Telefone", "Web", "E-mail"
    };

    private final AnalistaRepository analistaRepository;
    private final ClienteRepository clienteRepository;
    private final CarteiraRepository carteiraRepository;
    private final TicketRepository ticketRepository;

    @Bean
    @Order(100)
    CommandLineRunner seedSprint94AnalistasUsuarios() {
        return args -> {
            salvarOuAtualizarAnalista(dutyBreaker());

            List<Cliente> clientesPrincipais = criarClientesConexoes();
            distribuirTicketsPorConexaoECliente(clientesPrincipais);
            garantirPendenciasAbertas(clientesPrincipais);
        };
    }

    private AnalistaSeed dutyBreaker() {
        return new AnalistaSeed(
                "Duty Breaker",
                DUTY_BREAKER_EMAIL,
                "426.020.518-88",
                "08032-326",
                "Algo Gianini",
                "734",
                "Vila Nova Curuçá",
                "São Paulo",
                "São Paulo",
                "SP",
                "Brasil",
                "+55 11 9 70530162",
                LocalDate.of(1993, 6, 15),
                "@Hipcom123789",
                StatusOperador.ONLINE
        );
    }

    private List<AnalistaSeed> analistasFicticios() {
        return List.of(
                new AnalistaSeed("Ana Bugatti", "ana.bugatti@suporte.local", "101.101.101-01", "01001-001", "Rua dos Bugs", "101", "Centro", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0101", LocalDate.of(1994, 1, 10), "Ana@123", StatusOperador.ONLINE),
                new AnalistaSeed("Bruno Cache", "bruno.cache@suporte.local", "202.202.202-02", "02002-002", "Rua Memória Curta", "202", "Cacheville", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0202", LocalDate.of(1992, 2, 20), "Bruno@123", StatusOperador.ONLINE),
                new AnalistaSeed("Carla Query", "carla.query@suporte.local", "303.303.303-03", "03003-003", "Avenida Select", "303", "Banco Norte", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0303", LocalDate.of(1991, 3, 15), "Carla@123", StatusOperador.ONLINE),
                new AnalistaSeed("Diego Deploy", "diego.deploy@suporte.local", "404.404.404-04", "04004-004", "Travessa Build", "404", "Pipeline", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0404", LocalDate.of(1990, 4, 12), "Diego@123", StatusOperador.ONLINE),
                new AnalistaSeed("Elisa Firewall", "elisa.firewall@suporte.local", "505.505.505-05", "05005-005", "Rua Porta Aberta", "505", "Rede Sul", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0505", LocalDate.of(1995, 5, 25), "Elisa@123", StatusOperador.ONLINE),
                new AnalistaSeed("Fabio Script", "fabio.script@suporte.local", "606.606.606-06", "06006-006", "Rua Automação", "606", "Terminal", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0606", LocalDate.of(1993, 6, 6), "Fabio@123", StatusOperador.ONLINE),
                new AnalistaSeed("Giovana Token", "giovana.token@suporte.local", "707.707.707-07", "07007-007", "Rua OTP", "707", "Login", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0707", LocalDate.of(1996, 7, 17), "Giovana@123", StatusOperador.ONLINE),
                new AnalistaSeed("Heitor Backup", "heitor.backup@suporte.local", "808.808.808-08", "08008-008", "Rua Snapshot", "808", "Storage", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0808", LocalDate.of(1989, 8, 18), "Heitor@123", StatusOperador.ONLINE),
                new AnalistaSeed("Ingrid Pixel", "ingrid.pixel@suporte.local", "909.909.909-09", "09009-009", "Rua Layout", "909", "Design", "São Paulo", "São Paulo", "SP", "Brasil", "+55 11 90000-0909", LocalDate.of(1997, 9, 9), "Ingrid@123", StatusOperador.ONLINE)
        );
    }

    private Analista salvarOuAtualizarAnalista(AnalistaSeed seed) {
        Analista analista = buscarAnalistaExistente(seed)
                .orElseGet(Analista::new);
        analista.setNome(seed.nomeCompleto());
        analista.setNomeCompleto(seed.nomeCompleto());
        analista.setCpf(seed.cpf());
        analista.setCep(seed.cep());
        analista.setRua(seed.rua());
        analista.setNumero(seed.numero());
        analista.setBairro(seed.bairro());
        analista.setCidade(seed.cidade());
        analista.setEstado(seed.estado());
        analista.setUf(seed.uf());
        analista.setPais(seed.pais());
        analista.setCelular(seed.celular());
        analista.setDataNascimento(seed.dataNascimento());
        analista.setEmail(seed.email());
        analista.setSenha(seed.senha());
        analista.setNivel(NIVEL_PADRAO);
        analista.setFotoUrl(null);
        analista.setStatusOperador(seed.statusOperador());
        analista.setOnline(seed.statusOperador() != StatusOperador.OFFLINE);
        analista.setAtivo(true);
        return analistaRepository.save(analista);
    }

    private java.util.Optional<Analista> buscarAnalistaExistente(AnalistaSeed seed) {
        java.util.Optional<Analista> analistaAtual = analistaRepository.findByEmailIgnoreCase(seed.email());
        if (analistaAtual.isPresent()) {
            return analistaAtual;
        }
        if (DUTY_BREAKER_EMAIL.equalsIgnoreCase(seed.email())) {
            java.util.Optional<Analista> analistaEmailAntigo = analistaRepository.findByEmailIgnoreCase(DUTY_BREAKER_EMAIL_ANTIGO);
            if (analistaEmailAntigo.isPresent()) {
                return analistaEmailAntigo;
            }
            return analistaRepository.findByCpf(seed.cpf());
        }
        return java.util.Optional.empty();
    }

    private void transferirTicketsAnalistaTeste(Analista dutyBreaker) {
        analistaRepository.findByEmailIgnoreCase(AnalistaService.ANALISTA_PADRAO_EMAIL).ifPresent(analistaTeste -> {
            List<Ticket> tickets = ticketRepository.findByAnalistaResponsavelIdOrderByDataAberturaAsc(analistaTeste.getId());
            for (Ticket ticket : tickets) {
                ticket.setAnalistaResponsavel(dutyBreaker);
                ticketRepository.save(ticket);
            }
            analistaTeste.setStatusOperador(StatusOperador.OFFLINE);
            analistaTeste.setOnline(false);
            analistaRepository.save(analistaTeste);
        });
    }

    private List<Cliente> criarClientesConexoes() {
        return List.of(
                criarClienteConexao("FastComércio", "11910000001", "fastcomercio@clientes.local"),
                criarClienteConexao("Fênix", "11910000002", "fenix@clientes.local"),
                criarClienteConexao("Rocha Mendes", "11910000003", "rocha.mendes@clientes.local"),
                criarClienteConexao("Status Automação", "11910000004", "status.automacao@clientes.local")
        );
    }

    private Cliente criarClienteConexao(String nome, String telefone, String email) {
        Carteira carteira = carteiraRepository.findByNome(nome)
                .orElseGet(() -> {
                    Carteira novaCarteira = new Carteira();
                    novaCarteira.setNome(nome);
                    return carteiraRepository.save(novaCarteira);
                });

        Cliente cliente = clienteRepository.findByNome(nome)
                .orElseGet(() -> {
                    Cliente novoCliente = new Cliente();
                    novoCliente.setNome(nome);
                    return novoCliente;
                });
        cliente.setTelefone(telefone);
        cliente.setTelefoneContato(telefone);
        cliente.setEmail(email);
        cliente.setEmpresa(nome);
        cliente.setCidade("São Paulo");
        cliente.setUf("SP");
        cliente.setCarteira(carteira);
        return clienteRepository.save(cliente);
    }

    private void distribuirTicketsPorConexaoECliente(List<Cliente> clientesPrincipais) {
        List<Ticket> tickets = ticketRepository.findAllByOrderByDataAberturaDesc();
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = tickets.get(i);
            Cliente cliente = clientesPrincipais.get(i % clientesPrincipais.size());
            ticket.setCliente(cliente);
            ticket.setConexao(cliente.getNome());
            ticketRepository.save(ticket);
        }
    }

    private void garantirPendenciasAbertas(List<Cliente> clientesPrincipais) {
        for (int i = 0; i < clientesPrincipais.size(); i++) {
            Cliente cliente = clientesPrincipais.get(i);
            if (existeTicketAbertoNaConexao(cliente.getNome())) {
                continue;
            }

            Ticket ticket = new Ticket();
            ticket.setNumeroTicket(gerarNumeroTicket());
            ticket.setCliente(cliente);
            ticket.setConexao(cliente.getNome());
            ticket.setCanal(CANAIS[i % CANAIS.length]);
            ticket.setMensagemInicial("Pendência operacional " + cliente.getNome());
            ticket.setStatus(TicketStatus.ABERTO);
            ticket.setDataAbertura(LocalDateTime.now().minusMinutes((i + 1) * 11L));
            ticketRepository.save(ticket);
        }
    }

    private boolean existeTicketAbertoNaConexao(String conexao) {
        return ticketRepository.findByStatusInOrderByConexaoAscDataAberturaAsc(
                        List.of(TicketStatus.ABERTO, TicketStatus.AGUARDANDO_CLIENTE)
                )
                .stream()
                .anyMatch(ticket -> conexao.equals(ticket.getConexao()));
    }

    private String gerarNumeroTicket() {
        Integer proximoNumero = ticketRepository.getNextSequence();
        return String.format("TK-%06d", proximoNumero);
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

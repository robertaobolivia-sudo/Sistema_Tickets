package com.suporte.tickets.config;

import com.suporte.tickets.dto.ClienteRequestDTO;
import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.entity.WhatsappMatriz;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.ContatoEtiquetaRepository;
import com.suporte.tickets.repository.ContatoRepository;
import com.suporte.tickets.repository.InteracaoPendenteDecisaoRepository;
import com.suporte.tickets.repository.NotificacaoInternaRepository;
import com.suporte.tickets.repository.TicketAnexoRepository;
import com.suporte.tickets.repository.TicketInteracaoRepository;
import com.suporte.tickets.repository.TicketRepository;
import com.suporte.tickets.repository.TicketSatisfacaoRepository;
import com.suporte.tickets.repository.WhatsappMatrizRepository;
import com.suporte.tickets.service.ClienteService;
import com.suporte.tickets.service.TicketAtivoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reset operacional da base local + massa mínima Sprint 253.
 * Ativar apenas em dev: {@code app.sprint253.dev-reset=true}.
 */
@Slf4j
@Component
@Order(250)
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sprint253.dev-reset", havingValue = "true")
public class Sprint253DevMassaSeedConfig implements ApplicationRunner {

    private static final String ADMIN_EMAIL = AnalistasOficiaisConstants.JOAO_EMAIL;

    private final JdbcTemplate jdbcTemplate;
    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final ContatoRepository contatoRepository;
    private final WhatsappMatrizRepository whatsappMatrizRepository;
    private final TicketRepository ticketRepository;
    private final AnalistaRepository analistaRepository;
    private final TicketSatisfacaoRepository ticketSatisfacaoRepository;
    private final InteracaoPendenteDecisaoRepository interacaoPendenteDecisaoRepository;
    private final TicketAnexoRepository ticketAnexoRepository;
    private final TicketInteracaoRepository ticketInteracaoRepository;
    private final NotificacaoInternaRepository notificacaoInternaRepository;
    private final ContatoEtiquetaRepository contatoEtiquetaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.warn("Sprint 253: reset operacional DEV ativo (app.sprint253.dev-reset=true)");
        limparDadosOperacionais();
        Analista admin = analistaRepository.findByEmailIgnoreCase(ADMIN_EMAIL)
                .orElseThrow(() -> new IllegalStateException(
                        "Analista admin nao encontrado: " + ADMIN_EMAIL));
        List<ClienteSeed> seeds = List.of(
                new ClienteSeed(
                        "Rocha Mendes",
                        "Rocha Mendes Comercio LTDA",
                        "11.222.333/0001-41",
                        "123456789",
                        "Carlos Rocha",
                        "5511981110001",
                        "contato@rochamendes.com.br",
                        "Av. Paulista, 1000",
                        "01310100",
                        "Sao Paulo",
                        "SP",
                        "https://rochamendes.example",
                        "Seg-Sex 08:00-18:00"
                ),
                new ClienteSeed(
                        "Status Automação",
                        "Status Automacao Industria ME",
                        "22.333.444/0001-52",
                        "987654321",
                        "Ana Status",
                        "5511982220002",
                        "suporte@statusautomacao.com.br",
                        "Rua das Industrias, 200",
                        "13050000",
                        "Campinas",
                        "SP",
                        "https://statusautomacao.example",
                        "Seg-Sex 09:00-17:00"
                ),
                new ClienteSeed(
                        "FastComércio",
                        "Fast Comercio Varejo SA",
                        "33.444.555/0001-63",
                        "112233445",
                        "Bruno Fast",
                        "5511983330003",
                        "atendimento@fastcomercio.com.br",
                        "Shopping Center, Loja 45",
                        "30130000",
                        "Belo Horizonte",
                        "MG",
                        "https://fastcomercio.example",
                        "Seg-Sab 10:00-22:00"
                ),
                new ClienteSeed(
                        "Fênix",
                        "Fenix Servicos Digitais LTDA",
                        "44.555.666/0001-74",
                        "554433221",
                        "Diana Fenix",
                        "5511984440004",
                        "ola@fenixdigital.com.br",
                        "Alameda Fenix, 88",
                        "80010000",
                        "Curitiba",
                        "PR",
                        "https://fenixdigital.example",
                        "24h chat / Seg-Sex comercial 08-18"
                )
        );
        int idx = 0;
        for (ClienteSeed seed : seeds) {
            idx++;
            Cliente cliente = cadastrarCliente(seed);
            WhatsappMatriz matriz = garantirWhatsappMatriz(cliente, seed.whatsappMatriz());
            criarMassaTickets(cliente, matriz, admin, idx);
        }
        log.warn("Sprint 253: massa DEV concluida — {} clientes, {} tickets",
                clienteRepository.count(), ticketRepository.count());
    }

    private void limparDadosOperacionais() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        try {
            interacaoPendenteDecisaoRepository.deleteAllInBatch();
            ticketSatisfacaoRepository.deleteAllInBatch();
            ticketAnexoRepository.deleteAllInBatch();
            ticketInteracaoRepository.deleteAllInBatch();
            notificacaoInternaRepository.deleteAllInBatch();
            ticketRepository.deleteAllInBatch();
            contatoEtiquetaRepository.deleteAllInBatch();
            contatoRepository.deleteAllInBatch();
            whatsappMatrizRepository.deleteAllInBatch();
            clienteRepository.deleteAllInBatch();
            jdbcTemplate.update("DELETE FROM auditoria_eventos WHERE entidade IN ('TICKET','CLIENTE','CONTATO')");
        } finally {
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    private Cliente cadastrarCliente(ClienteSeed seed) {
        ClienteRequestDTO dto = new ClienteRequestDTO();
        dto.setNome(seed.responsavel());
        dto.setResponsavel(seed.responsavel());
        dto.setRazaoSocial(seed.razaoSocial());
        dto.setEmpresa(seed.razaoSocial());
        dto.setCnpj(seed.cnpj());
        dto.setInscricaoEstadual(seed.inscricaoEstadual());
        dto.setWhatsapp(seed.whatsappMatriz());
        dto.setTelefone(seed.whatsappMatriz());
        dto.setTelefoneContato(seed.whatsappMatriz());
        dto.setEmail(seed.email());
        dto.setEndereco(seed.endereco());
        dto.setCep(seed.cep());
        dto.setCidade(seed.cidade());
        dto.setUf(seed.uf());
        dto.setSite(seed.site());
        dto.setHorarioFuncionamento(seed.horario());
        dto.setStatus("ATIVO");
        dto.setClassificacaoCliente("SEM_CLASSIFICACAO");
        dto.setObservacoes("Massa controlada Sprint 253 — " + seed.nomeExibicao());
        return clienteRepository.findByCnpj(seed.cnpj())
                .or(() -> clienteRepository.findByNome(seed.responsavel()))
                .orElseGet(() -> {
                    var created = clienteService.criar(dto);
                    return clienteRepository.findById(created.getId())
                            .orElseThrow(() -> new IllegalStateException("Cliente nao persistido apos criar"));
                });
    }

    private WhatsappMatriz garantirWhatsappMatriz(Cliente cliente, String numero) {
        String norm = TicketAtivoService.normalizarTelefone(numero);
        return whatsappMatrizRepository.findByNumeroNormalizado(norm)
                .orElseGet(() -> {
                    WhatsappMatriz m = new WhatsappMatriz();
                    m.setCliente(cliente);
                    m.setNome("Matriz " + cliente.getRazaoSocial());
                    m.setNumero(numero);
                    m.setNumeroNormalizado(norm);
                    m.setAtivo(true);
                    return whatsappMatrizRepository.save(m);
                });
    }

    private void criarMassaTickets(Cliente cliente, WhatsappMatriz matriz, Analista admin, int indiceCliente) {
        LocalDateTime agora = LocalDateTime.now();
        List<TicketSpec> specs = List.of(
                new TicketSpec("ABERTO", TicketStatus.ABERTO, null, 1),
                new TicketSpec("EM_ATENDIMENTO", TicketStatus.EM_ATENDIMENTO, admin, 2),
                new TicketSpec("RESOLVIDO", TicketStatus.RESOLVIDO, admin, 3)
        );
        int contatoOffset = 0;
        for (TicketSpec spec : specs) {
            contatoOffset++;
            Contato contato = criarContato(cliente, indiceCliente, contatoOffset);
            String assunto = "S253-" + cliente.getRazaoSocial() + " — " + spec.rotulo();
            if (ticketRepository.existsByMensagemInicial(assunto)) {
                continue;
            }
            Ticket ticket = new Ticket();
            ticket.setNumeroTicket(gerarNumeroTicket());
            ticket.setCliente(cliente);
            ticket.setContato(contato);
            ticket.setWhatsappMatriz(matriz);
            ticket.setCanal("WhatsApp");
            ticket.setMensagemInicial(assunto);
            ticket.setStatus(spec.status());
            ticket.setDataAbertura(agora.minusHours(spec.offsetHoras()));
            if (spec.analista() != null) {
                ticket.setAnalistaResponsavel(spec.analista());
                ticket.setDataPrimeiroAtendimento(ticket.getDataAbertura().plusMinutes(15));
            }
            if (spec.status() == TicketStatus.RESOLVIDO) {
                ticket.setDataEncerramento(agora.minusHours(1));
            }
            ticketRepository.save(ticket);
        }
    }

    private Contato criarContato(Cliente cliente, int indiceCliente, int indiceContato) {
        String whatsapp = String.format("551198%03d%04d", indiceCliente, indiceContato * 111);
        String norm = TicketAtivoService.normalizarTelefone(whatsapp);
        return contatoRepository.findByCliente_IdAndWhatsappNormalizado(cliente.getId(), norm)
                .orElseGet(() -> {
                    Contato c = new Contato();
                    c.setCliente(cliente);
                    c.setNome("Contato " + indiceContato + " — " + cliente.getRazaoSocial());
                    c.setWhatsapp(whatsapp);
                    c.setWhatsappNormalizado(norm);
                    c.setEmail("contato" + indiceContato + "." + cliente.getId() + "@s253.local");
                    return contatoRepository.save(c);
                });
    }

    private String gerarNumeroTicket() {
        Integer proximoNumero = ticketRepository.getNextSequence();
        return String.format("TK-%06d", proximoNumero);
    }

    private record ClienteSeed(
            String nomeExibicao,
            String razaoSocial,
            String cnpj,
            String inscricaoEstadual,
            String responsavel,
            String whatsappMatriz,
            String email,
            String endereco,
            String cep,
            String cidade,
            String uf,
            String site,
            String horario
    ) {
    }

    private record TicketSpec(String rotulo, TicketStatus status, Analista analista, int offsetHoras) {
    }
}

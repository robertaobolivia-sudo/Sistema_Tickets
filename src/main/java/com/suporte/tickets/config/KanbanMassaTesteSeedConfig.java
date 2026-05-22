package com.suporte.tickets.config;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.Cliente;
import com.suporte.tickets.entity.Ticket;
import com.suporte.tickets.entity.TicketStatus;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.ClienteRepository;
import com.suporte.tickets.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class KanbanMassaTesteSeedConfig {

    private static final String NIVEL_PADRAO = "Nível 1";
    private static final String[] CANAIS = {
            "WhatsApp", "Telefone", "Web", "E-mail", "Balcão Misterioso"
    };

    private final AnalistaRepository analistaRepository;
    private final ClienteRepository clienteRepository;
    private final TicketRepository ticketRepository;

    /**
     * Desativado na Sprint 9.5.1 — apenas os quatro atendentes oficiais permanecem ativos.
     */
    @Bean
    @Order(40)
    CommandLineRunner seedKanbanMassaTeste() {
        return args -> {
            // massa Kanban de analistas de teste descontinuada
        };
    }

    private void seedAnalistasKanban() {
        List<AnalistaSeed> analistas = List.of(
                new AnalistaSeed("Ana Bugatti", "ana.bugatti@suporte.local", new String[]{
                        "Mouse sumiu do caixa",
                        "Impressora fiscal fazendo drama",
                        "Cliente clicou em tudo",
                        "PDV abriu de mau humor",
                        "Cupom fugiu da tela"
                }),
                new AnalistaSeed("Bruno Cache", "bruno.cache@suporte.local", new String[]{
                        "Sistema lembrou do erro antigo",
                        "Tela travou pensando na vida",
                        "Botão salvar tirou folga",
                        "TEF pediu café",
                        "Relatório veio com amnésia"
                }),
                new AnalistaSeed("Carla Query", "carla.query@suporte.local", new String[]{
                        "SELECT desapareceu no estoque",
                        "Produto duplicou sozinho",
                        "Banco respondeu em enigmas",
                        "Cadastro virou novela",
                        "MySQL acordou lento"
                }),
                new AnalistaSeed("Diego Deploy", "diego.deploy@suporte.local", new String[]{
                        "Atualização chegou sem avisar",
                        "Versão nova assustou o caixa",
                        "Serviço reiniciou e fingiu costume",
                        "Build passou mas ninguém acreditou",
                        "Deploy pediu segunda chance"
                }),
                new AnalistaSeed("Elisa Firewall", "elisa.firewall@suporte.local", new String[]{
                        "Internet saiu para almoçar",
                        "VPN entrou em modo ninja",
                        "Porta bloqueada por timidez",
                        "Roteador esqueceu o caminho",
                        "Ping voltou com saudade"
                }),
                new AnalistaSeed("Fabio Script", "fabio.script@suporte.local", new String[]{
                        "Script rodou para o lado errado",
                        "Atalho apertou o botão errado",
                        "PowerShell ficou filosófico",
                        "Automação digitou com preguiça",
                        "Comando funcionou sem querer"
                }),
                new AnalistaSeed("Giovana Token", "giovana.token@suporte.local", new String[]{
                        "Token expirou no suspense",
                        "Autenticador ficou tímido",
                        "OTP chegou atrasado",
                        "Código mudou de ideia",
                        "Login pediu confirmação emocional"
                }),
                new AnalistaSeed("Heitor Backup", "heitor.backup@suporte.local", new String[]{
                        "Backup fez cosplay de sumido",
                        "Arquivo compactou demais",
                        "Restauração respirou fundo",
                        "HD pediu férias",
                        "Pasta foi morar em outro lugar"
                }),
                new AnalistaSeed("Ingrid Pixel", "ingrid.pixel@suporte.local", new String[]{
                        "Botão saiu do alinhamento",
                        "Card cresceu sem autorização",
                        "Modal ficou exibido",
                        "Ícone resolveu dançar",
                        "Layout pediu carinho"
                })
        );

        for (AnalistaSeed seed : analistas) {
            Analista analista = analistaRepository.findByEmailIgnoreCase(seed.email())
                    .orElseGet(() -> criarAnalista(seed.nome(), seed.email()));
            garantirAnalistaAtivoOnline(analista);
            criarTicketsSeNecessario(analista, seed.assuntos());
        }
    }

    private Analista criarAnalista(String nome, String email) {
        Analista analista = new Analista();
        analista.setNome(nome);
        analista.setEmail(email);
        analista.setNivel(NIVEL_PADRAO);
        analista.setFotoUrl(null);
        analista.setOnline(true);
        analista.setAtivo(true);
        return analistaRepository.save(analista);
    }

    private void garantirAnalistaAtivoOnline(Analista analista) {
        boolean alterado = false;
        if (!NIVEL_PADRAO.equals(analista.getNivel())) {
            analista.setNivel(NIVEL_PADRAO);
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
        if (alterado) {
            analistaRepository.save(analista);
        }
    }

    private void criarTicketsSeNecessario(Analista analista, String[] assuntos) {
        if (ticketRepository.countByAnalistaResponsavelId(analista.getId()) > 0) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        for (int i = 0; i < assuntos.length; i++) {
            String assunto = assuntos[i];
            if (ticketRepository.existsByMensagemInicial(assunto)) {
                continue;
            }

            Cliente cliente = buscarOuCriarClienteTeste(analista.getNome(), i + 1);
            Ticket ticket = new Ticket();
            ticket.setNumeroTicket(gerarNumeroTicket());
            ticket.setCliente(cliente);
            ticket.setAnalistaResponsavel(analista);
            ticket.setCanal(CANAIS[i % CANAIS.length]);
            ticket.setConexao("Remoto");
            ticket.setMensagemInicial(assunto);
            ticket.setStatus(TicketStatus.EM_ATENDIMENTO);
            ticket.setDataAbertura(agora.minusMinutes((assuntos.length - i) * 3L));
            ticket.setDataPrimeiroAtendimento(agora.minusMinutes((assuntos.length - i) * 2L));
            ticketRepository.save(ticket);
        }
    }

    private Cliente buscarOuCriarClienteTeste(String nomeAnalista, int indice) {
        String nomeCliente = "Cliente Teste " + nomeAnalista + " " + indice;
        return clienteRepository.findByNome(nomeCliente)
                .orElseGet(() -> {
                    Cliente cliente = new Cliente();
                    cliente.setNome(nomeCliente);
                    int sufixo = Math.abs((nomeCliente + indice).hashCode()) % 100_000_000;
                    String telefone = String.format("119%08d", sufixo);
                    cliente.setTelefone(telefone);
                    cliente.setTelefoneContato(telefone);
                    cliente.setEmail("cliente." + indice + "." + nomeAnalista.toLowerCase().replace(' ', '.') + "@teste.local");
                    return clienteRepository.save(cliente);
                });
    }

    private String gerarNumeroTicket() {
        Integer proximoNumero = ticketRepository.getNextSequence();
        return String.format("TK-%06d", proximoNumero);
    }

    private record AnalistaSeed(String nome, String email, String[] assuntos) {
    }
}

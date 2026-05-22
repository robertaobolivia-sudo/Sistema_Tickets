package com.suporte.tickets.service;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.AuditoriaEvento;
import com.suporte.tickets.repository.AnalistaRepository;
import com.suporte.tickets.repository.AuditoriaEventoRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    public static final String ACAO_LOGIN_SUCESSO = "LOGIN_SUCESSO";
    public static final String ACAO_LOGOUT = "LOGOUT";
    public static final String ACAO_TICKET_CRIAR_UI = "TICKET_CRIAR_UI";
    public static final String ACAO_TICKET_STATUS = "TICKET_STATUS";
    public static final String ACAO_TICKET_ENCERRAR = "TICKET_ENCERRAR";
    public static final String ACAO_TICKET_REABRIR = "TICKET_REABRIR";
    public static final String ACAO_TICKET_ESCALONAR = "TICKET_ESCALONAR";
    public static final String ACAO_TICKET_REMOVER_ESCALONAMENTO = "TICKET_REMOVER_ESCALONAMENTO";
    public static final String ACAO_TICKET_OBSERVACAO_ATENDIMENTO = "TICKET_OBSERVACAO_ATENDIMENTO";
    public static final String ACAO_TICKET_ETIQUETAS = "TICKET_ETIQUETAS";
    public static final String ACAO_CONTATO_ETIQUETAS = "CONTATO_ETIQUETAS";
    public static final String ACAO_INTERACAO_PENDENTE_VINCULAR = "INTERACAO_PENDENTE_VINCULAR";
    public static final String ACAO_INTERACAO_PENDENTE_NOVO_TICKET = "INTERACAO_PENDENTE_NOVO_TICKET";
    public static final String ACAO_TICKET_ANEXO = "TICKET_ANEXO";
    public static final String ACAO_ANALISTA_PERFIL_ACESSO = "ANALISTA_PERFIL_ACESSO";
    public static final String ACAO_ANALISTA_CRIAR = "ANALISTA_CRIAR";
    public static final String ACAO_ANALISTA_EDITAR = "ANALISTA_EDITAR";
    public static final String ACAO_CONFIG_HORARIO_UTIL = "CONFIG_HORARIO_UTIL";
    public static final String ACAO_CONFIG_FERIADO = "CONFIG_FERIADO";
    public static final String ACAO_CONFIG_SLA_META = "CONFIG_SLA_META";
    public static final String ACAO_AVALIACAO_ENVIO_WHATSAPP = "AVALIACAO_ENVIO_WHATSAPP";

    public static final String ENTIDADE_TICKET = "TICKET";
    public static final String ENTIDADE_CONTATO = "CONTATO";
    public static final String ENTIDADE_ANALISTA = "ANALISTA";
    public static final String ENTIDADE_CONFIG = "CONFIG";

    private final AuditoriaEventoRepository auditoriaEventoRepository;
    private final AnalistaRepository analistaRepository;

    /**
     * Registra evento de auditoria. Nunca propaga exceção para não afetar o fluxo principal.
     */
    public void registrar(String acao, String entidade, String entidadeId, String descricao, Analista analista) {
        try {
            AuditoriaEvento evento = new AuditoriaEvento();
            evento.setAcao(truncar(acao, 80));
            evento.setEntidade(truncar(entidade, 50));
            evento.setEntidadeId(truncar(entidadeId, 100));
            evento.setDescricao(truncar(sanitizarDescricao(descricao), 500));
            if (analista != null) {
                evento.setAnalistaId(analista.getId());
                evento.setAnalistaNome(truncar(analista.getNome(), 150));
                evento.setPerfilAcesso(AnalistaService.resolverPerfilAcesso(analista).name());
            }
            preencherRequestContext(evento);
            auditoriaEventoRepository.save(evento);
        } catch (Exception e) {
            log.warn("Falha ao registrar auditoria [{}]: {}", acao, e.getMessage());
        }
    }

    public void registrar(String acao, String entidade, String entidadeId, String descricao, Long analistaId) {
        Analista analista = null;
        if (analistaId != null) {
            analista = analistaRepository.findById(analistaId).orElse(null);
        }
        registrar(acao, entidade, entidadeId, descricao, analista);
    }

    private void preencherRequestContext(AuditoriaEvento evento) {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes servletAttrs)) {
            return;
        }
        HttpServletRequest request = servletAttrs.getRequest();
        if (request == null) {
            return;
        }
        evento.setIpOrigem(truncar(request.getRemoteAddr(), 64));
        evento.setUserAgent(truncar(request.getHeader("User-Agent"), 255));
    }

    private static String sanitizarDescricao(String descricao) {
        if (descricao == null) {
            return "";
        }
        String s = descricao;
        if (s.length() > 500) {
            s = s.substring(0, 500);
        }
        return s.replaceAll("(?i)(senha|password|authToken|token)\\s*[=:][^\\s,;]+", "$1=***");
    }

    private static String truncar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        if (valor.length() <= max) {
            return valor;
        }
        return valor.substring(0, max);
    }
}

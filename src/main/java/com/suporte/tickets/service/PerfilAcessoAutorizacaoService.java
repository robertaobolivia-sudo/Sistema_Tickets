package com.suporte.tickets.service;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.PerfilAcesso;
import com.suporte.tickets.exception.AcessoNegadoException;
import com.suporte.tickets.repository.AnalistaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PerfilAcessoAutorizacaoService {

    public static final String HEADER_ANALISTA_ID = "X-Analista-Id";
    public static final String HEADER_ANALISTA_TOKEN = "X-Analista-Token";

    private final AnalistaRepository analistaRepository;

    public Analista exigirSessaoValida(Long analistaId, String authToken) {
        return validarSessao(analistaId, authToken);
    }

    public void exigirAdmin(Long analistaId, String authToken) {
        Analista analista = validarSessao(analistaId, authToken);
        if (AnalistaService.resolverPerfilAcesso(analista) != PerfilAcesso.ADMIN) {
            throw new AcessoNegadoException("Acesso negado. Permissao de administrador necessaria.");
        }
    }

    /** Indicadores gerenciais: ADMIN e SUPERVISOR. */
    public void exigirAdminOuSupervisor(Long analistaId, String authToken) {
        Analista analista = validarSessao(analistaId, authToken);
        PerfilAcesso perfil = AnalistaService.resolverPerfilAcesso(analista);
        if (perfil != PerfilAcesso.ADMIN && perfil != PerfilAcesso.SUPERVISOR) {
            throw new AcessoNegadoException("Acesso negado. Permissao de supervisor ou administrador necessaria.");
        }
    }

    /**
     * Permite leitura do cadastro do proprio analista autenticado ou de qualquer analista por ADMIN.
     */
    public void exigirProprioAnalistaOuAdmin(Long analistaId, String authToken, Long recursoAnalistaId) {
        Analista analista = validarSessao(analistaId, authToken);
        if (recursoAnalistaId != null && recursoAnalistaId.equals(analista.getId())) {
            return;
        }
        if (AnalistaService.resolverPerfilAcesso(analista) != PerfilAcesso.ADMIN) {
            throw new AcessoNegadoException("Acesso negado. Voce so pode consultar seus proprios dados.");
        }
    }

    public PerfilAcesso carregarPerfil(Long analistaId) {
        if (analistaId == null) {
            throw new AcessoNegadoException(
                    "Identificacao do analista obrigatoria (header " + HEADER_ANALISTA_ID + ").");
        }
        Analista analista = analistaRepository.findById(analistaId)
                .orElseThrow(() -> new AcessoNegadoException("Analista nao encontrado para autorizacao."));
        return AnalistaService.resolverPerfilAcesso(analista);
    }

    public Analista validarSessao(Long analistaId, String authToken) {
        if (analistaId == null) {
            throw new AcessoNegadoException(
                    "Identificacao do analista obrigatoria (header " + HEADER_ANALISTA_ID + ").");
        }
        if (authToken == null || authToken.isBlank()) {
            throw new AcessoNegadoException(
                    "Token de sessao obrigatorio (header " + HEADER_ANALISTA_TOKEN + "). Faca login novamente.");
        }
        Analista analista = analistaRepository.findById(analistaId)
                .orElseThrow(() -> new AcessoNegadoException("Analista nao encontrado para autorizacao."));
        String armazenado = analista.getAuthToken();
        if (armazenado == null || armazenado.isBlank()
                || !tokensIguais(armazenado, authToken.trim())) {
            throw new AcessoNegadoException("Sessao invalida ou expirada. Faca login novamente.");
        }
        LocalDateTime expiraEm = analista.getAuthTokenExpiraEm();
        if (expiraEm != null && LocalDateTime.now().isAfter(expiraEm)) {
            throw new AcessoNegadoException("Sessao expirada. Faca login novamente.");
        }
        return analista;
    }

    static boolean tokensIguais(String esperado, String informado) {
        if (esperado == null || informado == null) {
            return false;
        }
        byte[] a = esperado.getBytes(StandardCharsets.UTF_8);
        byte[] b = informado.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(a, b);
    }
}

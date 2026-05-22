package com.suporte.tickets.config;

import com.suporte.tickets.entity.GrupoCategoria;
import com.suporte.tickets.entity.SubgrupoCategoria;
import com.suporte.tickets.entity.Motivo;
import com.suporte.tickets.repository.GrupoCategoriaRepository;
import com.suporte.tickets.repository.MotivoRepository;
import com.suporte.tickets.repository.SubgrupoCategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CategoriaSeedConfig {

    private final GrupoCategoriaRepository grupoCategoriaRepository;
    private final SubgrupoCategoriaRepository subgrupoCategoriaRepository;
    private final MotivoRepository motivoRepository;

    @Bean
    CommandLineRunner seedCategorias() {
        return args -> {
            Map<String, List<String>> categorias = new LinkedHashMap<>();
            categorias.put("Fiscal", List.of("NF-e", "NFC-e", "SAT", "Impostos"));
            categorias.put("TEF", List.of("TLS", "PinPad", "Adquirente"));
            categorias.put("Banco de Dados", List.of("MySQL", "Backup", "Performance"));
            categorias.put("Sistema", List.of("Cadastro", "Relatorios", "Integracoes"));

            categorias.forEach((nomeGrupo, subgrupos) -> {
                GrupoCategoria grupo = grupoCategoriaRepository.findByNomeIgnoreCase(nomeGrupo)
                        .orElseGet(() -> {
                            GrupoCategoria novoGrupo = new GrupoCategoria();
                            novoGrupo.setNome(nomeGrupo);
                            novoGrupo.setAtivo(true);
                            return grupoCategoriaRepository.save(novoGrupo);
                        });

                subgrupos.forEach(nomeSubgrupo -> {
                    boolean existe = subgrupoCategoriaRepository
                            .existsByGrupoCategoriaIdAndNomeIgnoreCase(grupo.getId(), nomeSubgrupo);
                    if (!existe) {
                        SubgrupoCategoria subgrupo = new SubgrupoCategoria();
                        subgrupo.setGrupoCategoria(grupo);
                        subgrupo.setNome(nomeSubgrupo);
                        subgrupo.setAtivo(true);
                        SubgrupoCategoria salvo = subgrupoCategoriaRepository.save(subgrupo);
                        seedMotivoOutros(salvo);
                    }
                });
            });

            garantirMotivoAtivoEmSubcategoriasAtivas();
        };
    }

    /**
     * Sprint 216: subcategorias criadas fora do seed fixo (ex.: Configurações) precisam de ao menos um Motivo ativo.
     */
    void garantirMotivoAtivoEmSubcategoriasAtivas() {
        subgrupoCategoriaRepository.findByAtivoTrueOrderByNomeAsc().forEach(sub -> {
            boolean temMotivoAtivo = !motivoRepository
                    .findBySubgrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(sub.getId())
                    .isEmpty();
            if (!temMotivoAtivo) {
                seedMotivoOutros(sub);
            }
        });
    }

    private void seedMotivoOutros(SubgrupoCategoria subgrupo) {
        String nome = "Outros";
        if (!motivoRepository.existsBySubgrupoCategoriaIdAndNomeIgnoreCase(subgrupo.getId(), nome)) {
            Motivo motivo = new Motivo();
            motivo.setSubgrupoCategoria(subgrupo);
            motivo.setNome(nome);
            motivo.setAtivo(true);
            motivoRepository.save(motivo);
        }
    }
}

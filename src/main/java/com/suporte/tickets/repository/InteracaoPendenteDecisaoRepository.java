package com.suporte.tickets.repository;

import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteracaoPendenteDecisaoRepository extends JpaRepository<InteracaoPendenteDecisao, Long> {

    List<InteracaoPendenteDecisao> findByStatusOrderByCriadaEmDesc(InteracaoPendenteDecisaoStatus status);

    Optional<InteracaoPendenteDecisao> findByIdAndStatus(Long id, InteracaoPendenteDecisaoStatus status);

    boolean existsByContato_IdAndStatus(Integer contatoId, InteracaoPendenteDecisaoStatus status);

    @Query("""
            SELECT p FROM InteracaoPendenteDecisao p
            JOIN FETCH p.cliente c
            JOIN FETCH p.contato
            JOIN FETCH p.ticketAnterior
            WHERE p.status = :status
              AND c.ativo = true
            ORDER BY c.nome ASC, p.criadaEm ASC
            """)
    List<InteracaoPendenteDecisao> findPendentesPorClientesAtivos(
            @Param("status") InteracaoPendenteDecisaoStatus status);
}

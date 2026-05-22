package com.suporte.tickets.repository;

import com.suporte.tickets.entity.InteracaoPendenteDecisao;
import com.suporte.tickets.entity.InteracaoPendenteDecisaoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteracaoPendenteDecisaoRepository extends JpaRepository<InteracaoPendenteDecisao, Long> {

    List<InteracaoPendenteDecisao> findByStatusOrderByCriadaEmDesc(InteracaoPendenteDecisaoStatus status);

    Optional<InteracaoPendenteDecisao> findByIdAndStatus(Long id, InteracaoPendenteDecisaoStatus status);

    boolean existsByContato_IdAndStatus(Integer contatoId, InteracaoPendenteDecisaoStatus status);
}

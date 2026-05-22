package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Motivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MotivoRepository extends JpaRepository<Motivo, Long> {

    List<Motivo> findByAtivoTrueOrderByNomeAsc();

    List<Motivo> findBySubgrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(Long subgrupoId);

    boolean existsBySubgrupoCategoriaIdAndNomeIgnoreCase(Long subgrupoId, String nome);

    Optional<Motivo> findBySubgrupoCategoriaIdAndNomeIgnoreCase(Long subgrupoId, String nome);
}

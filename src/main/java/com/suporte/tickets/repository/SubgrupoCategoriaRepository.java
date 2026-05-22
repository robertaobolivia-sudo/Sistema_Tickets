package com.suporte.tickets.repository;

import com.suporte.tickets.entity.SubgrupoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubgrupoCategoriaRepository extends JpaRepository<SubgrupoCategoria, Long> {

    List<SubgrupoCategoria> findByAtivoTrueOrderByNomeAsc();

    List<SubgrupoCategoria> findByGrupoCategoriaIdAndAtivoTrueOrderByNomeAsc(Long grupoId);

    boolean existsByGrupoCategoriaIdAndNomeIgnoreCase(Long grupoId, String nome);
}

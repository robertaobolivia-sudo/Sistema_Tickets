package com.suporte.tickets.repository;

import com.suporte.tickets.entity.GrupoCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoCategoriaRepository extends JpaRepository<GrupoCategoria, Long> {

    List<GrupoCategoria> findByAtivoTrueOrderByNomeAsc();

    Optional<GrupoCategoria> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}

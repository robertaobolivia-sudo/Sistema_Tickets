package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Etiqueta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EtiquetaRepository extends JpaRepository<Etiqueta, Long> {

    List<Etiqueta> findAllByOrderByNomeAsc();

    List<Etiqueta> findByAtivoTrueOrderByNomeAsc();

    Optional<Etiqueta> findByNomeIgnoreCase(String nome);
}

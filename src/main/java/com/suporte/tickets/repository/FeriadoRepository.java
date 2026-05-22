package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {

    List<Feriado> findAllByOrderByDataAsc();

    List<Feriado> findByAtivoTrueOrderByDataAsc();

    Optional<Feriado> findFirstByDataAndAtivoTrue(LocalDate data);

    boolean existsByDataAndAtivoTrue(LocalDate data);

    boolean existsByDataAndAtivoTrueAndIdNot(LocalDate data, Long id);

    Optional<Feriado> findFirstByData(LocalDate data);
}

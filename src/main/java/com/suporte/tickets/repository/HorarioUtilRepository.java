package com.suporte.tickets.repository;

import com.suporte.tickets.entity.HorarioUtil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HorarioUtilRepository extends JpaRepository<HorarioUtil, Long> {

    Optional<HorarioUtil> findFirstByAtivoTrueOrderByIdAsc();

    Optional<HorarioUtil> findFirstByOrderByIdAsc();
}

package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Analista;
import com.suporte.tickets.entity.StatusOperador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalistaRepository extends JpaRepository<Analista, Long> {

    Optional<Analista> findByEmailIgnoreCase(String email);

    Optional<Analista> findByCpf(String cpf);

    List<Analista> findByOnlineTrueAndAtivoTrueOrderByNomeAsc();

    List<Analista> findByAtivoTrueOrderByNomeAsc();

    List<Analista> findAllByOrderByNomeAsc();

    long countByAtivoTrue();

    long countByAtivoTrueAndStatusOperador(StatusOperador statusOperador);
}

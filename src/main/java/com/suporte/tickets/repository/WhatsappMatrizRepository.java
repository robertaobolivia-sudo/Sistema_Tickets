package com.suporte.tickets.repository;

import com.suporte.tickets.entity.WhatsappMatriz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WhatsappMatrizRepository extends JpaRepository<WhatsappMatriz, Integer> {

    List<WhatsappMatriz> findByCliente_IdOrderByNomeAscNumeroAsc(Integer clienteId);

    Optional<WhatsappMatriz> findByNumeroNormalizado(String numeroNormalizado);

    boolean existsByNumeroNormalizado(String numeroNormalizado);

    boolean existsByNumeroNormalizadoAndIdNot(String numeroNormalizado, Integer id);
}

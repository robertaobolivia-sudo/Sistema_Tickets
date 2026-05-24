package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContatoRepository extends JpaRepository<Contato, Integer> {

    List<Contato> findByCliente_IdOrderByNomeAsc(Integer clienteId);

    Optional<Contato> findByCliente_IdAndWhatsappNormalizado(Integer clienteId, String whatsappNormalizado);

    boolean existsByCliente_IdAndWhatsappNormalizado(Integer clienteId, String whatsappNormalizado);

    List<Contato> findAllByOrderByNomeAsc();
}

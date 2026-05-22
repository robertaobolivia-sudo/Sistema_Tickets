package com.suporte.tickets.repository;

import com.suporte.tickets.entity.ContatoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContatoClienteRepository extends JpaRepository<ContatoCliente, Integer> {

    List<ContatoCliente> findByCliente_IdOrderByNomeAsc(Integer clienteId);

    List<ContatoCliente> findByCliente_IdAndAtivoTrueOrderByNomeAsc(Integer clienteId);

    Optional<ContatoCliente> findByCliente_IdAndPrincipalTrueAndAtivoTrue(Integer clienteId);

    @Query("""
            SELECT cc FROM ContatoCliente cc
            WHERE cc.telefone = :telefone OR cc.celular = :telefone
            """)
    List<ContatoCliente> findByTelefoneOuCelular(@Param("telefone") String telefone);

    @Query("""
            SELECT c FROM ContatoCliente c
            JOIN c.cliente cliente
            WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.telefone, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.celular, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY c.nome ASC
            """)
    List<ContatoCliente> pesquisar(@Param("termo") String termo);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ContatoCliente c SET c.principal = false
            WHERE c.cliente.id = :clienteId
              AND (:excludeId IS NULL OR c.id <> :excludeId)
            """)
    void removerPrincipalDosDemais(@Param("clienteId") Integer clienteId, @Param("excludeId") Integer excludeId);
}

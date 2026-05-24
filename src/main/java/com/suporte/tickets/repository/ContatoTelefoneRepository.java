package com.suporte.tickets.repository;

import com.suporte.tickets.entity.ContatoTelefone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ContatoTelefoneRepository extends JpaRepository<ContatoTelefone, Long> {

    @Query("""
            SELECT ct FROM ContatoTelefone ct
            JOIN FETCH ct.contato c
            WHERE ct.cliente.id = :clienteId AND ct.telefoneNormalizado = :telefoneNormalizado
            """)
    Optional<ContatoTelefone> findByCliente_IdAndTelefoneNormalizado(
            @Param("clienteId") Integer clienteId,
            @Param("telefoneNormalizado") String telefoneNormalizado);

    boolean existsByCliente_IdAndTelefoneNormalizado(Integer clienteId, String telefoneNormalizado);

    boolean existsByContato_IdAndTelefoneNormalizado(Integer contatoId, String telefoneNormalizado);

    List<ContatoTelefone> findByContato_IdOrderByTelefoneNormalizadoAsc(Integer contatoId);
}

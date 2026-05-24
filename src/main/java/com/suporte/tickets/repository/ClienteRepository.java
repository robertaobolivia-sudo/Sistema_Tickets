package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de banco de dados da entidade Cliente
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    /**
     * Busca um cliente pelo telefone
     * @param telefone Telefone do cliente
     * @return Optional com o cliente, ou vazio se não encontrado
     */
    Optional<Cliente> findByTelefone(String telefone);

    Optional<Cliente> findByTelefoneContato(String telefoneContato);

    /**
     * Busca um cliente pelo nome
     * @param nome Nome do cliente
     * @return Optional com o cliente, ou vazio se não encontrado
     */
    Optional<Cliente> findByNome(String nome);

    Optional<Cliente> findByCnpj(String cnpj);

    List<Cliente> findAllByOrderByDataCadastroDesc();

    List<Cliente> findByAtivoTrueOrderByDataCadastroDesc();

    long countByAtivoTrue();

    @Query("""
            SELECT c FROM Cliente c
            WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.telefone, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.telefoneContato, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.empresa, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.cnpj, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.email, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.cidade, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
               OR LOWER(COALESCE(c.uf, '')) LIKE LOWER(CONCAT('%', :termo, '%'))
            ORDER BY c.dataCadastro DESC
            """)
    List<Cliente> pesquisar(@Param("termo") String termo);

}

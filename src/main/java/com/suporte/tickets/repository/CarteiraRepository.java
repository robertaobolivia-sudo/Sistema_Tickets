package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Carteira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositório para operações de banco de dados da entidade Carteira
 */
@Repository
public interface CarteiraRepository extends JpaRepository<Carteira, Integer> {

    /**
     * Busca uma carteira pelo nome
     * @param nome Nome da carteira
     * @return Optional com a carteira, ou vazio se não encontrada
     */
    Optional<Carteira> findByNome(String nome);

    Optional<Carteira> findByNomeIgnoreCase(String nome);

    List<Carteira> findAllByOrderByNomeAsc();

}

package com.suporte.tickets.repository;

import com.suporte.tickets.entity.Contato;
import com.suporte.tickets.entity.ContatoEtiqueta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContatoEtiquetaRepository extends JpaRepository<ContatoEtiqueta, Long> {

    List<ContatoEtiqueta> findByContatoOrderByEtiqueta_NomeAsc(Contato contato);

    boolean existsByContato_IdAndEtiqueta_Id(Integer contatoId, Long etiquetaId);

    long countByContato_Id(Integer contatoId);

    void deleteByContato(Contato contato);
}

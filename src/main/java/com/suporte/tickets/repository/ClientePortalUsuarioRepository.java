package com.suporte.tickets.repository;

import com.suporte.tickets.entity.ClientePortalUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientePortalUsuarioRepository extends JpaRepository<ClientePortalUsuario, Long> {

    Optional<ClientePortalUsuario> findByEmailIgnoreCase(String email);

    List<ClientePortalUsuario> findByCliente_IdOrderByNomeAsc(Integer clienteId);

    List<ClientePortalUsuario> findAllByOrderByNomeAsc();
}

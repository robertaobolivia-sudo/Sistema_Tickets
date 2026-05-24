package com.suporte.tickets.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardAnalistaOnlineCardDTO {

    private Long id;
    private String nome;
    /** Perfil/cargo para exibição (ex.: Supervisor, Analista). */
    private String cargo;
    private String fotoUrl;
    /** ONLINE | OCUPADO | AUSENTE | OFFLINE — status de exibição no Dashboard. */
    private String statusExibicao;
    private int ticketsEmAtendimento;
}

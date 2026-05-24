package com.suporte.tickets.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EtiquetaOperacionalCatalogTest {

    @Test
    void reconheceNomesOperacionais() {
        assertTrue(EtiquetaOperacionalCatalog.isNomeOperacional("Indevido"));
        assertTrue(EtiquetaOperacionalCatalog.isNomeOperacional(" contato pessoal "));
        assertTrue(EtiquetaOperacionalCatalog.isNomeOperacional("PROPAGANDA"));
        assertFalse(EtiquetaOperacionalCatalog.isNomeOperacional("VIP"));
    }

    @Test
    void temAlgumaOperacionalNaLista() {
        assertTrue(EtiquetaOperacionalCatalog.temAlgumaOperacional(List.of("VIP", "Indevido")));
        assertFalse(EtiquetaOperacionalCatalog.temAlgumaOperacional(List.of("Comercial")));
    }
}

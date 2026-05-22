package com.suporte.tickets.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClassificacaoClienteTest {

    @Test
    void effective_nullRetornaSemClassificacao() {
        assertEquals(ClassificacaoCliente.SEM_CLASSIFICACAO, ClassificacaoCliente.effective(null));
    }

    @Test
    void parse_valoresValidos() {
        assertEquals(ClassificacaoCliente.N1, ClassificacaoCliente.parse("N1"));
        assertEquals(ClassificacaoCliente.N2, ClassificacaoCliente.parse("n2"));
        assertEquals(ClassificacaoCliente.SEM_CLASSIFICACAO, ClassificacaoCliente.parse("SEM_CLASSIFICACAO"));
    }

    @Test
    void parse_vazioOuInvalidoRetornaSemClassificacao() {
        assertEquals(ClassificacaoCliente.SEM_CLASSIFICACAO, ClassificacaoCliente.parse(null));
        assertEquals(ClassificacaoCliente.SEM_CLASSIFICACAO, ClassificacaoCliente.parse(""));
        assertEquals(ClassificacaoCliente.SEM_CLASSIFICACAO, ClassificacaoCliente.parse("REVENDA"));
    }
}

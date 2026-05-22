package com.suporte.tickets.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.suporte.tickets.dto.TicketInteracaoResponseDTO;
import com.suporte.tickets.dto.TicketResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketPdfService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final TicketService ticketService;
    private final TicketInteracaoService ticketInteracaoService;

    @Transactional(readOnly = true)
    public byte[] gerarPdf(String numeroTicket) {
        TicketResponseDTO ticket = ticketService.buscarPorNumero(numeroTicket);
        List<TicketInteracaoResponseDTO> interacoes = ticketInteracaoService.listarPorTicket(numeroTicket);
        return montarPdf(ticket, interacoes);
    }

    public String montarNomeArquivo(String numeroTicket) {
        return "ticket-" + textoOuVazio(numeroTicket) + ".pdf";
    }

    private byte[] montarPdf(TicketResponseDTO ticket, List<TicketInteracaoResponseDTO> interacoes) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            document.add(new Paragraph("Relatório do Ticket", titleFont));
            document.add(new Paragraph(" "));

            adicionarSecao(document, sectionFont, labelFont, valueFont, "Dados do Ticket", new String[][]{
                    {"Número do ticket", texto(ticket.getNumeroTicket())},
                    {"Status", texto(ticket.getStatus())},
                    {"Canal", texto(ticket.getCanal())},
                    {"Conexão", texto(ticket.getConexao())},
                    {"Analista responsável", texto(ticket.getAnalistaResponsavelNome())},
                    {"Mensagem inicial", texto(ticket.getMensagemInicial())},
                    {"Data de abertura", formatarData(ticket.getDataAbertura())},
                    {"Data de primeiro atendimento", formatarData(ticket.getDataPrimeiroAtendimento())},
                    {"Data de encerramento", formatarData(ticket.getDataEncerramento())}
            });

            adicionarSecao(document, sectionFont, labelFont, valueFont, "Dados do Cliente", new String[][]{
                    {"Nome", texto(ticket.getCliente())},
                    {"Contato solicitante", texto(ticket.getContatoSolicitanteNome())},
                    {"Telefone contato solicitante", texto(ticket.getContatoSolicitanteTelefone())},
                    {"E-mail contato solicitante", texto(ticket.getContatoSolicitanteEmail())},
                    {"Telefone", texto(ticket.getTelefone())},
                    {"Telefone de contato", texto(ticket.getTelefoneContato())},
                    {"E-mail", texto(ticket.getEmail())},
                    {"Empresa", texto(ticket.getEmpresa())},
                    {"CNPJ", texto(ticket.getCnpj())},
                    {"Cidade", texto(ticket.getCidade())},
                    {"UF", texto(ticket.getUf())}
            });

            adicionarSecao(document, sectionFont, labelFont, valueFont, "Dados de Encerramento", new String[][]{
                    {"Grupo", texto(ticket.getGrupoCategoriaNome())},
                    {"Subgrupo", texto(ticket.getSubgrupoCategoriaNome())},
                    {"Comentário de encerramento", texto(ticket.getComentarioEncerramento())}
            });

            document.add(new Paragraph("Histórico de Interações", sectionFont));
            document.add(new Paragraph(" "));

            if (interacoes == null || interacoes.isEmpty()) {
                document.add(new Paragraph("-", valueFont));
            } else {
                PdfPTable table = new PdfPTable(new float[]{2.2f, 1.4f, 1.4f, 4f});
                table.setWidthPercentage(100);
                adicionarCabecalho(table, labelFont, "Data/hora", "Tipo", "Visibilidade", "Mensagem");
                for (TicketInteracaoResponseDTO interacao : interacoes) {
                    adicionarLinha(table, valueFont,
                            formatarData(interacao.getCriadoEm()),
                            texto(interacao.getTipoInteracao()),
                            texto(interacao.getVisibilidade()),
                            texto(interacao.getMensagem()));
                }
                document.add(table);
            }

            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Falha ao gerar PDF do ticket", e);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao gerar PDF do ticket", e);
        }
    }

    private void adicionarSecao(
            Document document,
            Font sectionFont,
            Font labelFont,
            Font valueFont,
            String titulo,
            String[][] linhas
    ) throws DocumentException {
        document.add(new Paragraph(titulo, sectionFont));
        document.add(new Paragraph(" "));
        PdfPTable table = new PdfPTable(new float[]{2.5f, 5.5f});
        table.setWidthPercentage(100);
        for (String[] linha : linhas) {
            table.addCell(celula(linha[0], labelFont, true));
            table.addCell(celula(linha[1], valueFont, false));
        }
        document.add(table);
        document.add(new Paragraph(" "));
    }

    private void adicionarCabecalho(PdfPTable table, Font font, String... colunas) {
        for (String coluna : colunas) {
            table.addCell(celula(coluna, font, true));
        }
    }

    private void adicionarLinha(PdfPTable table, Font font, String... colunas) {
        for (String coluna : colunas) {
            table.addCell(celula(coluna, font, false));
        }
    }

    private PdfPCell celula(String conteudo, Font font, boolean header) {
        PdfPCell cell = new PdfPCell(new Phrase(texto(conteudo), font));
        cell.setPadding(4f);
        if (header) {
            cell.setGrayFill(0.9f);
        }
        return cell;
    }

    private String texto(Object valor) {
        if (valor == null) {
            return "-";
        }
        if (valor instanceof String str) {
            return str.isBlank() ? "-" : str.trim();
        }
        return String.valueOf(valor);
    }

    private String textoOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String formatarData(LocalDateTime data) {
        if (data == null) {
            return "-";
        }
        return DATE_TIME_FORMATTER.format(data);
    }
}

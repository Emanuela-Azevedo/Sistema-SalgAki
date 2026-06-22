package com.salgaki.config;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.salgaki.dto.CardapioDTO;


@Component
public class CardapioPdfGenerator {

    public byte[] gerarPdf(List<CardapioDTO> produtos) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLACK);
            Paragraph title = new Paragraph("Cardápio - Produtos Disponíveis", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph("\n"));

            if (produtos.isEmpty()) {
                document.add(new Paragraph("Nenhum produto disponível no momento."));
            } else {
                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);

                table.addCell(new PdfPCell(new Phrase("Produto")));
                table.addCell(new PdfPCell(new Phrase("Preço")));
                table.addCell(new PdfPCell(new Phrase("Quantidade")));

                for (CardapioDTO dto : produtos) {
                    table.addCell(dto.produtoNome());
                    table.addCell("R$ " + dto.preco());
                    table.addCell(dto.quantidade().toString());
                }

                document.add(table);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do cardápio", e);
        }
    }
}


package com.salgaki.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.salgaki.model.Produto;
import com.salgaki.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardapioService {

    private final ProdutoRepository produtoRepository;

    public byte[] gerarPdfCardapio() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);

            doc.add(new Paragraph("🍴 CARDÁPIO - SALGAKI 🍴")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18));

            String datahora = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            doc.add(new Paragraph("Atualizado em: " + datahora)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));

            doc.add(new Paragraph("\n"));

            List<Produto> produtosDisponiveis = produtoRepository.findAll()
                    .stream()
                    .filter(p -> p.getQuantidade() > 0)
                    .toList();

            if (produtosDisponiveis.isEmpty()) {
                doc.add(new Paragraph("Nenhum produto disponível no momento.")
                        .setTextAlignment(TextAlignment.CENTER));
            } else {
                Table table = new Table(4);
                table.addCell("Categoria");
                table.addCell("Produto");
                table.addCell("Preço");
                table.addCell("Disponível");

                for (Produto p : produtosDisponiveis) {
                    table.addCell(p.getCategoria().getNome());
                    table.addCell(p.getNome());
                    table.addCell("R$ " + String.format("%.2f", p.getPreco()));
                    table.addCell(p.getQuantidade() + " un");
                }

                doc.add(table);
            }

            doc.add(new Paragraph("\n"));
            doc.add(new Paragraph("Obrigado pela preferência!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Erro ao gerar PDF do cardápio", e);
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
    }
}

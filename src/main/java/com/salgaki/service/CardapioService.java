package com.salgaki.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.salgaki.dto.CardapioDTO;
import com.salgaki.model.Estoque;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;

    public byte[] gerarPdfCardapio() {
        List<Estoque> disponiveis = estoqueRepository.findByQuantidadeGreaterThan(0);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("Cardápio SalgAki"));
            document.add(new Paragraph(" "));

            if (disponiveis.isEmpty()) {
                document.add(new Paragraph("Nenhum produto disponível no momento."));
            } else {
                for (Estoque e : disponiveis) {
                    document.add(new Paragraph(
                            String.format("%s (R$ %.2f) | Disponível: %d",
                                    e.getProduto().getNome(),
                                    e.getProduto().getPreco(),
                                    e.getQuantidade())
                    ));
                }
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF do cardápio", e);
        }
    }
    public List<CardapioDTO> listarProdutosDisponiveis() {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getEstoque() != null && p.getEstoque().getQuantidade() > 0)
                .map(CardapioDTO::fromProduto)
                .toList();
    }
}

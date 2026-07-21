package com.salgaki.service;

import com.salgaki.dto.EstoqueCreateDTO;
import com.salgaki.model.Estoque;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.Produto;
import com.salgaki.model.TipoMovimentacao;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.repository.MovimentacaoRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.service.exception.EntidadeNaoEncontradaException;
import com.salgaki.service.exception.EstoqueInsuficienteException;
import com.salgaki.service.exception.ValidadeInavalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    @Transactional
    public Estoque criarEstoque(EstoqueCreateDTO dto) {

        Produto produto = produtoRepository.findById(dto.getProdutoId())
                .orElseThrow(() ->
                        new EntidadeNaoEncontradaException("Produto não encontrado."));

        Estoque lote = new Estoque();
        lote.setProduto(produto);
        lote.setQuantidade(dto.getQuantidade());
        lote.setDataValidade(dto.getDataValidade());

        lote = estoqueRepository.save(lote);

        movimentacaoRepository.save(
                new MovimentacaoEstoque(
                        null,
                        lote,
                        dto.getQuantidade(),
                        TipoMovimentacao.ENTRADA,
                        LocalDate.now()
                )
        );

        return lote;
    }

    @Transactional
    public Estoque adicionarEstoque(Long produtoId,
                                    Integer quantidade,
                                    LocalDate dataValidade) {

        if (dataValidade.isBefore(LocalDate.now())) {
            throw new ValidadeInavalidaException(
                    "A data de validade não pode ser menor que a data atual."
            );
        }

        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() ->
                        new EntidadeNaoEncontradaException("Produto não encontrado."));

        Estoque lote = new Estoque();
        lote.setProduto(produto);
        lote.setQuantidade(quantidade);
        lote.setDataValidade(dataValidade);

        lote = estoqueRepository.save(lote);

        movimentacaoRepository.save(
                new MovimentacaoEstoque(
                        null,
                        lote,
                        quantidade,
                        TipoMovimentacao.ENTRADA,
                        LocalDate.now()
                )
        );

        return lote;
    }

    @Transactional
    public void removerEstoque(Long produtoId, Integer quantidade) {

        List<Estoque> lotes =
                estoqueRepository.findByProdutoIdOrderByDataValidadeAsc(produtoId);

        if (lotes.isEmpty()) {
            throw new EntidadeNaoEncontradaException("Produto sem estoque.");
        }

        int restante = quantidade;

        for (Estoque lote : lotes) {

            if (restante == 0)
                break;

            int disponivel = lote.getQuantidade();

            if (disponivel == 0)
                continue;

            int retirada = Math.min(disponivel, restante);

            lote.setQuantidade(disponivel - retirada);

            movimentacaoRepository.save(
                    new MovimentacaoEstoque(
                            null,
                            lote,
                            retirada,
                            TipoMovimentacao.SAIDA,
                            LocalDate.now()
                    )
            );

            estoqueRepository.save(lote);

            restante -= retirada;
        }

        if (restante > 0) {
            throw new EstoqueInsuficienteException(
                    "Quantidade insuficiente em estoque."
            );
        }
    }

    public List<Estoque> listarEstoquesBaixos() {

        Map<Produto, Integer> totalPorProduto = estoqueRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        Estoque::getProduto,
                        Collectors.summingInt(Estoque::getQuantidade)
                ));

        return totalPorProduto.entrySet()
                .stream()
                .filter(entry -> entry.getValue() <= 5)
                .map(entry -> {
                    Estoque estoque = new Estoque();
                    estoque.setProduto(entry.getKey());
                    estoque.setQuantidade(entry.getValue());
                    return estoque;
                })
                .toList();
    }

    public List<Estoque> consultarEstoque(Long produtoId) {
        return estoqueRepository.findByProdutoIdOrderByDataValidadeAsc(produtoId);
    }

    public List<Estoque> listarTodos() {
        return estoqueRepository.findAll();
    }
}
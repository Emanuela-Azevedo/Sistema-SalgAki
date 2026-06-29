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
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidade(0);
        estoque.setDataValidade(dto.getDataValidade());

        estoque = estoqueRepository.save(estoque);

        if (dto.getQuantidade() > 0) {
            adicionarEstoque(
                    produto.getId(),
                    dto.getQuantidade(),
                    dto.getDataValidade()
            );

            estoque = consultarEstoque(produto.getId());
        }

        return estoque;
    }


    @Transactional
    public Estoque adicionarEstoque(Long produtoId, Integer quantidade, LocalDate dataValidade) {

        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseGet(() -> {
                    Produto produto = produtoRepository.findById(produtoId)
                            .orElseThrow(() ->
                                    new EntidadeNaoEncontradaException("Produto não encontrado"));

                    Estoque novo = new Estoque();
                    novo.setProduto(produto);
                    novo.setQuantidade(0);
                    novo.setDataValidade(dataValidade);

                    return estoqueRepository.save(novo);
                });

        estoque.setQuantidade(estoque.getQuantidade() + quantidade);

        movimentacaoRepository.save(
                new MovimentacaoEstoque(
                        null,
                        estoque,
                        quantidade,
                        TipoMovimentacao.ENTRADA,
                        LocalDateTime.now()
                )
        );

        return estoqueRepository.save(estoque);
    }

    @Transactional
    public Estoque removerEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Estoque não encontrado para produto " + produtoId));

        if (estoque.getQuantidade() < quantidade) {
            throw new EstoqueInsuficienteException("Quantidade insuficiente em estoque");
        }

        estoque.setQuantidade(estoque.getQuantidade() - quantidade);

        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                estoque,
                quantidade,
                TipoMovimentacao.SAIDA,
                LocalDateTime.now()
        );
        movimentacaoRepository.save(movimentacao);

        return estoqueRepository.save(estoque);
    }

    public Estoque consultarEstoque(Long produtoId) {
        return estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Estoque não encontrado para produto " + produtoId));
    }
    public List<Estoque> listarEstoquesBaixos() {
        return estoqueRepository.findByQuantidadeLessThan(5);
    }

}
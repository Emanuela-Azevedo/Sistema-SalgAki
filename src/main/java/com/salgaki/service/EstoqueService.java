package com.salgaki.service;

import com.salgaki.model.Estoque;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.Produto;
import com.salgaki.model.TipoMovimentacao;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.repository.MovimentacaoRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.service.exception.EstoqueInsuficienteException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final ProdutoRepository produtoRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    @Transactional
    public Estoque criarEstoque(Produto produto) {
        Estoque estoque = new Estoque();
        estoque.setProduto(produto);
        estoque.setQuantidade(0); // inicia sempre com 0
        return estoqueRepository.save(estoque);
    }

    @Transactional
    public Estoque adicionarEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado para produto " + produtoId));

        estoque.setQuantidade(estoque.getQuantidade() + quantidade);

        // registrar movimentação
        MovimentacaoEstoque movimentacao = new MovimentacaoEstoque(
                null,
                estoque,
                quantidade,
                TipoMovimentacao.ENTRADA,
                LocalDateTime.now()
        );
        movimentacaoRepository.save(movimentacao);

        return estoqueRepository.save(estoque);
    }

    @Transactional
    public Estoque removerEstoque(Long produtoId, Integer quantidade) {
        Estoque estoque = estoqueRepository.findByProdutoId(produtoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Estoque não encontrado para produto " + produtoId));

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
                .orElseThrow(() -> new RuntimeException("Estoque não encontrado para produto " + produtoId));
    }
    public List<Estoque> listarEstoquesBaixos() {
        // exemplo: limite fixo de 5 unidades
        return estoqueRepository.findByQuantidadeLessThan(5);
    }

}
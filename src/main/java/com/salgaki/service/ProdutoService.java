package com.salgaki.service;

import com.salgaki.model.Estoque;
import com.salgaki.model.Produto;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.service.exception.EntidadeEmUsoException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;


    public Produto criar(Produto produto) {
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    public Produto atualizar(Long id, Produto produtoAtualizado) {
        Produto existente = buscarPorId(id);
        existente.setNome(produtoAtualizado.getNome());
        existente.setPreco(produtoAtualizado.getPreco());
        existente.setCategoria(produtoAtualizado.getCategoria());
        return produtoRepository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = buscarPorId(id);

        Estoque estoque = estoqueRepository.findByProdutoId(id)
                .orElse(null);

        if (estoque != null && estoque.getQuantidade() != null && estoque.getQuantidade() > 0) {
            throw new EntidadeEmUsoException("Produto não pode ser excluído, pois ainda existem itens em estoque.");
        }

        if (estoque != null && estoque.getMovimentacoes() != null && !estoque.getMovimentacoes().isEmpty()) {
            throw new EntidadeEmUsoException("Produto não pode ser excluído, pois possui movimentações de estoque.");
        }

        if (estoque != null) {
            estoqueRepository.delete(estoque);
        }

        produtoRepository.delete(produto);
    }
}

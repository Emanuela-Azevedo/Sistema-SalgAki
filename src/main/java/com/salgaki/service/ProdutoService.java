package com.salgaki.service;

import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.mapper.ProdutoMapper;
import com.salgaki.model.Categoria;
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

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final CategoriaService categoriaService;

    public Produto criar(ProdutoCreateDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());
        Produto produto = ProdutoMapper.toProduto(dto, categoria);
        return produtoRepository.save(produto);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
    }

    public List<Produto> listar(String nome, Long categoriaId, boolean alfabetica) {
        List<Produto> produtos = produtoRepository.findAll();

        if (nome != null && !nome.isBlank()) {
            produtos = produtos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }

        if (categoriaId != null) {
            produtos = produtos.stream()
                    .filter(p -> p.getCategoria().getId().equals(categoriaId))
                    .toList();
        }

        if (alfabetica) {
            produtos = produtos.stream()
                    .sorted(Comparator.comparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }

        return produtos;
    }

    public Produto atualizar(Long id, ProdutoCreateDTO dto) {
        Produto existente = buscarPorId(id);
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());

        existente.setNome(dto.getNome());
        existente.setPreco(dto.getPreco());
        existente.setCategoria(categoria);

        return produtoRepository.save(existente);
    }

    @Transactional
    public void deletar(Long id) {
        Produto produto = buscarPorId(id);

        Estoque estoque = estoqueRepository.findByProdutoId(id).orElse(null);

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

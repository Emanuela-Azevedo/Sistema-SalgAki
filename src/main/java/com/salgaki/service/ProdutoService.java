package com.salgaki.service;

import com.salgaki.model.Produto;
import com.salgaki.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    @Transactional
    public Produto criar(Produto produto) {
        return produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarTodos() {
        return produtoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto atualizar(Long id, Produto dados) {
        Produto produto = buscarPorId(id);
        produto.setNome(dados.getNome());
        produto.setPreco(dados.getPreco());
        produto.setQuantidade(dados.getQuantidade());
        produto.setCategoria(dados.getCategoria());
        return produtoRepository.save(produto);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        produtoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findByNomeContainingIgnoreCase(nome);
    }

    @Transactional(readOnly = true)
    public List<Produto> filtrarPorCategoria(String categoria) {
        return produtoRepository.findByCategoriaIgnoreCase(categoria);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarOrdemAlfabetica() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }
}

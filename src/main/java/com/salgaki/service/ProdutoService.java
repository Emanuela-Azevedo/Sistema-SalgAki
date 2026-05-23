package com.salgaki.service;

import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.service.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;

    public ProdutoService(ProdutoRepository produtoRepository, CategoriaService categoriaService) {
        this.produtoRepository = produtoRepository;
        this.categoriaService = categoriaService;
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
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + id));
    }

    @Transactional
    public Produto atualizar(Long id, Produto dados) {
        Produto produto = buscarPorId(id);
        produto.setNome(dados.getNome());
        produto.setPreco(dados.getPreco());
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
    public List<Produto> filtrarPorCategoria(Long categoriaId) {
        categoriaService.buscarPorId(categoriaId);
        return produtoRepository.findByCategoria_Id(categoriaId);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarOrdemAlfabetica() {
        return produtoRepository.findAllByOrderByNomeAsc();
    }
}

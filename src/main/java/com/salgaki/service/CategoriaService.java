package com.salgaki.service;

import com.salgaki.model.Categoria;
import com.salgaki.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public Categoria criar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + id));
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dados) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(dados.getNome());
        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);
        categoriaRepository.deleteById(id);
    }
}

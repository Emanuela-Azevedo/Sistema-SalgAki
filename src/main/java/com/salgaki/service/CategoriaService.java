package com.salgaki.service;

import com.salgaki.model.Categoria;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.service.exception.EntidadeDuplicadaException;
import com.salgaki.service.exception.EntidadeEmUsoException;
import com.salgaki.service.exception.EntidadeNaoEncontradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;


    @Transactional
    public Categoria criar(Categoria categoria) {
        validarNomeDuplicado(categoria.getNome(), null);
        return categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Categoria buscarPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Categoria não cadastrada."));
    }

    @Transactional
    public Categoria atualizar(Long id, Categoria dados) {
        Categoria categoria = buscarPorId(id);

        validarNomeDuplicado(dados.getNome(), id);

        categoria.setNome(dados.getNome());

        return categoriaRepository.save(categoria);
    }

    @Transactional
    public void deletar(Long id) {
        buscarPorId(id);

        try {
            categoriaRepository.deleteById(id);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(
                    "Não é possível excluir a categoria, pois ela está sendo utilizada por um ou mais produtos."
            );
        }
    }
    private void validarNomeDuplicado(String nome, Long idCategoria) {
        categoriaRepository.findByNomeIgnoreCase(nome)
                .filter(categoria -> idCategoria == null || !categoria.getId().equals(idCategoria))
                .ifPresent(categoria -> {
                    throw new EntidadeDuplicadaException(
                            "Categoria \"" + nome + "\" já cadastrada."
                    );
                });
    }
}

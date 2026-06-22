package com.salgaki.repository;

import com.salgaki.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByCategoria_Id(Long categoriaId);

    List<Produto> findAllByOrderByNomeAsc();
}
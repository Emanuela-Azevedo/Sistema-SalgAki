package com.salgaki.repository;

import com.salgaki.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    List<Estoque> findByProdutoId(Long produtoId);

    List<Estoque> findByProdutoIdOrderByDataValidadeAsc(Long produtoId);

    List<Estoque> findByQuantidadeGreaterThan(int quantidade);

    List<Estoque> findAll();
}

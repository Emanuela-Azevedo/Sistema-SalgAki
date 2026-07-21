package com.salgaki.repository;

import com.salgaki.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByEstoqueProdutoIdAndDataMovimentacaoBetween(
            Long produtoId,
            LocalDate de,
            LocalDate ate
    );
}
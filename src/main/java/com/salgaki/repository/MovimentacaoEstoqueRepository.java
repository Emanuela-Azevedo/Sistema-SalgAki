package com.salgaki.repository;

import com.salgaki.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @Query("""
        SELECT COALESCE(
            SUM(CASE WHEN m.tipo = 'ENTRADA' THEN m.quantidade ELSE -m.quantidade END), 0
        )
        FROM MovimentacaoEstoque m WHERE m.produto.id = :produtoId
    """)
    Integer calcularSaldo(@Param("produtoId") Long produtoId);

    List<MovimentacaoEstoque> findByProdutoIdAndDataHoraBetweenOrderByDataHoraAsc(
            Long produtoId, LocalDateTime de, LocalDateTime ate);
}

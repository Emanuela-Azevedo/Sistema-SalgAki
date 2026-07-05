package com.salgaki.service;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.TipoMovimentacao;
import com.salgaki.repository.MovimentacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoEstoqueRepository;

    public RelatorioMovimentacaoDTO gerarRelatorio(Long produtoId, LocalDateTime de, LocalDateTime ate) {
        validarPeriodo(de, ate);

        List<MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository
                .findByEstoqueProdutoIdAndDataMovimentacaoBetween(produtoId, de, ate);

        int entradas = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.ENTRADA)
                .mapToInt(MovimentacaoEstoque::getQuantidade)
                .sum();

        int saidas = movimentacoes.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.SAIDA)
                .mapToInt(MovimentacaoEstoque::getQuantidade)
                .sum();

        int saldo = entradas - saidas;

        String produtoNome = movimentacoes.isEmpty()
                ? null
                : movimentacoes.get(0).getEstoque().getProduto().getNome();

        return new RelatorioMovimentacaoDTO(
                produtoId,
                produtoNome,
                entradas,
                saidas,
                saldo,
                movimentacoes.stream().map(this::toResponseDTO).collect(Collectors.toList())
        );
    }

    public List<MovimentacaoResponseDTO> listarMovimentacoes(Long produtoId, LocalDateTime de, LocalDateTime ate) {
        validarPeriodo(de, ate);

        return movimentacaoEstoqueRepository
                .findByEstoqueProdutoIdAndDataMovimentacaoBetween(produtoId, de, ate)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private void validarPeriodo(LocalDateTime de, LocalDateTime ate) {
        if (de.isAfter(ate)) {
            throw new IllegalArgumentException("Data inicial não pode ser maior que a final");
        }
    }

    private MovimentacaoResponseDTO toResponseDTO(MovimentacaoEstoque m) {
        return new MovimentacaoResponseDTO(
                m.getId(),
                m.getEstoque().getProduto().getId(),
                m.getEstoque().getProduto().getNome(),
                m.getTipo(),
                m.getQuantidade(),
                m.getDataMovimentacao()
        );
    }
}

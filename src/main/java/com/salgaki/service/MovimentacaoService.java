package com.salgaki.service;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.dto.mapper.MovimentacaoMapper;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.TipoMovimentacao;
import com.salgaki.repository.MovimentacaoRepository;
import com.salgaki.service.exception.DataInvalidaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoEstoqueRepository;

    public List<MovimentacaoEstoque> buscarMovimentacoes(Long produtoId, LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);
        return movimentacaoEstoqueRepository
                .findByEstoqueProdutoIdAndDataMovimentacaoBetween(produtoId, de, ate);
    }

    public RelatorioMovimentacaoDTO gerarRelatorio(Long produtoId, LocalDate de, LocalDate ate) {
        validarPeriodo(de, ate);

        List<MovimentacaoEstoque> movimentacoes = buscarMovimentacoes(produtoId, de, ate);

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
                null
        );
    }

    private void validarPeriodo(LocalDate de, LocalDate ate) {
        if (de.isAfter(ate)) {
            throw new DataInvalidaException("Data inicial não pode ser maior que a final");
        }
    }
}

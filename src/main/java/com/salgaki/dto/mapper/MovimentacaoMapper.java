package com.salgaki.dto.mapper;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.model.MovimentacaoEstoque;

public class MovimentacaoMapper {
    public static MovimentacaoResponseDTO toDto(MovimentacaoEstoque movimentacao) {
        return new MovimentacaoResponseDTO(
                movimentacao.getId(),
                movimentacao.getEstoque().getId(),
                movimentacao.getEstoque().getProduto().getNome(),
                movimentacao.getTipo(),
                movimentacao.getQuantidade(),
                movimentacao.getDataMovimentacao()
        );
    }
}

package com.salgaki.dto.mapper;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.model.MovimentacaoEstoque;

public class MovimentacaoMapper {

    public static MovimentacaoResponseDTO toResponseDTO(MovimentacaoEstoque m) {

        return new MovimentacaoResponseDTO(
                m.getId(),
                m.getEstoque().getId(),
                m.getEstoque().getProduto().getNome(),
                m.getEstoque().getDataValidade(),
                m.getTipo(),
                m.getQuantidade(),
                m.getDataMovimentacao()
        );
    }
}
package com.salgaki.dto;

import com.salgaki.model.MovimentacaoEstoque.TipoMovimentacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimentacaoDTO {

    @NotNull
    private TipoMovimentacao tipo;

    @NotNull
    @Positive
    private Integer quantidade;
}

package com.salgaki.dto;

import com.salgaki.model.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimentacaoCreateDTO {
    private TipoMovimentacao tipo;
    private Integer quantidade;
}

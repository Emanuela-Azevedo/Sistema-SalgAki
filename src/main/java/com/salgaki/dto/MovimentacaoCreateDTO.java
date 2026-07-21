package com.salgaki.dto;

import com.salgaki.model.TipoMovimentacao;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimentacaoCreateDTO {
    private TipoMovimentacao tipo;
    @Min(0)
    @Max(1000)
    private Integer quantidade;
}

package com.salgaki.dto;

import com.salgaki.model.TipoMovimentacao;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MovimentacaoResponseDTO {
    private Long id;
    private Long estoqueId;
    private String produtoNome;
    private LocalDate dataValidade;
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private LocalDate dataHora;
}

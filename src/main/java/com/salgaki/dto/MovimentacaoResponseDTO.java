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
    private Long id;                // id da movimentação
    private Long produtoId;
    private String produtoNome;
    private Long estoqueId;         // novo campo
    private LocalDate dataValidade; // novo campo
    private TipoMovimentacao tipo;
    private Integer quantidade;
    private LocalDateTime dataHora;
}

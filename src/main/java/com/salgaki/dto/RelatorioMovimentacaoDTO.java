package com.salgaki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioMovimentacaoDTO {
    private Long produtoId;
    private String produtoNome;
    private Integer totalEntradas;
    private Integer totalSaidas;
    private Integer saldoPeriodo;
    private List<MovimentacaoResponseDTO> movimentacoes;

}
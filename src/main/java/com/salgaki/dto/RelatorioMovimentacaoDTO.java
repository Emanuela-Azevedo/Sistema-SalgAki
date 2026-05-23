package com.salgaki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RelatorioMovimentacaoDTO {

    private String produto;
    private int totalEntradas;
    private int totalSaidas;
    private int saldoPeriodo;
    private List<ItemMovimentacaoDTO> movimentacoes;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemMovimentacaoDTO {
        private String tipo;
        private Integer quantidade;
        private LocalDateTime dataHora;
    }
}

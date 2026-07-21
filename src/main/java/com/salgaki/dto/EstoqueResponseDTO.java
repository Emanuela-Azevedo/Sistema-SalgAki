package com.salgaki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstoqueResponseDTO {
    private Long id;
    private String produtoNome;
    private Integer quantidade;
    private LocalDate dataValidade;
}



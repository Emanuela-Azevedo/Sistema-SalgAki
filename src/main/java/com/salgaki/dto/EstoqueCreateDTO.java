package com.salgaki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EstoqueCreateDTO {
    private Long produtoId;
    private Integer quantidade;
    private LocalDate dataValidade;
}

package com.salgaki.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    @FutureOrPresent
    private LocalDate dataValidade;
}

package com.salgaki.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
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
    @Min(0)
    private Integer quantidade;
    @NotNull
    @FutureOrPresent
    private LocalDate dataValidade;
}

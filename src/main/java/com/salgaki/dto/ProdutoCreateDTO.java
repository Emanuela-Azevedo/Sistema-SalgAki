package com.salgaki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoCreateDTO {

    @NotBlank
    private String nome;

    @NotNull
    @Positive
    private Double preco;

    @NotNull
    @PositiveOrZero
    private Integer quantidade;

    @NotBlank
    private String categoria;
}

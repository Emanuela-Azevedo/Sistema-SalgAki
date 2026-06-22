package com.salgaki.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProdutoCreateDTO {

    @NotBlank
    private String nome;

    @NotNull
    @Min(0)
    private Double preco;

    @NotNull
    private Long categoriaId;

}


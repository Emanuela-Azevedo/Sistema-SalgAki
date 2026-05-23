package com.salgaki.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioCreateDTO {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}

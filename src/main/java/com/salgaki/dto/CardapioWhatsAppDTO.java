package com.salgaki.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CardapioWhatsAppDTO {

    @NotBlank(message = "Número de WhatsApp é obrigatório")
    @Pattern(regexp = "^\\+?[0-9()\s-]{10,20}$", message = "Número deve conter entre 10 e 20 caracteres válidos: dígitos, +, espaço, parênteses ou traço")
    private String numeroWhatsApp;
}

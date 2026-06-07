package com.salgaki.controller;

import com.salgaki.dto.CardapioWhatsAppDTO;
import com.salgaki.service.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cardapio")
public class CardapioController {

    private final WhatsAppService whatsAppService;

    @PostMapping("/enviar-whatsapp")
    public ResponseEntity<String> enviarCardapioWhatsApp(
            @RequestBody @Valid CardapioWhatsAppDTO dto) {
        try {
            whatsAppService.enviarCardapio(dto.getNumeroWhatsApp());
            return ResponseEntity.ok("Cardápio enviado com sucesso para " + dto.getNumeroWhatsApp());
        } catch (Exception e) {
            log.error("Erro ao enviar cardápio", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao enviar cardápio: " + e.getMessage());
        }
    }
}

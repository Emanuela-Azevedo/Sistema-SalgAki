package com.salgaki.controller;

import com.salgaki.service.CardapioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cardapio")
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/texto")
    public ResponseEntity<String> gerarCardapioTexto() {
        return ResponseEntity.ok(cardapioService.gerarTextoCardapio());
    }
}
package com.salgaki.controller;

import com.salgaki.config.CardapioPdfGenerator;
import com.salgaki.dto.CardapioDTO;
import com.salgaki.service.CardapioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/cardapio")
public class CardapioController {

    private final CardapioService cardapioService;
    private final CardapioPdfGenerator cardapioPdfGenerator;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> gerarCardapioPdf() {
        List<CardapioDTO> produtosDisponiveis = cardapioService.listarProdutosDisponiveis();

        byte[] pdfBytes = cardapioPdfGenerator.gerarPdf(produtosDisponiveis);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=cardapio.pdf")
                .body(pdfBytes);
    }
}
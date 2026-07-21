package com.salgaki.controller;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.dto.mapper.MovimentacaoMapper;
import com.salgaki.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    @GetMapping("/{produtoId}/relatorio")
    public ResponseEntity<RelatorioMovimentacaoDTO> relatorio(
            @PathVariable Long produtoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate ate) {

        RelatorioMovimentacaoDTO relatorio = movimentacaoService.gerarRelatorio(produtoId, de, ate);

        List<MovimentacaoResponseDTO> movimentacoesDTO = movimentacaoService.buscarMovimentacoes(produtoId, de, ate)
                .stream()
                .map(MovimentacaoMapper::toResponseDTO)
                .toList();

        relatorio.setMovimentacoes(movimentacoesDTO);

        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/{produtoId}/detalhes")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes(
            @PathVariable Long produtoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate ate) {

        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.buscarMovimentacoes(produtoId, de, ate)
                .stream()
                .map(MovimentacaoMapper::toResponseDTO)
                .toList();

        return movimentacoes.isEmpty()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(movimentacoes);
    }
}
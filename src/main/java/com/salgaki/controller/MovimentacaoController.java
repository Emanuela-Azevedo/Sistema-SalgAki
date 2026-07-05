package com.salgaki.controller;

import com.salgaki.dto.MovimentacaoResponseDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.service.MovimentacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {

        RelatorioMovimentacaoDTO relatorio = movimentacaoService.gerarRelatorio(produtoId, de, ate);
        return ResponseEntity.ok(relatorio);
    }

    @GetMapping("/{produtoId}/detalhes")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes(
            @PathVariable Long produtoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {

        List<MovimentacaoResponseDTO> movimentacoes = movimentacaoService.listarMovimentacoes(produtoId, de, ate);

        if (movimentacoes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(movimentacoes);
    }
}

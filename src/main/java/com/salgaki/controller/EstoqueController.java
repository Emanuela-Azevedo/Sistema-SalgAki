package com.salgaki.controller;

import com.salgaki.dto.EstoqueResponseDTO;
import com.salgaki.dto.mapper.EstoqueMapper;
import com.salgaki.model.Estoque;
import com.salgaki.service.EstoqueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping("/{produtoId}")
    public ResponseEntity<EstoqueResponseDTO> consultar(@PathVariable Long produtoId) {
        Estoque estoque = estoqueService.consultarEstoque(produtoId);
        return ResponseEntity.ok(EstoqueMapper.toDto(estoque));
    }

    @PutMapping("/{produtoId}/entrada")
    public ResponseEntity<EstoqueResponseDTO> entrada(@PathVariable Long produtoId,
                                                      @RequestParam Integer quantidade) {
        Estoque estoque = estoqueService.adicionarEstoque(produtoId, quantidade);
        return ResponseEntity.ok(EstoqueMapper.toDto(estoque));
    }

    @PutMapping("/{produtoId}/saida")
    public ResponseEntity<EstoqueResponseDTO> saida(@PathVariable Long produtoId,
                                                    @RequestParam Integer quantidade) {
        Estoque estoque = estoqueService.removerEstoque(produtoId, quantidade);
        return ResponseEntity.ok(EstoqueMapper.toDto(estoque));
    }
    @GetMapping("/baixo")
    public ResponseEntity<List<EstoqueResponseDTO>> listarEstoquesBaixos() {
        List<Estoque> estoquesBaixos = estoqueService.listarEstoquesBaixos();
        List<EstoqueResponseDTO> dtos = estoquesBaixos.stream()
                .map(EstoqueMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }


}

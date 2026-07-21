package com.salgaki.controller;

import com.salgaki.dto.EstoqueCreateDTO;
import com.salgaki.dto.EstoqueResponseDTO;
import com.salgaki.dto.mapper.EstoqueMapper;
import com.salgaki.model.Estoque;
import com.salgaki.model.Produto;
import com.salgaki.service.EstoqueService;
import com.salgaki.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/estoques")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping
    public ResponseEntity<EstoqueResponseDTO> criar(
            @RequestBody @Valid EstoqueCreateDTO dto) {

        Estoque estoque = estoqueService.criarEstoque(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EstoqueMapper.toDto(estoque));
    }

    @GetMapping("/{produtoId}")
    public ResponseEntity<List<EstoqueResponseDTO>> consultar(
            @PathVariable Long produtoId) {

        return ResponseEntity.ok(
                EstoqueMapper.toDtoList(
                        estoqueService.consultarEstoque(produtoId)
                )
        );
    }

    @PostMapping("/{produtoId}/entrada")
    public ResponseEntity<EstoqueResponseDTO> entrada(
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade,
            @RequestParam LocalDate dataValidade) {

        Estoque lote = estoqueService.adicionarEstoque(
                produtoId,
                quantidade,
                dataValidade
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EstoqueMapper.toDto(lote));
    }

    @PutMapping("/{produtoId}/saida")
    public ResponseEntity<Void> saida(
            @PathVariable Long produtoId,
            @RequestParam Integer quantidade) {

        estoqueService.removerEstoque(produtoId, quantidade);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/baixo")
    public ResponseEntity<List<EstoqueResponseDTO>> listarEstoquesBaixos() {

        return ResponseEntity.ok(
                EstoqueMapper.toDtoList(
                        estoqueService.listarEstoquesBaixos()
                )
        );
    }
    @GetMapping
    public ResponseEntity<List<EstoqueResponseDTO>> listarTodos() {
        return ResponseEntity.ok(
                EstoqueMapper.toDtoList(
                        estoqueService.listarTodos()
                )
        );
    }
}
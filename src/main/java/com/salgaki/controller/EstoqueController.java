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
    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<EstoqueResponseDTO> criar(@RequestBody @Valid EstoqueCreateDTO dto) {
        Produto produto = produtoService.buscarPorId(dto.getProdutoId());
        Estoque estoque = estoqueService.criarEstoque(produto, dto.getDataValidade());
        if (dto.getQuantidade() > 0) {
            estoqueService.adicionarEstoque(produto.getId(), dto.getQuantidade(),dto.getDataValidade());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(EstoqueMapper.toDto(estoque));
    }



    @GetMapping("/{produtoId}")
    public ResponseEntity<EstoqueResponseDTO> consultar(@PathVariable Long produtoId) {
        Estoque estoque = estoqueService.consultarEstoque(produtoId);
        return ResponseEntity.ok(EstoqueMapper.toDto(estoque));
    }

    @PutMapping("/{produtoId}/entrada")
    public ResponseEntity<EstoqueResponseDTO> entrada(@PathVariable Long produtoId,
                                                      @RequestParam Integer quantidade, @RequestParam LocalDate dataValidade) {
        Estoque estoque = estoqueService.adicionarEstoque(produtoId, quantidade,dataValidade);
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

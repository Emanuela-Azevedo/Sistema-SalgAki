package com.salgaki.controller;

import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.ProdutoResponseDTO;
import com.salgaki.dto.mapper.ProdutoMapper;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.ProdutoService;
import com.salgaki.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;
    private final EstoqueService estoqueService;

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody @Valid ProdutoCreateDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());
        Produto produto = produtoService.criar(ProdutoMapper.toProduto(dto, categoria));

        // cria estoque inicial = 0 para o produto
        estoqueService.criarEstoque(produto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProdutoMapper.toDto(produto));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false, defaultValue = "false") boolean alfabetica
    ) {
        List<Produto> produtos = produtoService.listarTodos();

        if (nome != null && !nome.isBlank()) {
            produtos = produtos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
        }

        if (categoriaId != null) {
            produtos = produtos.stream()
                    .filter(p -> p.getCategoria().getId().equals(categoriaId))
                    .toList();
        }

        if (alfabetica) {
            produtos = produtos.stream()
                    .sorted((p1, p2) -> p1.getNome().compareToIgnoreCase(p2.getNome()))
                    .toList();
        }

        return ResponseEntity.ok(produtos.stream()
                .map((Produto produto) -> ProdutoMapper.toDto(produto))
                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(ProdutoMapper.toDto(produto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoCreateDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());
        Produto atualizado = produtoService.atualizar(id, ProdutoMapper.toProduto(dto, categoria));
        return ResponseEntity.ok(ProdutoMapper.toDto(atualizado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    // Entrada de estoque
    @PutMapping("/{id}/entrada")
    public ResponseEntity<Void> entradaEstoque(@PathVariable Long id, @RequestParam Integer quantidade) {
        estoqueService.adicionarEstoque(id, quantidade);
        return ResponseEntity.ok().build();
    }

    // Saída de estoque
    @PutMapping("/{id}/saida")
    public ResponseEntity<Void> saidaEstoque(@PathVariable Long id, @RequestParam Integer quantidade) {
        estoqueService.removerEstoque(id, quantidade);
        return ResponseEntity.ok().build();
    }
}
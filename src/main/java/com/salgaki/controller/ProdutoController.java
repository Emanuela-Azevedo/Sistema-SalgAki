package com.salgaki.controller;

import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.ProdutoResponseDTO;
import com.salgaki.dto.mapper.ProdutoMapper;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/products")
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> criar(@RequestBody @Valid ProdutoCreateDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());
        Produto produto = produtoService.criar(ProdutoMapper.toProduto(dto, categoria));
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoMapper.toDto(produto));
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponseDTO>> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false, defaultValue = "false") boolean alfabetica) {

        List<Produto> produtos;

        if (nome != null) {
            produtos = produtoService.buscarPorNome(nome);
        } else if (categoriaId != null) {
            produtos = produtoService.filtrarPorCategoria(categoriaId);
        } else if (alfabetica) {
            produtos = produtoService.listarOrdemAlfabetica();
        } else {
            produtos = produtoService.listarTodos();
        }

        return ResponseEntity.ok(produtos.stream().map(ProdutoMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ProdutoMapper.toDto(produtoService.buscarPorId(id)));
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
}

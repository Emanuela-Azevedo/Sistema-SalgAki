package com.salgaki.controller;

import com.salgaki.dto.MovimentacaoDTO;
import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.ProdutoResponseDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.dto.mapper.ProdutoMapper;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.EstoqueService;
import com.salgaki.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProdutoMapper.toDto(produto, 0));
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

        return ResponseEntity.ok(toListDto(produtos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(ProdutoMapper.toDto(produto, estoqueService.calcularSaldo(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid ProdutoCreateDTO dto) {
        Categoria categoria = categoriaService.buscarPorId(dto.getCategoriaId());
        Produto atualizado = produtoService.atualizar(id, ProdutoMapper.toProduto(dto, categoria));
        return ResponseEntity.ok(ProdutoMapper.toDto(atualizado, estoqueService.calcularSaldo(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        produtoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/estoque")
    public ResponseEntity<ProdutoResponseDTO> movimentarEstoque(
            @PathVariable Long id,
            @RequestBody @Valid MovimentacaoDTO dto) {
        int novoSaldo = estoqueService.movimentar(id, dto.getTipo(), dto.getQuantidade());
        Produto produto = produtoService.buscarPorId(id);
        return ResponseEntity.ok(ProdutoMapper.toDto(produto, novoSaldo));
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<List<ProdutoResponseDTO>> estoqueBaixo() {
        return ResponseEntity.ok(toListDto(estoqueService.listarEstoqueBaixo()));
    }

    @GetMapping("/{id}/movimentacoes")
    public ResponseEntity<RelatorioMovimentacaoDTO> relatorio(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime ate) {
        return ResponseEntity.ok(estoqueService.gerarRelatorio(id, de, ate));
    }

    private List<ProdutoResponseDTO> toListDto(List<Produto> produtos) {
        return produtos.stream()
                .map(p -> ProdutoMapper.toDto(p, estoqueService.calcularSaldo(p.getId())))
                .toList();
    }
}

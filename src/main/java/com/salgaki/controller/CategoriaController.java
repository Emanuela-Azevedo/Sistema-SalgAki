package com.salgaki.controller;

import com.salgaki.dto.CategoriaCreateDTO;
import com.salgaki.dto.CategoriaResponseDTO;
import com.salgaki.dto.mapper.CategoriaMapper;
import com.salgaki.model.Categoria;
import com.salgaki.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> criar(@RequestBody @Valid CategoriaCreateDTO dto) {
        Categoria categoria = categoriaService.criar(CategoriaMapper.toCategoria(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(CategoriaMapper.toDto(categoria));
    }

    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listarTodas().stream().map(CategoriaMapper::toDto).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(CategoriaMapper.toDto(categoriaService.buscarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid CategoriaCreateDTO dto) {
        Categoria atualizada = categoriaService.atualizar(id, CategoriaMapper.toCategoria(dto));
        return ResponseEntity.ok(CategoriaMapper.toDto(atualizada));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}

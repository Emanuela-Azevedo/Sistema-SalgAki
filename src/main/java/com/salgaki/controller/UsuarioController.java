package com.salgaki.controller;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.dto.UsuarioResponseDTO;
import com.salgaki.dto.mapper.UsuarioMapper;
import com.salgaki.model.Usuario;
import com.salgaki.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody @Valid UsuarioCreateDTO dto) {
        Usuario salvo = usuarioService.criarUsuario(UsuarioMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponseDTO(salvo));
    }

    
    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> obterUsuario() {
        Usuario usuario = usuarioService.buscarUsuario();
        return ResponseEntity.ok(UsuarioMapper.toResponseDTO(usuario));
    }

    @PutMapping("/senha")
    public ResponseEntity<UsuarioResponseDTO> atualizarSenha(@RequestParam String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        Usuario atualizado = usuarioService.atualizarSenha(novaSenha);
        return ResponseEntity.ok(UsuarioMapper.toResponseDTO(atualizado));
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> usuarioExiste() {
        return ResponseEntity.ok(usuarioService.existeUsuario());
    }
}

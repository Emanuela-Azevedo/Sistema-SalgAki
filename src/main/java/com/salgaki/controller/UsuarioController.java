package com.salgaki.controller;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.dto.UsuarioResponseDTO;
import com.salgaki.dto.mapper.UsuarioMapper;
import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody UsuarioCreateDTO dto) {
        // Converte DTO para entidade
        Usuario usuario = UsuarioMapper.toEntity(dto);

        // Codifica a senha antes de salvar
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Salva no banco
        Usuario salvo = usuarioRepository.save(usuario);

        // Converte para DTO de resposta
        UsuarioResponseDTO response = UsuarioMapper.toResponseDTO(salvo);

        return ResponseEntity.ok(response);
    }
}
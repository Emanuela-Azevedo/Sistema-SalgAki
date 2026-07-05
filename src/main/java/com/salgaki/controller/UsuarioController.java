package com.salgaki.controller;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.dto.UsuarioResponseDTO;
import com.salgaki.dto.mapper.UsuarioMapper;
import com.salgaki.model.Usuario;
import com.salgaki.security.JwtUserDetailsService;
import com.salgaki.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtUserDetailsService jwtService;

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody @Valid UsuarioCreateDTO dto) {
        Usuario salvo = usuarioService.criarUsuario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioMapper.toResponseDTO(salvo));
    }

    @GetMapping
    public ResponseEntity<UsuarioResponseDTO> obterUsuario() {
        Usuario usuario = usuarioService.buscarUsuario();
        return ResponseEntity.ok(UsuarioMapper.toResponseDTO(usuario));
    }

    @PutMapping("/senha")
    public ResponseEntity<UsuarioResponseDTO> atualizarSenha(
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha) {

        Usuario atualizado = usuarioService.atualizarSenha(senhaAtual, novaSenha);

        String novoToken = jwtService.getTokenAuthenticated(atualizado.getUsername()).getToken();

        UsuarioResponseDTO dto = UsuarioMapper.toResponseDTO(atualizado);

        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + novoToken)
                .body(dto);
    }


    @PutMapping("/username")
    public ResponseEntity<UsuarioResponseDTO> atualizarUsername(
            @RequestParam String senhaAtual,
            @RequestParam String novoUsername) {

        // regra de negócio: valida senha atual e atualiza username
        Usuario atualizado = usuarioService.atualizarUsername(senhaAtual, novoUsername);

        // gerar novo token JWT com o username atualizado
        String novoToken = jwtService.getTokenAuthenticated(atualizado.getUsername()).getToken();

        // montar DTO apenas com dados públicos do usuário
        UsuarioResponseDTO dto = UsuarioMapper.toResponseDTO(atualizado);

        // retornar DTO + token no header Authorization
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + novoToken)
                .body(dto);
    }

    @GetMapping("/existe")
    public ResponseEntity<Boolean> usuarioExiste() {
        return ResponseEntity.ok(usuarioService.existeUsuario());
    }
}


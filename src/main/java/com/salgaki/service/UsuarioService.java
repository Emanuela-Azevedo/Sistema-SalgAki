package com.salgaki.service;

import java.util.Optional;
import java.util.Base64;

public class UsuarioService {

    private UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public LoginResponseDTO autenticar(LoginRequestDTO request) {

        Optional<Usuario> usuarioOpt = usuarioRepository.buscarPorUsername(request.getUsername());

        // valida se encontrou usuário
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuário não encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // valida senha
        if (!usuario.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        // gerar token simples (mock)
        String token = gerarToken(usuario);

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setTipo("Bearer");

        return response;
    }

    private String gerarToken(Usuario usuario) {
        String raw = usuario.getUsername() + ":" + System.currentTimeMillis();
        return Base64.getEncoder().encodeToString(raw.getBytes());
    }
}

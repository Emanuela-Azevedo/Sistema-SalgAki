package com.salgaki.service;

import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria o único usuário do sistema.
     * Se já existir algum usuário, lança exceção.
     */
    @Transactional
    public Usuario criarUsuario(Usuario usuario) {
        if (usuarioRepository.count() > 0) {
            throw new IllegalStateException("Já existe um usuário cadastrado. Não é permitido criar mais.");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        return usuarioRepository.save(usuario);
    }

    /**
     * Busca o único usuário cadastrado.
     */
    @Transactional(readOnly = true)
    public Usuario buscarUsuario() {
        return usuarioRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Nenhum usuário cadastrado."));
    }
}

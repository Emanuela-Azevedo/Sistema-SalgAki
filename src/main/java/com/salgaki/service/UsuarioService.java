package com.salgaki.service;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.dto.mapper.UsuarioMapper;
import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import com.salgaki.service.exception.EntidadeDuplicadaException;
import com.salgaki.service.exception.EntidadeNaoEncontradaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    private Usuario usuarioSingleton;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public synchronized Usuario criarUsuario(UsuarioCreateDTO dto) {
        if (usuarioRepository.count() > 0) {
            throw new EntidadeDuplicadaException("Já existe um usuário cadastrado no sistema. Apenas um usuário é permitido.");
        }

        try {
            Usuario usuario = UsuarioMapper.toEntity(dto);
            usuario.setId(null);
            usuario.setSingletonKey(1);
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            Usuario usuarioSalvo = usuarioRepository.saveAndFlush(usuario);
            this.usuarioSingleton = usuarioSalvo;

            return usuarioSalvo;
        } catch (DataIntegrityViolationException ex) {
            throw new EntidadeDuplicadaException("Já existe um usuário cadastrado no sistema. Apenas um usuário é permitido.");
        }
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuario() {
        if (usuarioSingleton == null) {
            usuarioSingleton = usuarioRepository.findAll()
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new EntidadeNaoEncontradaException("Nenhum usuário cadastrado no sistema."));
        }
        return usuarioSingleton;
    }

    @Transactional
    public Usuario atualizarSenha(String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException("Senha não pode ser vazia");
        }
        Usuario usuario = buscarUsuario();
        usuario.setPassword(passwordEncoder.encode(novaSenha));

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        this.usuarioSingleton = usuarioAtualizado;

        return usuarioAtualizado;
    }

    @Transactional
    public Usuario atualizarUsername(String novoUsername) {
        if (novoUsername == null || novoUsername.isBlank()) {
            throw new IllegalArgumentException("Username não pode ser vazio");
        }
        Usuario usuario = buscarUsuario();
        usuario.setUsername(novoUsername);

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);
        this.usuarioSingleton = usuarioAtualizado;

        return usuarioAtualizado;
    }

    @Transactional(readOnly = true)
    public boolean existeUsuario() {
        return usuarioRepository.count() > 0;
    }

    public void limparCache() {
        this.usuarioSingleton = null;
    }
}

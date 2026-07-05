package com.salgaki.service;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import com.salgaki.service.exception.EntidadeDuplicadaException;
import com.salgaki.service.exception.EntidadeNaoEncontradaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();
        usuarioService.limparCache();
    }

    @Test
    void criarUsuario_QuandoNaoExiste_DeveSalvarUsuario() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");

        Usuario salvo = usuarioService.criarUsuario(dto);

        assertNotNull(salvo.getId());
        assertEquals("usuarioTeste", salvo.getUsername());
        assertTrue(passwordEncoder.matches("senha123", salvo.getPassword()));
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void criarUsuario_QuandoJaExiste_DeveLancarEntidadeDuplicadaException() {
        UsuarioCreateDTO primeiro = new UsuarioCreateDTO("usuarioTeste", "senha123");
        usuarioService.criarUsuario(primeiro);

        UsuarioCreateDTO segundo = new UsuarioCreateDTO("outroUsuario", "senha456");

        assertThrows(EntidadeDuplicadaException.class, () -> usuarioService.criarUsuario(segundo));
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void buscarUsuario_QuandoExiste_DeveRetornarUsuario() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        Usuario salvo = usuarioService.criarUsuario(dto);

        Usuario encontrado = usuarioService.buscarUsuario();

        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals(salvo.getUsername(), encontrado.getUsername());
    }

    @Test
    void buscarUsuario_QuandoNaoExiste_DeveLancarEntityNotFoundException() {
        assertThrows(EntidadeNaoEncontradaException.class, usuarioService::buscarUsuario);
    }

    @Test
    void atualizarSenha_QuandoUsuarioExiste_DeveAtualizarSenha() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        Usuario salvo = usuarioService.criarUsuario(dto);

        Usuario atualizado = usuarioService.atualizarSenha("novaSenha123");

        assertEquals(salvo.getId(), atualizado.getId());
        assertTrue(passwordEncoder.matches("novaSenha123", atualizado.getPassword()));
        assertFalse(passwordEncoder.matches("senha123", atualizado.getPassword()));
    }

    @Test
    void atualizarSenha_QuandoSenhaVazia_DeveLancarIllegalArgumentException() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        usuarioService.criarUsuario(dto);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizarSenha(""));
    }

    @Test
    void atualizarUsername_QuandoUsuarioExiste_DeveAtualizarUsername() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        Usuario salvo = usuarioService.criarUsuario(dto);

        Usuario atualizado = usuarioService.atualizarUsername("novoUsuario");

        assertEquals(salvo.getId(), atualizado.getId());
        assertEquals("novoUsuario", atualizado.getUsername());
    }

    @Test
    void atualizarUsername_QuandoUsernameVazio_DeveLancarIllegalArgumentException() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        usuarioService.criarUsuario(dto);

        assertThrows(IllegalArgumentException.class, () -> usuarioService.atualizarUsername(""));
    }

    @Test
    void existeUsuario_QuandoUsuarioCadastrado_DeveRetornarTrue() {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("usuarioTeste", "senha123");
        usuarioService.criarUsuario(dto);

        assertTrue(usuarioService.existeUsuario());
    }

    @Test
    void existeUsuario_QuandoNaoTemUsuarioCadastrado_DeveRetornarFalse() {
        assertFalse(usuarioService.existeUsuario());
    }
}
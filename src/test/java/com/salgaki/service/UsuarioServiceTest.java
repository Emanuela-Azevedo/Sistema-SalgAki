package com.salgaki.service;

import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import com.salgaki.service.exception.EntidadeDuplicadaException;
import com.salgaki.service.exception.EntityNotFoundException;
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
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioTeste");
        usuario.setPassword("senha123");

        Usuario salvo = usuarioService.criarUsuario(usuario);

        assertNotNull(salvo.getId());
        assertEquals("usuarioTeste", salvo.getUsername());
        assertTrue(passwordEncoder.matches("senha123", salvo.getPassword()));
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void criarUsuario_QuandoJaExiste_DeveLancarEntidadeDuplicadaException() {
        Usuario primeiroUsuario = new Usuario();
        primeiroUsuario.setUsername("usuarioTeste");
        primeiroUsuario.setPassword("senha123");
        usuarioService.criarUsuario(primeiroUsuario);

        Usuario segundoUsuario = new Usuario();
        segundoUsuario.setUsername("outroUsuario");
        segundoUsuario.setPassword("senha456");

        assertThrows(EntidadeDuplicadaException.class, () -> usuarioService.criarUsuario(segundoUsuario));
        assertEquals(1, usuarioRepository.count());
    }

    @Test
    void buscarUsuario_QuandoExiste_DeveRetornarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioTeste");
        usuario.setPassword("senha123");
        Usuario salvo = usuarioService.criarUsuario(usuario);

        Usuario encontrado = usuarioService.buscarUsuario();

        assertEquals(salvo.getId(), encontrado.getId());
        assertEquals(salvo.getUsername(), encontrado.getUsername());
    }

    @Test
    void buscarUsuario_QuandoNaoExiste_DeveLancarEntityNotFoundException() {
        assertThrows(EntityNotFoundException.class, usuarioService::buscarUsuario);
    }

    @Test
    void atualizarSenha_QuandoUsuarioExiste_DeveAtualizarSenha() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioTeste");
        usuario.setPassword("senha123");
        Usuario salvo = usuarioService.criarUsuario(usuario);

        Usuario atualizado = usuarioService.atualizarSenha("novaSenha123");

        assertEquals(salvo.getId(), atualizado.getId());
        assertTrue(passwordEncoder.matches("novaSenha123", atualizado.getPassword()));
        assertFalse(passwordEncoder.matches("senha123", atualizado.getPassword()));
    }

    @Test
    void existeUsuario_QuandoUsuarioCadastrado_DeveRetornarTrue() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioTeste");
        usuario.setPassword("senha123");
        usuarioService.criarUsuario(usuario);

        assertTrue(usuarioService.existeUsuario());
    }

    @Test
    void existeUsuario_QuandoNaoTemUsuarioCadastrado_DeveRetornarFalse() {
        assertFalse(usuarioService.existeUsuario());
    }
}

package com.salgaki.Login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        Usuario usuario = new Usuario();
        usuario.setUsername("usuarioTeste");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);
    }

    @Test
    void login_ComCredenciaisValidas_DeveRetornarToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_ComCredenciaisInvalidas_DeveRetornarErro401() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioInvalido");
        dto.setPassword("senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Credenciais inválidas"));
    }
    @Test
    void logout_DeveRetornarMensagemSucesso() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout realizado com sucesso!"));
    }
}

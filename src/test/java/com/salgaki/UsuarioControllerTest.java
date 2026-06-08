package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UsuarioControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        usuarioRepository.deleteAll();

        // cria usuário inicial com senha codificada
        Usuario usuario = new Usuario(null, "luciana", passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);

        // login via AuthController
        LoginRequestDTO loginRequest = new LoginRequestDTO("luciana", "senha123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void deveObterUsuario() throws Exception {
        mockMvc.perform(get("/usuario")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("luciana"));
    }

    @Test
    void deveAtualizarSenha() throws Exception {
        mockMvc.perform(put("/usuario/senha")
                        .param("novaSenha", "novaSenha456")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("luciana"));
    }

    @Test
    void deveVerificarSeUsuarioExiste() throws Exception {
        mockMvc.perform(get("/usuario/existe")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void naoDevePermitirCriarSegundoUsuario() throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO("outroUser", "senha789");

        mockMvc.perform(post("/usuario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }
}

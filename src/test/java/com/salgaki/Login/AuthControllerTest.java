package com.salgaki.Login;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertNotEquals;


import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    void login_ComCredenciaisValidas_DeveRetornarTokenNoHeader() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_ComCredenciaisValidas_TokenDoHeaderDeveSerIgualAoBody() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String authorizationHeader = result.getResponse().getHeader("Authorization");
        assertNotNull(authorizationHeader);

        JsonNode responseJson = objectMapper.readTree(result.getResponse().getContentAsString());
        String bodyToken = responseJson.get("token").asText();
        String headerToken = authorizationHeader.substring("Bearer ".length());

        assertEquals(bodyToken, headerToken);
    }

    @Test
    void acesso_Protegido_SemToken_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/categorias"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acesso_Protegido_ComTokenInvalido_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/categorias")
                        .header("Authorization", "Bearer token_invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acesso_Protegido_ComTokenValido_DeveRetornar200() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        String tokenHeader = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andReturn()
                .getResponse()
                .getHeader("Authorization");

        mockMvc.perform(get("/categorias")
                        .header("Authorization", tokenHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());
    }

    @Test
    void acesso_Protegido_ComTokenValido_DevePermitirListarProdutos() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        String tokenHeader = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andReturn()
                .getResponse()
                .getHeader("Authorization");

        mockMvc.perform(get("/produtos")
                        .header("Authorization", tokenHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void login_RetornaUsernameEtokenNoBody() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");
        dto.setPassword("senha123");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("usuarioTeste"))
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
    void login_SemSenha_DeveRetornar401() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO();
        dto.setUsername("usuarioTeste");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_DeveRetornarMensagemSucesso() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Logout realizado com sucesso!"));
    }
}

package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.model.Usuario;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.ProdutoRepository;
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
class ProdutoControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Categoria categoriaSalgado;
    private Categoria categoriaDoce;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        categoriaSalgado = categoriaRepository.save(new Categoria(null, "Salgado"));
        categoriaDoce = categoriaRepository.save(new Categoria(null, "Doce"));

        // cria usuário para login
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
    void deveCriarProduto() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", 5.0, categoriaSalgado.getId());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Coxinha"))
                .andExpect(jsonPath("$.categoria.nome").value("Salgado"));
    }

    @Test
    void deveListarProdutos() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, categoriaSalgado));

        mockMvc.perform(get("/produtos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pastel"));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha G", 7.0, categoriaSalgado.getId());

        mockMvc.perform(put("/produtos/" + salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Coxinha G"))
                .andExpect(jsonPath("$.preco").value(7.0));
    }

    @Test
    void deveDeletarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(delete("/produtos/" + salvo.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveRetornarProdutoPorId() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(get("/produtos/" + salvo.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Coxinha"))
                .andExpect(jsonPath("$.preco").value(5.0));
    }

    @Test
    void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/produtos/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveBuscarPorNome() throws Exception {
        produtoRepository.save(new Produto(null, "Empada", 6.0, categoriaSalgado));

        mockMvc.perform(get("/produtos").param("nome", "emp")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Empada"));
    }

    @Test
    void deveFiltrarPorCategoria() throws Exception {
        produtoRepository.save(new Produto(null, "Brigadeiro", 3.0, categoriaDoce));

        mockMvc.perform(get("/produtos").param("categoriaId", categoriaDoce.getId().toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Brigadeiro"));
    }

    @Test
    void deveListarEmOrdemAlfabetica() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, categoriaSalgado));
        produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(get("/produtos").param("alfabetica", "true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Coxinha"))
                .andExpect(jsonPath("$[1].nome").value("Pastel"));
    }
}

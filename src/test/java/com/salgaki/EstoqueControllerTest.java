package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.EstoqueCreateDTO;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.model.Usuario;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.repository.UsuarioRepository;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ProdutoService produtoService;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private ObjectMapper objectMapper;

    private Produto produto;
    private String token;
    private LocalDate validade;

    @BeforeEach
    void setup() throws Exception {
        estoqueRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Categoria categoria = categoriaService.criar(new Categoria(null, "Bebidas"));

        // agora usamos ProdutoCreateDTO
        produto = produtoService.criar(
                new ProdutoCreateDTO("Suco de Laranja", 5.50, categoria.getId())
        );

        validade = LocalDate.now().plusDays(30);

        // cria usuário e login
        Usuario usuario = new Usuario();
        usuario.setUsername("luciana");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);

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
    void deveCriarEstoqueInicial() throws Exception {
        LocalDate validade = LocalDate.now().plusDays(30);
        EstoqueCreateDTO dto = new EstoqueCreateDTO(produto.getId(), 5, validade);

        mockMvc.perform(post("/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.produtoNome").value("Suco de Laranja"))
                .andExpect(jsonPath("$.quantidade").value(5))
                .andExpect(jsonPath("$.dataValidade").value(validade.toString()));
    }

    @Test
    void deveAdicionarEntradaDeEstoque() throws Exception {
        EstoqueCreateDTO dto = new EstoqueCreateDTO(produto.getId(), 0, validade);
        mockMvc.perform(post("/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/estoques/" + produto.getId() + "/entrada")
                        .param("quantidade", "10")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(10))
                .andExpect(jsonPath("$.dataValidade").value(validade.toString()));
    }

    @Test
    void deveRemoverSaidaDeEstoque() throws Exception {
        EstoqueCreateDTO dto = new EstoqueCreateDTO(produto.getId(), 10, validade);
        mockMvc.perform(post("/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(put("/estoques/" + produto.getId() + "/saida")
                        .param("quantidade", "5")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(5))
                .andExpect(jsonPath("$.dataValidade").value(validade.toString()));
    }

    @Test
    void deveConsultarEstoque() throws Exception {
        EstoqueCreateDTO dto = new EstoqueCreateDTO(produto.getId(), 8, validade);
        mockMvc.perform(post("/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/estoques/" + produto.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(8))
                .andExpect(jsonPath("$.dataValidade").value(validade.toString()));
    }

    @Test
    void deveListarEstoquesBaixos() throws Exception {
        EstoqueCreateDTO dto = new EstoqueCreateDTO(produto.getId(), 3, validade);
        mockMvc.perform(post("/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/estoques/baixo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantidade").value(3))
                .andExpect(jsonPath("$[0].dataValidade").value(validade.toString()));
    }
}
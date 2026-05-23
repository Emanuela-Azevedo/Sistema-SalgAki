package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Categoria categoriaSalgado;
    private Categoria categoriaDoce;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        categoriaSalgado = categoriaRepository.save(new Categoria(null, "Salgado"));
        categoriaDoce = categoriaRepository.save(new Categoria(null, "Doce"));
    }

    @Test
    @WithMockUser
    void deveCriarProduto() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", 5.0, categoriaSalgado.getId());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Coxinha"))
                .andExpect(jsonPath("$.categoria.nome").value("Salgado"));
    }

    @Test
    @WithMockUser
    void deveListarProdutos() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, categoriaSalgado));

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pastel"));
    }

    @Test
    @WithMockUser
    void deveBuscarPorNome() throws Exception {
        produtoRepository.save(new Produto(null, "Empada", 6.0, categoriaSalgado));

        mockMvc.perform(get("/produtos").param("nome", "emp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Empada"));
    }

    @Test
    @WithMockUser
    void deveFiltrarPorCategoria() throws Exception {
        produtoRepository.save(new Produto(null, "Brigadeiro", 3.0, categoriaDoce));

        mockMvc.perform(get("/produtos").param("categoriaId", categoriaDoce.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Brigadeiro"));
    }

    @Test
    @WithMockUser
    void deveAtualizarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha G", 7.0, categoriaSalgado.getId());

        mockMvc.perform(put("/produtos/" + salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Coxinha G"));
    }

    @Test
    @WithMockUser
    void deveDeletarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(delete("/produtos/" + salvo.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deveListarEmOrdemAlfabetica() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, categoriaSalgado));
        produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(get("/produtos").param("alfabetica", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Coxinha"))
                .andExpect(jsonPath("$[1].nome").value("Pastel"));
    }

    @Test
    @WithMockUser
    void deveRetornarProdutoPorId() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoriaSalgado));

        mockMvc.perform(get("/produtos/" + salvo.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Coxinha"))
                .andExpect(jsonPath("$.preco").value(5.0));
    }

    @Test
    @WithMockUser
    void deveRetornar404QuandoProdutoNaoEncontrado() throws Exception {
        mockMvc.perform(get("/produtos/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void deveRetornar400AoCriarProdutoSemNome() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("", 5.0, categoriaSalgado.getId());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deveRetornar400AoCriarProdutoComPrecoNegativo() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", -1.0, categoriaSalgado.getId());

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deveRetornar400AoCriarProdutoSemCategoria() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", 5.0, null);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void deveRetornar404AoCriarProdutoComCategoriaInexistente() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", 5.0, 999L);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }
}
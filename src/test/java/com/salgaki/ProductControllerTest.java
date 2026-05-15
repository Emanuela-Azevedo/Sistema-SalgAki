package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.model.Produto;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void deveCriarProduto() throws Exception {
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha", 5.0, 10, "Salgado");

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Coxinha"))
                .andExpect(jsonPath("$.categoria").value("Salgado"));
    }

    @Test
    @WithMockUser
    void deveListarProdutos() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, 20, "Salgado"));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Pastel"));
    }

    @Test
    @WithMockUser
    void deveBuscarPorNome() throws Exception {
        produtoRepository.save(new Produto(null, "Empada", 6.0, 15, "Salgado"));

        mockMvc.perform(get("/products").param("nome", "emp"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Empada"));
    }

    @Test
    @WithMockUser
    void deveFiltrarPorCategoria() throws Exception {
        produtoRepository.save(new Produto(null, "Brigadeiro", 3.0, 30, "Doce"));

        mockMvc.perform(get("/products").param("categoria", "Doce"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Brigadeiro"));
    }

    @Test
    @WithMockUser
    void deveAtualizarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, 10, "Salgado"));
        ProdutoCreateDTO dto = new ProdutoCreateDTO("Coxinha G", 7.0, 5, "Salgado");

        mockMvc.perform(put("/products/" + salvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Coxinha G"));
    }

    @Test
    @WithMockUser
    void deveDeletarProduto() throws Exception {
        Produto salvo = produtoRepository.save(new Produto(null, "Coxinha", 5.0, 10, "Salgado"));

        mockMvc.perform(delete("/products/" + salvo.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void deveListarEmOrdemAlfabetica() throws Exception {
        produtoRepository.save(new Produto(null, "Pastel", 4.0, 20, "Salgado"));
        produtoRepository.save(new Produto(null, "Coxinha", 5.0, 10, "Salgado"));

        mockMvc.perform(get("/products").param("alfabetica", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Coxinha"))
                .andExpect(jsonPath("$[1].nome").value("Pastel"));
    }
}

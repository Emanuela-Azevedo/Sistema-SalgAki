package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.EstoqueCreateDTO;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.model.Usuario;
import com.salgaki.repository.*;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.EstoqueService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CardapioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private EstoqueRepository estoqueRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    private Produto produtoComEstoque;
    private Produto produtoSemEstoque;
    private String token;
    private LocalDate validade;

    @BeforeEach
    void setup() throws Exception {

        movimentacaoRepository.deleteAll();
        estoqueRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Categoria categoria =
                categoriaService.criar(new Categoria(null, "Bebidas"));

        validade = LocalDate.now().plusDays(30);

        produtoComEstoque = produtoService.criar(
                new Produto(null,
                        "Suco de Laranja",
                        5.50,
                        categoria,
                        null)
        );

        estoqueService.criarEstoque(
                new EstoqueCreateDTO(
                        produtoComEstoque.getId(),
                        10,
                        validade
                )
        );

        produtoSemEstoque = produtoService.criar(
                new Produto(null,
                        "Refrigerante Cola",
                        6.00,
                        categoria,
                        null)
        );

        estoqueService.criarEstoque(
                new EstoqueCreateDTO(
                        produtoSemEstoque.getId(),
                        0,
                        validade
                )
        );

        Usuario usuario = new Usuario();
        usuario.setUsername("luciana");
        usuario.setPassword(passwordEncoder.encode("senha123"));
        usuarioRepository.save(usuario);

        LoginRequestDTO loginRequest =
                new LoginRequestDTO("luciana", "senha123");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = objectMapper.readTree(response)
                .get("token")
                .asText();
    }

    @Test
    void deveGerarTextoCardapioApenasComProdutosDisponiveis() throws Exception {

        String texto = mockMvc.perform(get("/cardapio/texto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(texto).contains("Cardápio SalgAki");
        assertThat(texto).contains("Suco de Laranja");
        assertThat(texto).contains("Preço: R$");
        assertThat(texto).contains("Disponível: 10 unidade(s)");

        assertThat(texto).doesNotContain("Refrigerante Cola");
    }

    @Test
    void deveMostrarMensagemQuandoNaoHaProdutosDisponiveis() throws Exception {

        estoqueService.removerEstoque(produtoComEstoque.getId(), 10);

        String texto = mockMvc.perform(get("/cardapio/texto")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(texto)
                .contains("Nenhum produto disponível no momento.");

        assertThat(texto)
                .doesNotContain("Suco de Laranja");

        assertThat(texto)
                .doesNotContain("Refrigerante Cola");
    }
}
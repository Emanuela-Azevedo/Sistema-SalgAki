package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Produto;
import com.salgaki.model.Usuario;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.repository.UsuarioRepository;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.service.CategoriaService;
import com.salgaki.service.ProdutoService;
import com.salgaki.service.EstoqueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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

    private Produto produtoComEstoque;
    private Produto produtoSemEstoque;
    private String token;
    private LocalDate validade;

    @BeforeEach
    void setup() throws Exception {
        estoqueRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Categoria categoria = categoriaService.criar(new Categoria(null, "Bebidas"));
        validade = LocalDate.now().plusDays(30);

        produtoComEstoque = produtoService.criar(new Produto(null, "Suco de Laranja", 5.50, categoria, null));
        estoqueService.criarEstoque(produtoComEstoque, validade);
        estoqueService.adicionarEstoque(
                produtoComEstoque.getId(),
                10,
                LocalDate.of(2026, 10, 25)
        );
        produtoSemEstoque = produtoService.criar(new Produto(null, "Refrigerante Cola", 6.00, categoria, null));
        estoqueService.criarEstoque(produtoSemEstoque, validade);
        // não adiciona quantidade → estoque fica 0

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
    void deveGerarCardapioPdfApenasComProdutosDisponiveis() throws Exception {
        byte[] pdfBytes = mockMvc.perform(get("/cardapio/pdf")
                        .accept(MediaType.APPLICATION_PDF)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        // extrai texto do PDF
        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String pdfText = stripper.getText(document);

            // só o produto com estoque > 0 deve aparecer
            assertThat(pdfText).contains("Suco de Laranja");
            assertThat(pdfText).doesNotContain("Refrigerante Cola"); // estoque 0 não aparece
        }
    }

    @Test
    void deveMostrarMensagemQuandoNaoHaProdutosDisponiveis() throws Exception {
        // zera o estoque do produto disponível
        estoqueService.removerEstoque(produtoComEstoque.getId(), 10);

        byte[] pdfBytes = mockMvc.perform(get("/cardapio/pdf")
                        .accept(MediaType.APPLICATION_PDF)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String pdfText = stripper.getText(document);

            // mensagem padrão quando não há produtos disponíveis
            assertThat(pdfText).contains("Nenhum produto disponível no momento.");
            assertThat(pdfText).doesNotContain("Suco de Laranja");
            assertThat(pdfText).doesNotContain("Refrigerante Cola");
        }
    }
}
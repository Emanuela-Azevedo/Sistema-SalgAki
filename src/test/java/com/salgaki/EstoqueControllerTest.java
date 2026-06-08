package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.LoginRequestDTO;
import com.salgaki.dto.MovimentacaoCreateDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Estoque;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.Produto;
import com.salgaki.model.TipoMovimentacao;
import com.salgaki.model.Usuario;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.EstoqueRepository;
import com.salgaki.repository.MovimentacaoRepository;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstoqueControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProdutoRepository produtoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private EstoqueRepository estoqueRepository;
    @Autowired private MovimentacaoRepository movimentacaoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Produto produto;
    private Estoque estoque;
    private String token;

    @BeforeEach
    void setUp() throws Exception {
        movimentacaoRepository.deleteAll();
        estoqueRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();
        usuarioRepository.deleteAll();

        Categoria categoria = categoriaRepository.save(new Categoria(null, "Salgado"));
        produto = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoria));
        estoque = estoqueRepository.save(new Estoque(null, produto, 0, LocalDateTime.now().toLocalDate(), null));

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
    void deveRegistrarEntradaERetornarSaldoAtualizado() throws Exception {
        mockMvc.perform(put("/estoques/" + produto.getId() + "/entrada")
                        .header("Authorization", "Bearer " + token)
                        .param("quantidade", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(20));
    }

    @Test
    void deveRegistrarSaidaERetornarSaldoAtualizado() throws Exception {
        // entrada de 10
        mockMvc.perform(put("/estoques/" + produto.getId() + "/entrada")
                .header("Authorization", "Bearer " + token)
                .param("quantidade", "10"));

        // saída de 3
        mockMvc.perform(put("/estoques/" + produto.getId() + "/saida")
                        .header("Authorization", "Bearer " + token)
                        .param("quantidade", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(7));
    }

    @Test
    void deveFalharSaidaComEstoqueInsuficiente() throws Exception {
        mockMvc.perform(put("/estoques/" + produto.getId() + "/saida")
                        .header("Authorization", "Bearer " + token)
                        .param("quantidade", "10"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveRetornarProdutosComEstoqueBaixo() throws Exception {
        // saldo = 0, abaixo do limite de 5
        mockMvc.perform(get("/estoques/baixo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].produtoNome").value("Coxinha"));
    }

    @Test
    void deveRetornarRelatorioComTotaisCorretos() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(null, estoque, 30, TipoMovimentacao.ENTRADA, agora.minusHours(2));
        MovimentacaoEstoque saida = new MovimentacaoEstoque(null, estoque, 10, TipoMovimentacao.SAIDA, agora.minusHours(1));

        movimentacaoRepository.save(entrada);
        movimentacaoRepository.save(saida);

        mockMvc.perform(get("/movimentacoes/" + produto.getId() + "/relatorio")
                        .header("Authorization", "Bearer " + token)
                        .param("de", agora.minusDays(1).toString())
                        .param("ate", agora.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoNome").value("Coxinha"))
                .andExpect(jsonPath("$.totalEntradas").value(30))
                .andExpect(jsonPath("$.totalSaidas").value(10))
                .andExpect(jsonPath("$.saldoPeriodo").value(20))
                .andExpect(jsonPath("$.movimentacoes.length()").value(2));
    }

    @Test
    void deveConsultarEstoqueAtual() throws Exception {
        mockMvc.perform(get("/estoques/" + produto.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produtoNome").value("Coxinha"))
                .andExpect(jsonPath("$.quantidade").value(0));
    }

    @Test
    void deveListarMovimentacoesDetalhadas() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(null, estoque, 5, TipoMovimentacao.ENTRADA, agora.minusHours(3));
        MovimentacaoEstoque saida = new MovimentacaoEstoque(null, estoque, 2, TipoMovimentacao.SAIDA, agora.minusHours(2));

        movimentacaoRepository.save(entrada);
        movimentacaoRepository.save(saida);

        mockMvc.perform(get("/movimentacoes/" + produto.getId() + "/detalhes")
                        .header("Authorization", "Bearer " + token)
                        .param("de", agora.minusDays(1).toString())
                        .param("ate", agora.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].produtoNome").value("Coxinha"));
    }
}

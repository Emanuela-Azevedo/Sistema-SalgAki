package com.salgaki;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.salgaki.dto.MovimentacaoDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.MovimentacaoEstoque.TipoMovimentacao;
import com.salgaki.model.Produto;
import com.salgaki.repository.CategoriaRepository;
import com.salgaki.repository.MovimentacaoEstoqueRepository;
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
    @Autowired private MovimentacaoEstoqueRepository movimentacaoRepository;

    private Produto produto;

    @BeforeEach
    void setUp() {
        movimentacaoRepository.deleteAll();
        produtoRepository.deleteAll();
        categoriaRepository.deleteAll();

        Categoria categoria = categoriaRepository.save(new Categoria(null, "Salgado"));
        produto = produtoRepository.save(new Produto(null, "Coxinha", 5.0, categoria));
    }

    @Test
    @WithMockUser
    void deveRegistrarEntradaERetornarSaldoAtualizado() throws Exception {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setTipo(TipoMovimentacao.ENTRADA);
        dto.setQuantidade(20);

        mockMvc.perform(patch("/produtos/" + produto.getId() + "/estoque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(20));
    }

    @Test
    @WithMockUser
    void deveRegistrarSaidaERetornarSaldoAtualizado() throws Exception {
        // entrada de 10
        MovimentacaoDTO entrada = new MovimentacaoDTO();
        entrada.setTipo(TipoMovimentacao.ENTRADA);
        entrada.setQuantidade(10);
        mockMvc.perform(patch("/produtos/" + produto.getId() + "/estoque")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entrada)));

        // saída de 3
        MovimentacaoDTO saida = new MovimentacaoDTO();
        saida.setTipo(TipoMovimentacao.SAIDA);
        saida.setQuantidade(3);

        mockMvc.perform(patch("/produtos/" + produto.getId() + "/estoque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saida)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidade").value(7));
    }

    @Test
    @WithMockUser
    void deveFalharSaidaComEstoqueInsuficiente() throws Exception {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setTipo(TipoMovimentacao.SAIDA);
        dto.setQuantidade(10);

        mockMvc.perform(patch("/produtos/" + produto.getId() + "/estoque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser
    void deveRetornarProdutosComEstoqueBaixo() throws Exception {
        // saldo = 0, abaixo do limite de 5
        mockMvc.perform(get("/produtos/estoque-baixo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Coxinha"));
    }

    @Test
    @WithMockUser
    void naoDeveRetornarProdutoComEstoqueAdequado() throws Exception {
        MovimentacaoDTO dto = new MovimentacaoDTO();
        dto.setTipo(TipoMovimentacao.ENTRADA);
        dto.setQuantidade(10);
        mockMvc.perform(patch("/produtos/" + produto.getId() + "/estoque")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        mockMvc.perform(get("/produtos/estoque-baixo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser
    void deveRetornarRelatorioComTotaisCorretos() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(null, produto, TipoMovimentacao.ENTRADA, 30, agora.minusHours(2));
        MovimentacaoEstoque saida = new MovimentacaoEstoque(null, produto, TipoMovimentacao.SAIDA, 10, agora.minusHours(1));
        movimentacaoRepository.save(entrada);
        movimentacaoRepository.save(saida);

        mockMvc.perform(get("/produtos/" + produto.getId() + "/movimentacoes")
                        .param("de", agora.minusDays(1).toString())
                        .param("ate", agora.plusDays(1).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.produto").value("Coxinha"))
                .andExpect(jsonPath("$.totalEntradas").value(30))
                .andExpect(jsonPath("$.totalSaidas").value(10))
                .andExpect(jsonPath("$.saldoPeriodo").value(20))
                .andExpect(jsonPath("$.movimentacoes.length()").value(2));
    }

    @Test
    @WithMockUser
    void deveRetornarRelatorioVazioForaDoPeriodo() throws Exception {
        LocalDateTime agora = LocalDateTime.now();

        MovimentacaoEstoque entrada = new MovimentacaoEstoque(null, produto, TipoMovimentacao.ENTRADA, 30, agora.minusDays(10));
        movimentacaoRepository.save(entrada);

        mockMvc.perform(get("/produtos/" + produto.getId() + "/movimentacoes")
                        .param("de", agora.minusDays(3).toString())
                        .param("ate", agora.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEntradas").value(0))
                .andExpect(jsonPath("$.totalSaidas").value(0))
                .andExpect(jsonPath("$.movimentacoes").isEmpty());
    }
}

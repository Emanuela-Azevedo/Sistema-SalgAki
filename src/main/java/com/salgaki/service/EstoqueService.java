package com.salgaki.service;

import com.salgaki.dto.RelatorioMovimentacaoDTO;
import com.salgaki.dto.RelatorioMovimentacaoDTO.ItemMovimentacaoDTO;
import com.salgaki.model.MovimentacaoEstoque;
import com.salgaki.model.MovimentacaoEstoque.TipoMovimentacao;
import com.salgaki.model.Produto;
import com.salgaki.repository.MovimentacaoEstoqueRepository;
import com.salgaki.repository.ProdutoRepository;
import com.salgaki.service.exception.EstoqueInsuficienteException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private static final int LIMITE_ESTOQUE_BAIXO = 5;

    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final ProdutoService produtoService;

    @Transactional
    public int movimentar(Long produtoId, TipoMovimentacao tipo, Integer quantidade) {
        Produto produto = produtoService.buscarPorId(produtoId);

        if (tipo == TipoMovimentacao.SAIDA) {
            int saldoAtual = calcularSaldo(produtoId);
            if (saldoAtual < quantidade) {
                throw new EstoqueInsuficienteException(saldoAtual);
            }
        }

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setProduto(produto);
        mov.setTipo(tipo);
        mov.setQuantidade(quantidade);
        mov.setDataHora(LocalDateTime.now());
        movimentacaoRepository.save(mov);

        return calcularSaldo(produtoId);
    }

    @Transactional(readOnly = true)
    public int calcularSaldo(Long produtoId) {
        return movimentacaoRepository.calcularSaldo(produtoId);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarEstoqueBaixo() {
        return produtoRepository.findAll().stream()
                .filter(p -> calcularSaldo(p.getId()) <= LIMITE_ESTOQUE_BAIXO)
                .toList();
    }

    @Transactional(readOnly = true)
    public RelatorioMovimentacaoDTO gerarRelatorio(Long produtoId, LocalDateTime de, LocalDateTime ate) {
        Produto produto = produtoService.buscarPorId(produtoId);

        List<MovimentacaoEstoque> movs = movimentacaoRepository
                .findByProdutoIdAndDataHoraBetweenOrderByDataHoraAsc(produtoId, de, ate);

        int totalEntradas = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.ENTRADA)
                .mapToInt(MovimentacaoEstoque::getQuantidade).sum();

        int totalSaidas = movs.stream()
                .filter(m -> m.getTipo() == TipoMovimentacao.SAIDA)
                .mapToInt(MovimentacaoEstoque::getQuantidade).sum();

        List<ItemMovimentacaoDTO> itens = movs.stream()
                .map(m -> new ItemMovimentacaoDTO(m.getTipo().name(), m.getQuantidade(), m.getDataHora()))
                .toList();

        return new RelatorioMovimentacaoDTO(produto.getNome(), totalEntradas, totalSaidas, totalEntradas - totalSaidas, itens);
    }
}

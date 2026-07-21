package com.salgaki.service;

import com.salgaki.model.Estoque;
import com.salgaki.repository.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final EstoqueRepository estoqueRepository;

    public String gerarTextoCardapio() {
        List<Estoque> disponiveis = estoqueRepository.findByQuantidadeGreaterThan(0);

        StringBuilder sb = new StringBuilder();
        sb.append("*Cardápio SalgAki*\n\n");

        if (disponiveis.isEmpty()) {
            sb.append("Nenhum produto disponível no momento.");
        } else {

            Map<Long, Integer> quantidadePorProduto = disponiveis.stream()
                    .collect(Collectors.groupingBy(
                            e -> e.getProduto().getId(),
                            Collectors.summingInt(Estoque::getQuantidade)
                    ));

            Map<Long, Estoque> produtos = disponiveis.stream()
                    .collect(Collectors.toMap(
                            e -> e.getProduto().getId(),
                            e -> e,
                            (existente, novo) -> existente
                    ));

            for (Long produtoId : quantidadePorProduto.keySet()) {

                Estoque estoque = produtos.get(produtoId);

                sb.append("• ")
                        .append(estoque.getProduto().getNome())
                        .append("\n")
                        .append("Preço: R$ ")
                        .append(String.format("%.2f", estoque.getProduto().getPreco()))
                        .append("\n")
                        .append("Disponível: ")
                        .append(quantidadePorProduto.get(produtoId))
                        .append(" unidade(s)\n\n");
            }
        }

        sb.append("Aguardamos seu pedido!");

        return sb.toString();
    }
}

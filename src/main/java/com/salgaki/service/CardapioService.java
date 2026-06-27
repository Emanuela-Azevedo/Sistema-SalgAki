package com.salgaki.service;

import com.salgaki.model.Estoque;
import com.salgaki.repository.EstoqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

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
            for (Estoque e : disponiveis) {
                sb.append("• ")
                        .append(e.getProduto().getNome())
                        .append("\n")
                        .append("Preço: R$ ")
                        .append(String.format("%.2f", e.getProduto().getPreco()))
                        .append("\n")
                        .append("Disponível: ")
                        .append(e.getQuantidade())
                        .append(" unidade(s)\n\n");
            }
        }

        sb.append("Aguardamos seu pedido!");

        return sb.toString();
    }
}

package com.salgaki.dto;

import com.salgaki.model.Estoque;
import com.salgaki.model.Produto;

public record CardapioDTO(String produtoNome, Double preco, Integer quantidade) {

    public static CardapioDTO fromProduto(Produto produto) {

        int quantidadeTotal = produto.getEstoques() == null
                ? 0
                : produto.getEstoques()
                .stream()
                .mapToInt(Estoque::getQuantidade)
                .sum();

        return new CardapioDTO(
                produto.getNome(),
                produto.getPreco(),
                quantidadeTotal
        );
    }
}
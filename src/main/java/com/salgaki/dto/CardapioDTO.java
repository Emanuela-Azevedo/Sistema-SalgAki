package com.salgaki.dto;

import com.salgaki.model.Produto;

public record CardapioDTO(String produtoNome, Double preco, Integer quantidade) {
    public static CardapioDTO fromProduto(Produto produto) {
        return new CardapioDTO(
                produto.getNome(),
                produto.getPreco(),
                produto.getEstoque() != null ? produto.getEstoque().getQuantidade() : 0
        );
    }
}

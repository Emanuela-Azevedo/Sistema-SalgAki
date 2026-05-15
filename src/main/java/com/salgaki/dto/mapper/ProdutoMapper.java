package com.salgaki.dto.mapper;

import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.ProdutoResponseDTO;
import com.salgaki.model.Produto;

public class ProdutoMapper {

    public static Produto toProduto(ProdutoCreateDTO dto) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setPreco(dto.getPreco());
        produto.setQuantidade(dto.getQuantidade());
        produto.setCategoria(dto.getCategoria());
        return produto;
    }

    public static ProdutoResponseDTO toDto(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                produto.getQuantidade(),
                produto.getCategoria()
        );
    }
}

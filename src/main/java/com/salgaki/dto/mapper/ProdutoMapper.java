package com.salgaki.dto.mapper;

import com.salgaki.dto.ProdutoCreateDTO;
import com.salgaki.dto.ProdutoResponseDTO;
import com.salgaki.model.Categoria;
import com.salgaki.model.Estoque;
import com.salgaki.model.Produto;

public class ProdutoMapper {

    public static Produto toProduto(ProdutoCreateDTO dto, Categoria categoria) {
        Produto produto = new Produto();
        produto.setNome(dto.getNome());
        produto.setPreco(dto.getPreco());
        produto.setCategoria(categoria);
        return produto;
    }

    public static ProdutoResponseDTO toDto(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getPreco(),
                CategoriaMapper.toDto(produto.getCategoria())
        );
    }
}
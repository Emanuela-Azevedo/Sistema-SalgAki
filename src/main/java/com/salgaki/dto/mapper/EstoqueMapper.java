package com.salgaki.dto.mapper;

import com.salgaki.dto.EstoqueResponseDTO;
import com.salgaki.model.Estoque;

import java.util.List;

public class EstoqueMapper {

    public static EstoqueResponseDTO toDto(Estoque estoque) {

        if (estoque == null) {
            return null;
        }

        return new EstoqueResponseDTO(
                estoque.getId(),
                estoque.getProduto().getNome(),
                estoque.getQuantidade(),
                estoque.getDataValidade() != null
                        ? estoque.getDataValidade()
                        : null
        );
    }

    public static List<EstoqueResponseDTO> toDtoList(List<Estoque> estoques) {
        return estoques.stream()
                .map(EstoqueMapper::toDto)
                .toList();
    }
}

package com.salgaki.dto.mapper;

import com.salgaki.dto.CategoriaCreateDTO;
import com.salgaki.dto.CategoriaResponseDTO;
import com.salgaki.model.Categoria;

import java.util.List;

public class CategoriaMapper {

    public static Categoria toCategoria(CategoriaCreateDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        return categoria;
    }

    public static CategoriaResponseDTO toDto(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome());
    }

    public static List<CategoriaResponseDTO> toDtoList(List<Categoria> categorias) {
        return categorias.stream()
                .map(CategoriaMapper::toDto)
                .toList();
    }
}

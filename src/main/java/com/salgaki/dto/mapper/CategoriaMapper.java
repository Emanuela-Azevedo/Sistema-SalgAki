package com.salgaki.dto.mapper;

import com.salgaki.dto.CategoriaCreateDTO;
import com.salgaki.dto.CategoriaResponseDTO;
import com.salgaki.model.Categoria;

public class CategoriaMapper {

    public static Categoria toCategoria(CategoriaCreateDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.getNome());
        return categoria;
    }

    public static CategoriaResponseDTO toDto(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNome());
    }
}

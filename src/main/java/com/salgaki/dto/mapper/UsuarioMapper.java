package com.salgaki.dto.mapper;

import com.salgaki.dto.UsuarioCreateDTO;
import com.salgaki.dto.UsuarioResponseDTO;
import com.salgaki.model.Usuario;

public class UsuarioMapper {

    // DTO → Entity
    public static Usuario toEntity(UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword());
        return usuario;
    }

    // Entity → ResponseDTO
    public static UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        return dto;
    }

    public static void updateEntityFromDTO(UsuarioCreateDTO dto, Usuario usuario) {
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword());
    }
}
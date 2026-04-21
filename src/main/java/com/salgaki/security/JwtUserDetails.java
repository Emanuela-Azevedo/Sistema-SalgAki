package com.salgaki.security;

import com.salgaki.model.Usuario;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

public class JwtUserDetails extends User {

    private final Usuario usuario;

    public JwtUserDetails(Usuario usuario) {
        super(
                usuario.getUsername(),
                usuario.getPassword(),
                Collections.emptyList()
        );
        this.usuario = usuario;
    }

    public Long getId() {
        return this.usuario.getId();
    }

    public String getNome() {
        return this.usuario.getUsername();
    }
}
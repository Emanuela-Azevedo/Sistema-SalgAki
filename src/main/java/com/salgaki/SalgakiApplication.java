package com.salgaki;

import com.salgaki.model.Usuario;
import com.salgaki.repository.UsuarioRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SalgakiApplication {
	public static void main(String[] args) {
		ApplicationContext ctx = SpringApplication.run(SalgakiApplication.class, args);

		UsuarioRepository usuarioRepository = ctx.getBean(UsuarioRepository.class);
		PasswordEncoder passwordEncoder = ctx.getBean(PasswordEncoder.class);

		// Cria usuário com senha encriptada
		Usuario usuario = new Usuario();
		usuario.setUsername("luciana");
		usuario.setPassword(passwordEncoder.encode("123456"));

		usuarioRepository.save(usuario);

		System.out.println("Usuário criado com sucesso: " + usuario.getUsername());
	}
}

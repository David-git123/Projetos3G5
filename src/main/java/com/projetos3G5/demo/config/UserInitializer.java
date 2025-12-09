package com.projetos3G5.demo.config;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private RepoCadastro repoCadastro;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Super usuA�rio com papel ADMINISTRADOR
        ensureUser("super@care.com", "Super Admin", "super123", "ADMINISTRADOR");

        // UsuA�rio cliente padrA�o para testes (reclamado no login)
        ensureUser("joao@gmail.com", "Joao", "123", "CLIENTE");
    }

    private void ensureUser(String email, String nome, String rawPassword, String tipoAcesso) {
        Pessoa usuario = repoCadastro.findByEmail(email);
        if (usuario == null) {
            usuario = new Pessoa();
            usuario.setEmail(email);
            usuario.setNome(nome);
        } else {
            if (usuario.getNome() == null || usuario.getNome().isBlank()) {
                usuario.setNome(nome);
            }
        }
        usuario.setSenha(passwordEncoder.encode(rawPassword));
        usuario.setTipoAcesso(tipoAcesso);
        repoCadastro.save(usuario);
    }
}

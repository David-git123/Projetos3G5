package com.projetos3G5.demo.service;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CadastroService {

    @Autowired
    private RepoCadastro repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean salvarUsuario(Pessoa pessoa){
        if(pessoa.getNome() == null || pessoa.getEmail() == null || pessoa.getSenha() == null || pessoa.getTipoAcesso() == null){
            return false;
        }

        // Atualiza se ja existir e for o mesmo registro
        Pessoa existente = repo.findByEmail(pessoa.getEmail());
        if (existente != null && (pessoa.getId() == null || !existente.getId().equals(pessoa.getId()))) {
            return false;
        }

        boolean senhaJaHash = pessoa.getSenha().startsWith("$2a$") || pessoa.getSenha().startsWith("$2b$");
        String senhaHash = senhaJaHash ? pessoa.getSenha() : passwordEncoder.encode(pessoa.getSenha());
        pessoa.setSenha(senhaHash);
        repo.save(pessoa);
        return true;
    }

    public boolean autenticarUsuario(String email, String senha){
        Pessoa pessoa = repo.findByEmail(email);
        if(pessoa != null){
            // Comparar senha com hash
            return passwordEncoder.matches(senha, pessoa.getSenha());
        }
        else{
            return false;
        }
    }
}

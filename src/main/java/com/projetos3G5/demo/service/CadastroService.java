package com.projetos3G5.demo.service;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CadastroService {

    @Autowired
    private RepoCadastro repo;

    public boolean salvarUsuario(Pessoa pessoa){
        if(pessoa.getNome() == null || pessoa.getEmail() == null || pessoa.getSenha() == null || pessoa.getTipoAcesso() == null){
            return false;
        }
        else{
            if(repo.existsByEmail(pessoa.getEmail())){
                return false;
            }
            else{
                repo.save(pessoa);
                return true;
            }
        }
    }

    public boolean autenticarUsuario(String email, String senha){
        Pessoa pessoa = repo.findByEmail(email);
        if(pessoa != null){
            if(pessoa.getSenha().equals(senha)){
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}

package com.projetos3G5.demo.repositories;

import com.projetos3G5.demo.entities.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepoCadastro extends JpaRepository<Pessoa, Long>{
    public boolean existsByEmail(String email);
    public Pessoa  findByEmail(String email);
}

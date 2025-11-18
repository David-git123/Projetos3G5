package com.projetos3G5.demo.repositories;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepoFormulario extends JpaRepository<Formulario, Long> {
    
    List<Formulario> findByCriador(Pessoa criador);
    
    List<Formulario> findByCriadorAndStatus(Pessoa criador, String status);
    
    List<Formulario> findByStatus(String status);
    
    Optional<Formulario> findByIdAndCriador(Long id, Pessoa criador);
}


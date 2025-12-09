package com.projetos3G5.demo.repositories;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.RespostaFormulario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoRespostaFormulario extends JpaRepository<RespostaFormulario, Long> {
    List<RespostaFormulario> findByFormulario(Formulario formulario);
    void deleteByFormulario(Formulario formulario);
}

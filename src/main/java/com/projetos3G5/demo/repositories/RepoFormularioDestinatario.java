package com.projetos3G5.demo.repositories;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.FormularioDestinatario;
import com.projetos3G5.demo.entities.Pessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepoFormularioDestinatario extends JpaRepository<FormularioDestinatario, Long> {
    List<FormularioDestinatario> findByFormulario(Formulario formulario);
    List<FormularioDestinatario> findByDestinatario(Pessoa destinatario);
    boolean existsByFormularioAndDestinatario(Formulario formulario, Pessoa destinatario);
    void deleteByFormulario(Formulario formulario);
}

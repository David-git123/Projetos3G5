package com.projetos3G5.demo.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class FormularioDestinatario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;

    @ManyToOne
    @JoinColumn(name = "destinatario_id")
    private Pessoa destinatario;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public FormularioDestinatario() {}

    public FormularioDestinatario(Formulario formulario, Pessoa destinatario) {
        this.formulario = formulario;
        this.destinatario = destinatario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Formulario getFormulario() {
        return formulario;
    }

    public void setFormulario(Formulario formulario) {
        this.formulario = formulario;
    }

    public Pessoa getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Pessoa destinatario) {
        this.destinatario = destinatario;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}

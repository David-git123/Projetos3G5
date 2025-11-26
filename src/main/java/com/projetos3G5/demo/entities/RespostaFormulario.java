package com.projetos3G5.demo.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class RespostaFormulario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "formulario_id")
    private Formulario formulario;

    @ManyToOne
    @JoinColumn(name = "respondente_id")
    private Pessoa respondente;

    private String respondenteEmail;

    @Column(columnDefinition = "TEXT")
    private String respostasJson;

    private LocalDateTime dataCriacao = LocalDateTime.now();

    public RespostaFormulario() {}

    public RespostaFormulario(Formulario formulario, Pessoa respondente, String respondenteEmail, String respostasJson) {
        this.formulario = formulario;
        this.respondente = respondente;
        this.respondenteEmail = respondenteEmail;
        this.respostasJson = respostasJson;
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

    public Pessoa getRespondente() {
        return respondente;
    }

    public void setRespondente(Pessoa respondente) {
        this.respondente = respondente;
    }

    public String getRespondenteEmail() {
        return respondenteEmail;
    }

    public void setRespondenteEmail(String respondenteEmail) {
        this.respondenteEmail = respondenteEmail;
    }

    public String getRespostasJson() {
        return respostasJson;
    }

    public void setRespostasJson(String respostasJson) {
        this.respostasJson = respostasJson;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
}

package com.projetos3G5.demo.entities;

import jakarta.persistence.*;

@Entity
public class Pergunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "formulario_id", nullable = false)
    private Formulario formulario;
    
    private String texto;
    
    @Column(name = "tipo_pergunta")
    private String tipoPergunta; // TEXTO, MULTIPLA_ESCOLHA, ESCALA, DATA, etc.
    
    @Column(name = "obrigatoria")
    private boolean obrigatoria;
    
    @Column(name = "ordem")
    private Integer ordem;
    
    @Column(name = "opcoes", length = 1000)
    private String opcoes; // Para perguntas de múltipla escolha, armazenar as opções separadas por vírgula

    public Pergunta() {
    }

    public Pergunta(Formulario formulario, String texto, String tipoPergunta, boolean obrigatoria, Integer ordem) {
        this.formulario = formulario;
        this.texto = texto;
        this.tipoPergunta = tipoPergunta;
        this.obrigatoria = obrigatoria;
        this.ordem = ordem;
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getTipoPergunta() {
        return tipoPergunta;
    }

    public void setTipoPergunta(String tipoPergunta) {
        this.tipoPergunta = tipoPergunta;
    }

    public boolean isObrigatoria() {
        return obrigatoria;
    }

    public void setObrigatoria(boolean obrigatoria) {
        this.obrigatoria = obrigatoria;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public String getOpcoes() {
        return opcoes;
    }

    public void setOpcoes(String opcoes) {
        this.opcoes = opcoes;
    }
}


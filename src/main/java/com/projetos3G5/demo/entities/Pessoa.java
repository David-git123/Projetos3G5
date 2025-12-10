package com.projetos3G5.demo.entities;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

@Entity
public class Pessoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String email;
    private String senha;
    private String tipoAcesso;
    private String empresaNome;

    public Pessoa(){;}

    public Pessoa(String nome, String email, String senha, String tipoAcesso){
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.tipoAcesso = tipoAcesso;
    }

    public void setNome(String nome) { this.nome = nome; }
    public void setEmail(String email) { this.email = email; }
    public void setSenha(String senha) { this.senha = senha; }
    public void setTipoAcesso(String tipoAcesso) { this.tipoAcesso = tipoAcesso; }
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getSenha() { return senha; }
    public String getTipoAcesso() { return tipoAcesso; }
    public String getEmpresaNome() { return empresaNome; }
}

package com.projetos3G5.demo.service;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.entities.Pergunta;
import com.projetos3G5.demo.repositories.RepoCadastro;
import com.projetos3G5.demo.repositories.RepoFormulario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FormularioService {

    @Autowired
    private RepoFormulario repoFormulario;

    @Autowired
    private RepoCadastro repoCadastro;

    /**
     * Cria um novo formulário
     * Valida se o usuário é administrador e se os dados são válidos
     */
    public Formulario criarFormulario(String titulo, String descricao, Long criadorId) {
        // Validar se o criador existe
        Optional<Pessoa> criadorOpt = repoCadastro.findById(criadorId);
        if (criadorOpt.isEmpty()) {
            return null;
        }

        Pessoa criador = criadorOpt.get();

        // Debug: verificar tipo de acesso
        System.out.println("[DEBUG FORMULARIO] Tipo de acesso do usuário: '" + criador.getTipoAcesso() + "'");
        System.out.println("[DEBUG FORMULARIO] É administrador? " + "ADMINISTRADOR".equalsIgnoreCase(criador.getTipoAcesso()));

        // Validar se o usuário é administrador
        if (!"ADMINISTRADOR".equalsIgnoreCase(criador.getTipoAcesso())) {
            System.out.println("[DEBUG FORMULARIO] Usuário não é administrador. Tipo: " + criador.getTipoAcesso());
            return null;
        }

        // Validar dados do formulário
        if (titulo == null || titulo.trim().isEmpty()) {
            return null;
        }

        // Criar formulário
        Formulario formulario = new Formulario(titulo, descricao, criador);
        return repoFormulario.save(formulario);
    }

    //Adiciona uma pergunta a um formulário
    public boolean adicionarPergunta(Long formularioId, Long criadorId, String texto, 
                                     String tipoPergunta, boolean obrigatoria, Integer ordem) {
        // Validar se o formulário existe e pertence ao criador
        Optional<Pessoa> criadorOpt = repoCadastro.findById(criadorId);
        if (criadorOpt.isEmpty()) {
            return false;
        }

        Pessoa criador = criadorOpt.get();
        Optional<Formulario> formularioOpt = repoFormulario.findByIdAndCriador(formularioId, criador);
        
        if (formularioOpt.isEmpty()) {
            return false;
        }

        Formulario formulario = formularioOpt.get();

        // Validar dados da pergunta
        if (texto == null || texto.trim().isEmpty()) {
            return false;
        }

        if (tipoPergunta == null || tipoPergunta.trim().isEmpty()) {
            return false;
        }

        // Criar pergunta
        Pergunta pergunta = new Pergunta(formulario, texto, tipoPergunta, obrigatoria, ordem);
        formulario.getPerguntas().add(pergunta);
        repoFormulario.save(formulario);

        return true;
    }

    //Busca formulário por ID e valida se pertence ao criador
    public Optional<Formulario> buscarFormularioPorId(Long formularioId, Long criadorId) {
        Optional<Pessoa> criadorOpt = repoCadastro.findById(criadorId);
        if (criadorOpt.isEmpty()) {
            return Optional.empty();
        }

        Pessoa criador = criadorOpt.get();
        return repoFormulario.findByIdAndCriador(formularioId, criador);
    }

    //Lista todos os formulários de um criador
    public List<Formulario> listarFormulariosPorCriador(Long criadorId) {
        Optional<Pessoa> criadorOpt = repoCadastro.findById(criadorId);
        if (criadorOpt.isEmpty()) {
            return List.of();
        }

        Pessoa criador = criadorOpt.get();
        if (!"ADMINISTRADOR".equalsIgnoreCase(criador.getTipoAcesso())) {
            return List.of();
        }

        return repoFormulario.findByCriador(criador);
    }

    //Lista formulários de um criador por status
    public List<Formulario> listarFormulariosPorStatus(Long criadorId, String status) {
        Optional<Pessoa> criadorOpt = repoCadastro.findById(criadorId);
        if (criadorOpt.isEmpty()) {
            return List.of();
        }

        Pessoa criador = criadorOpt.get();
        if (!"ADMINISTRADOR".equalsIgnoreCase(criador.getTipoAcesso())) {
            return List.of();
        }

        return repoFormulario.findByCriadorAndStatus(criador, status);
    }

    //Atualiza um formulário existente
    public boolean atualizarFormulario(Long formularioId, Long criadorId, String titulo, 
                                       String descricao, String status) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        
        if (formularioOpt.isEmpty()) {
            return false;
        }

        Formulario formulario = formularioOpt.get();

        if (titulo != null && !titulo.trim().isEmpty()) {
            formulario.setTitulo(titulo);
        }

        if (descricao != null) {
            formulario.setDescricao(descricao);
        }

        if (status != null && !status.trim().isEmpty()) {
            formulario.setStatus(status);
        }

        repoFormulario.save(formulario);
        return true;
    }

    //Remove um formulário
    public boolean removerFormulario(Long formularioId, Long criadorId) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        
        if (formularioOpt.isEmpty()) {
            return false;
        }

        repoFormulario.delete(formularioOpt.get());
        return true;
    }

    //Valida se um usuário é administrador
    public boolean isAdministrador(Long usuarioId) {
        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            return false;
        }

        Pessoa pessoa = pessoaOpt.get();
        return "ADMINISTRADOR".equalsIgnoreCase(pessoa.getTipoAcesso());
    }
}


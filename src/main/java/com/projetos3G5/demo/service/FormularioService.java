package com.projetos3G5.demo.service;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.entities.Pergunta;
import com.projetos3G5.demo.entities.FormularioDestinatario;
import com.projetos3G5.demo.entities.RespostaFormulario;
import com.projetos3G5.demo.repositories.RepoCadastro;
import com.projetos3G5.demo.repositories.RepoFormulario;
import com.projetos3G5.demo.repositories.RepoFormularioDestinatario;
import com.projetos3G5.demo.repositories.RepoRespostaFormulario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FormularioService {

    @Autowired
    private RepoFormulario repoFormulario;

    @Autowired
    private RepoCadastro repoCadastro;

    @Autowired
    private RepoFormularioDestinatario repoFormularioDestinatario;

    @Autowired
    private RepoRespostaFormulario repoRespostaFormulario;

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
            // Se voltou para rascunho ou inativo, limpamos destinatários e respostas
            if ("RASCUNHO".equalsIgnoreCase(status) || "INATIVO".equalsIgnoreCase(status)) {
                limparDestinatarios(formulario);
                repoRespostaFormulario.deleteByFormulario(formulario);
            }
        }

        repoFormulario.save(formulario);
        return true;
    }

    //Remove um formulário
    @Transactional
    public boolean removerFormulario(Long formularioId, Long criadorId) {
        Optional<Formulario> formularioOpt = repoFormulario.findById(formularioId);
        if (formularioOpt.isEmpty()) {
            return false;
        }
        Formulario formulario = formularioOpt.get();
        // Permitir se criador ou administrador
        if (!formulario.getCriador().getId().equals(criadorId) && !isAdministrador(criadorId)) {
            return false;
        }
        if ("INATIVO".equalsIgnoreCase(formulario.getStatus())) {
            // Exclusão definitiva
            limparDestinatarios(formulario);
            repoRespostaFormulario.deleteByFormulario(formulario);
            repoFormulario.delete(formulario);
            return true;
        } else {
            formulario.setStatus("INATIVO"); // soft delete para aparecer em "deletadas"
            repoFormulario.save(formulario);
            limparDestinatarios(formulario);
            repoRespostaFormulario.deleteByFormulario(formulario);
            return true;
        }
    }

    // Recupera formulário marcado como deletado para rascunho
    @Transactional
    public boolean recuperarFormulario(Long formularioId, Long criadorId) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        if (formularioOpt.isEmpty()) return false;
        Formulario formulario = formularioOpt.get();
        formulario.setStatus("RASCUNHO");
        repoFormulario.save(formulario);
        limparDestinatarios(formulario);
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

    // Destinatários
    public List<FormularioDestinatario> listarDestinatarios(Long formularioId, Long criadorId) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        if (formularioOpt.isEmpty()) return List.of();
        return repoFormularioDestinatario.findByFormulario(formularioOpt.get());
    }

    @Transactional
    public List<FormularioDestinatario> atribuirDestinatarios(Long formularioId, Long criadorId, List<Long> destinatariosIds, boolean selecionarTodos) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        if (formularioOpt.isEmpty()) return List.of();
        Formulario formulario = formularioOpt.get();

        List<Pessoa> destinatarios;
        if (selecionarTodos) {
            destinatarios = repoCadastro.findAll();
        } else {
            destinatarios = repoCadastro.findAllById(destinatariosIds);
        }

        // Substitui completamente a lista: remove antigos e grava novos
        repoFormularioDestinatario.deleteByFormulario(formulario);
        destinatarios.forEach(dest -> repoFormularioDestinatario.save(new FormularioDestinatario(formulario, dest)));

        return repoFormularioDestinatario.findByFormulario(formulario);
    }

    // Respostas
    public boolean salvarRespostaPublica(Long formularioId, Long respondenteId, String respondenteEmail, String respostasJson) {
        Optional<Formulario> formularioOpt = repoFormulario.findById(formularioId);
        if (formularioOpt.isEmpty()) return false;
        Formulario form = formularioOpt.get();

        Pessoa respondente = null;
        if (respondenteId != null) {
            respondente = repoCadastro.findById(respondenteId).orElse(null);
        }

        RespostaFormulario resposta = new RespostaFormulario(form, respondente, respondenteEmail, respostasJson);
        repoRespostaFormulario.save(resposta);
        return true;
    }

    public Optional<Formulario> buscarPublico(Long formularioId) {
        return repoFormulario.findById(formularioId)
                .filter(f -> "ATIVO".equalsIgnoreCase(f.getStatus()) || "PUBLICO".equalsIgnoreCase(f.getStatus()));
    }

    // Formularios recebidos (destinatários)
    public List<Formulario> listarRecebidos(Long usuarioId) {
        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) return List.of();
        Pessoa pessoa = pessoaOpt.get();
        List<FormularioDestinatario> dests = repoFormularioDestinatario.findByDestinatario(pessoa);
        return dests.stream()
                .map(FormularioDestinatario::getFormulario)
                // Somente formulários publicados (ATIVO/PUBLICO) chegam ao destinatário
                .filter(f -> {
                    String st = f.getStatus() != null ? f.getStatus().toUpperCase() : "";
                    return "ATIVO".equals(st) || "PUBLICO".equals(st);
                })
                .collect(Collectors.toList());
    }

    public void limparDestinatarios(Formulario formulario) {
        repoFormularioDestinatario.deleteByFormulario(formulario);
    }

    @Transactional
    public boolean despublicar(Long formularioId, Long criadorId) {
        Optional<Formulario> formularioOpt = buscarFormularioPorId(formularioId, criadorId);
        if (formularioOpt.isEmpty()) return false;
        Formulario formulario = formularioOpt.get();
        formulario.setStatus("RASCUNHO");
        repoFormulario.save(formulario);
        limparDestinatarios(formulario);
        return true;
    }

    // manter aqui sem redefinir (já movido para a posição anterior)
}


package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Formulario;
import com.projetos3G5.demo.entities.Pergunta;
import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import com.projetos3G5.demo.service.FormularioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/formularios")
public class FormularioRestController {

    @Autowired
    private FormularioService formularioService;

    @Autowired
    private RepoCadastro repoCadastro;

    // Criar novo formulário
    @PostMapping
    public ResponseEntity<Map<String, Object>> criarFormulario(
            @RequestBody Map<String, String> data,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        // Debug
        System.out.println("[DEBUG CRIAR FORM] Sessão ID: " + session.getId());
        System.out.println("[DEBUG CRIAR FORM] Usuário ID: " + usuarioId);
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            response.put("sessionId", session.getId());
            response.put("debug", "Sessão existe mas não tem usuarioId");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String titulo = data.get("titulo");
        String descricao = data.get("descricao");

        if (titulo == null || titulo.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Título é obrigatório");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Verificar se o usuário existe no banco
        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            System.out.println("[DEBUG CRIAR FORM] ERRO: Usuário com ID " + usuarioId + " não encontrado no banco!");
            System.out.println("[DEBUG CRIAR FORM] Isso pode acontecer se o banco foi resetado (H2 em memória)");
            response.put("success", false);
            response.put("message", "Usuário não encontrado no banco. O banco H2 em memória foi resetado. Crie um novo usuário e faça login novamente.");
            response.put("usuarioId", usuarioId);
            response.put("dica", "O banco H2 em memória é resetado quando o servidor reinicia. Crie um novo usuário.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Pessoa pessoa = pessoaOpt.get();
        System.out.println("[DEBUG CRIAR FORM] Usuário encontrado: " + pessoa.getNome());
        System.out.println("[DEBUG CRIAR FORM] Tipo de acesso: " + pessoa.getTipoAcesso());
        System.out.println("[DEBUG CRIAR FORM] Email: " + pessoa.getEmail());
        
        // Verificar tipo antes de tentar criar
        if (!"ADMINISTRADOR".equalsIgnoreCase(pessoa.getTipoAcesso())) {
            response.put("success", false);
            response.put("message", "Erro ao criar formulário. Você precisa ser ADMINISTRADOR. Seu tipo atual: " + pessoa.getTipoAcesso());
            response.put("tipoUsuario", pessoa.getTipoAcesso());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        Formulario formulario = formularioService.criarFormulario(titulo, descricao, usuarioId);
        
        if (formulario == null) {
            response.put("success", false);
            response.put("message", "Erro ao criar formulário. Verifique os dados.");
            response.put("tipoUsuario", pessoa.getTipoAcesso());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("success", true);
        response.put("message", "Formulário criado com sucesso");
        response.put("formulario", toMap(formulario));
        return ResponseEntity.ok(response);
    }

    // Link público para visualizar formulário ativo
    @GetMapping("/public/{id}")
    public ResponseEntity<Map<String, Object>> obterFormularioPublico(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Optional<Formulario> formularioOpt = formularioService.buscarPublico(id);
        if (formularioOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Formulário não encontrado ou inativo");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        response.put("success", true);
        response.put("formulario", toMap(formularioOpt.get()));
        return ResponseEntity.ok(response);
    }

    // Listar formulários do usuário
    @GetMapping
    public ResponseEntity<Map<String, Object>> listarFormularios(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        // Debug
        System.out.println("[DEBUG LISTAR] Sessão ID: " + session.getId());
        System.out.println("[DEBUG LISTAR] Usuário ID: " + usuarioId);
        
        // Sempre retornar success: true se autenticado, mesmo sem formulários
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            response.put("formularios", List.of());
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        }

        List<Formulario> formularios = formularioService.listarFormulariosPorCriador(usuarioId);
        List<Map<String, Object>> formulariosMap = formularios.stream()
                .map(this::toMap)
                .collect(Collectors.toList());

        response.put("success", true);
        response.put("formularios", formulariosMap);
        response.put("message", formulariosMap.isEmpty() ? "Nenhum formulário encontrado" : "Formulários listados com sucesso");
        return ResponseEntity.ok(response);
    }

    // Formularios recebidos (destinatários)
    @GetMapping("/recebidos")
    public ResponseEntity<Map<String, Object>> listarRecebidos(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            response.put("formularios", List.of());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        List<Formulario> forms = formularioService.listarRecebidos(usuarioId);
        List<Map<String, Object>> formulariosMap = forms.stream().map(this::toMap).collect(Collectors.toList());
        response.put("success", true);
        response.put("formularios", formulariosMap);
        return ResponseEntity.ok(response);
    }

    // Buscar formulário por ID
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> buscarFormulario(
            @PathVariable Long id,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Formulario> formularioOpt = formularioService.buscarFormularioPorId(id, usuarioId);
        
        if (formularioOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Formulário não encontrado ou você não tem permissão");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("success", true);
        response.put("formulario", toMap(formularioOpt.get()));
        return ResponseEntity.ok(response);
    }

    // Atualizar formulário
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> atualizarFormulario(
            @PathVariable Long id,
            @RequestBody Map<String, String> data,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String titulo = data.get("titulo");
        String descricao = data.get("descricao");
        String status = data.get("status");

        boolean sucesso = formularioService.atualizarFormulario(id, usuarioId, titulo, descricao, status);
        
        if (sucesso) {
            response.put("success", true);
            response.put("message", "Formulário atualizado com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erro ao atualizar formulário");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Remover formulário
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> removerFormulario(
            @PathVariable Long id,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        boolean sucesso = formularioService.removerFormulario(id, usuarioId);
        
        if (sucesso) {
            response.put("success", true);
            response.put("message", "Formulário removido com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erro ao remover formulário");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // Adicionar pergunta ao formulário
    @PostMapping("/{id}/perguntas")
    public ResponseEntity<Map<String, Object>> adicionarPergunta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> data,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String texto = (String) data.get("texto");
        String tipoPergunta = (String) data.get("tipoPergunta");
        Boolean obrigatoria = (Boolean) data.getOrDefault("obrigatoria", false);
        Integer ordem = data.get("ordem") != null ? (Integer) data.get("ordem") : 1;

        if (texto == null || texto.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Texto da pergunta é obrigatório");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean sucesso = formularioService.adicionarPergunta(id, usuarioId, texto, tipoPergunta, obrigatoria, ordem);
        
        if (sucesso) {
            response.put("success", true);
            response.put("message", "Pergunta adicionada com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erro ao adicionar pergunta");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{id}/despublicar")
    public ResponseEntity<Map<String, Object>> despublicar(
            @PathVariable Long id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
          response.put("success", false);
          response.put("message", "Usuário não autenticado");
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        boolean ok = formularioService.despublicar(id, usuarioId);
        response.put("success", ok);
        response.put("message", ok ? "Publicação removida e movida para rascunho." : "Não foi possível despublicar.");
        return ok ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @PostMapping("/{id}/recuperar")
    public ResponseEntity<Map<String, Object>> recuperar(
            @PathVariable Long id,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
          response.put("success", false);
          response.put("message", "Usuário não autenticado");
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        boolean ok = formularioService.recuperarFormulario(id, usuarioId);
        response.put("success", ok);
        response.put("message", ok ? "Formulário recuperado para rascunho." : "Não foi possível recuperar.");
        return ok ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Atribuir destinatários (selecionar usuários para envio)
    @PostMapping("/{id}/destinatarios")
    public ResponseEntity<Map<String, Object>> atribuirDestinatarios(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        if (!formularioService.isAdministrador(usuarioId)) {
            response.put("success", false);
            response.put("message", "Apenas administradores podem atribuir destinatários");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        boolean selecionarTodos = Boolean.TRUE.equals(body.get("selecionarTodos"));
        List<Integer> ids = (List<Integer>) body.getOrDefault("usuarioIds", List.of());
        List<Long> destinatariosIds = ids.stream().map(Integer::longValue).collect(Collectors.toList());

        var dests = formularioService.atribuirDestinatarios(id, usuarioId, destinatariosIds, selecionarTodos);
        response.put("success", true);
        response.put("destinatarios", dests.stream().map(d -> Map.of(
                "id", d.getId(),
                "usuarioId", d.getDestinatario().getId(),
                "nome", d.getDestinatario().getNome(),
                "email", d.getDestinatario().getEmail()
        )));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/destinatarios")
    public ResponseEntity<Map<String, Object>> listarDestinatarios(@PathVariable Long id, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        var dests = formularioService.listarDestinatarios(id, usuarioId);
        response.put("success", true);
        response.put("destinatarios", dests.stream().map(d -> Map.of(
                "id", d.getId(),
                "usuarioId", d.getDestinatario().getId(),
                "nome", d.getDestinatario().getNome(),
                "email", d.getDestinatario().getEmail()
        )));
        return ResponseEntity.ok(response);
    }

    // Responder formulário (público ou autenticado)
    @PostMapping({"/{id}/respostas", "/public/{id}/respostas"})
    public ResponseEntity<Map<String, Object>> responder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        String respostasJson = body.get("respostasJson") != null ? body.get("respostasJson").toString() : "{}";
        String email = body.get("email") != null ? body.get("email").toString() : null;

        boolean ok = formularioService.salvarRespostaPublica(id, usuarioId, email, respostasJson);
        response.put("success", ok);
        response.put("message", ok ? "Resposta registrada" : "Não foi possível registrar a resposta");
        return ok ? ResponseEntity.ok(response) : ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // Helper para converter Formulario para Map
    private Map<String, Object> toMap(Formulario formulario) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", formulario.getId());
        map.put("titulo", formulario.getTitulo());
        map.put("descricao", formulario.getDescricao());
        map.put("status", formulario.getStatus());
        map.put("dataCriacao", formulario.getDataCriacao() != null ? formulario.getDataCriacao().toString() : null);
        map.put("dataAtualizacao", formulario.getDataAtualizacao() != null ? formulario.getDataAtualizacao().toString() : null);
        
        if (formulario.getPerguntas() != null) {
            List<Map<String, Object>> perguntas = formulario.getPerguntas().stream()
                    .map(this::perguntaToMap)
                    .collect(Collectors.toList());
            map.put("perguntas", perguntas);
        } else {
            map.put("perguntas", List.of());
        }
        
        return map;
    }

    private Map<String, Object> perguntaToMap(Pergunta pergunta) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", pergunta.getId());
        map.put("texto", pergunta.getTexto());
        map.put("tipoPergunta", pergunta.getTipoPergunta());
        map.put("obrigatoria", pergunta.isObrigatoria());
        map.put("ordem", pergunta.getOrdem());
        map.put("opcoes", pergunta.getOpcoes());
        return map;
    }
}


package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import com.projetos3G5.demo.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    @Autowired
    private CadastroService cadastroService;

    @Autowired
    private RepoCadastro repoCadastro;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> credentials,
            HttpSession session) {
        
        String email = credentials.get("email");
        String senha = credentials.get("password");
        
        Map<String, Object> response = new HashMap<>();
        
        if (email == null || senha == null) {
            response.put("success", false);
            response.put("message", "Email e senha são obrigatórios");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean isAutenticado = cadastroService.autenticarUsuario(email, senha);
        
        if (isAutenticado) {
            Pessoa pessoa = repoCadastro.findByEmail(email);
            if (pessoa != null) {
                // Criar/atualizar sessão
                session.setAttribute("usuarioId", pessoa.getId());
                session.setAttribute("usuarioNome", pessoa.getNome());
                session.setAttribute("usuarioTipo", pessoa.getTipoAcesso());
                
                // Debug: verificar se sessão foi criada
                System.out.println("[DEBUG LOGIN] Sessão criada - ID: " + session.getId());
                System.out.println("[DEBUG LOGIN] Usuário ID salvo: " + pessoa.getId());
                System.out.println("[DEBUG LOGIN] Verificando atributo: " + session.getAttribute("usuarioId"));
                
                response.put("success", true);
                response.put("message", "Login realizado com sucesso");
                response.put("sessionId", session.getId()); // Incluir sessionId na resposta para debug
                Map<String, Object> user = new HashMap<>();
                user.put("id", pessoa.getId());
                user.put("name", pessoa.getNome());
                user.put("email", pessoa.getEmail());
                user.put("role", pessoa.getTipoAcesso().toLowerCase());
                response.put("user", user);
                
                return ResponseEntity.ok(response);
            }
        }
        
        response.put("success", false);
        response.put("message", "Email ou senha incorretos");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> data) {
        Map<String, Object> response = new HashMap<>();
        
        String nome = data.get("nome");
        String email = data.get("email");
        String senha = data.get("password");
        String tipo = data.get("tipo");
        
        if (nome == null || email == null || senha == null || tipo == null) {
            response.put("success", false);
            response.put("message", "Todos os campos são obrigatórios");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Converter tipo para o formato esperado pelo backend
        String tipoAcesso;
        if (tipo.equalsIgnoreCase("empresa") || tipo.equalsIgnoreCase("administrador")) {
            tipoAcesso = "ADMINISTRADOR";
        } else {
            tipoAcesso = "CLIENTE";
        }
        
        System.out.println("[DEBUG REGISTER] Tipo recebido: '" + tipo + "'");
        System.out.println("[DEBUG REGISTER] Tipo convertido: '" + tipoAcesso + "'");
        
        Pessoa pessoa = new Pessoa(nome, email, senha, tipoAcesso);
        boolean sucesso = cadastroService.salvarUsuario(pessoa);
        
        if (sucesso) {
            // Verificar o que foi salvo
            Pessoa pessoaSalva = repoCadastro.findByEmail(email);
            if (pessoaSalva != null) {
                System.out.println("[DEBUG REGISTER] Pessoa salva com tipo: '" + pessoaSalva.getTipoAcesso() + "'");
            }
        }
        
        if (sucesso) {
            response.put("success", true);
            response.put("message", "Cadastro realizado com sucesso");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Erro ao cadastrar. Email já pode estar em uso.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        // Debug: logar informações da sessão
        System.out.println("[DEBUG] Sessão ID: " + session.getId());
        System.out.println("[DEBUG] Usuário ID na sessão: " + usuarioId);
        System.out.println("[DEBUG] Todos os atributos da sessão: " + java.util.Collections.list(session.getAttributeNames()));
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("user", null);
            response.put("sessionId", session.getId());
            return ResponseEntity.ok(response);
        }
        
        Pessoa pessoa = repoCadastro.findById(usuarioId).orElse(null);
        if (pessoa != null) {
            Map<String, Object> user = new HashMap<>();
            user.put("id", pessoa.getId());
            user.put("name", pessoa.getNome());
            user.put("email", pessoa.getEmail());
            user.put("role", pessoa.getTipoAcesso().toLowerCase());
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        }
        
        response.put("success", false);
        response.put("user", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logout realizado com sucesso");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/user")
    public ResponseEntity<Map<String, Object>> atualizarPerfil(
            @RequestBody Map<String, String> data,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();
        
        // Atualizar campos fornecidos
        if (data.containsKey("nome")) {
            String nome = data.get("nome");
            if (nome != null && !nome.trim().isEmpty()) {
                pessoa.setNome(nome.trim());
            }
        }
        
        if (data.containsKey("email")) {
            String email = data.get("email");
            if (email != null && !email.trim().isEmpty()) {
                // Verificar se email já está em uso por outro usuário
                Pessoa pessoaComEmail = repoCadastro.findByEmail(email.trim());
                if (pessoaComEmail != null && !pessoaComEmail.getId().equals(usuarioId)) {
                    response.put("success", false);
                    response.put("message", "Este email já está em uso por outro usuário");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                pessoa.setEmail(email.trim());
            }
        }

        try {
            repoCadastro.save(pessoa);
            
            // Atualizar sessão
            session.setAttribute("usuarioNome", pessoa.getNome());
            
            response.put("success", true);
            response.put("message", "Perfil atualizado com sucesso");
            Map<String, Object> user = new HashMap<>();
            user.put("id", pessoa.getId());
            user.put("name", pessoa.getNome());
            user.put("email", pessoa.getEmail());
            user.put("role", pessoa.getTipoAcesso().toLowerCase());
            response.put("user", user);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao atualizar perfil: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/user/password")
    public ResponseEntity<Map<String, Object>> alterarSenha(
            @RequestBody Map<String, String> data,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String senhaAtual = data.get("senhaAtual");
        String novaSenha = data.get("novaSenha");

        if (senhaAtual == null || novaSenha == null || novaSenha.length() < 6) {
            response.put("success", false);
            response.put("message", "Senha inválida. A nova senha deve ter pelo menos 6 caracteres.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuário não encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();
        
        // Verificar senha atual
        if (!cadastroService.autenticarUsuario(pessoa.getEmail(), senhaAtual)) {
            response.put("success", false);
            response.put("message", "Senha atual incorreta");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // Atualizar senha
        try {
            pessoa.setSenha(novaSenha);
            cadastroService.salvarUsuario(pessoa); // Isso vai fazer o hash da senha
            
            response.put("success", true);
            response.put("message", "Senha alterada com sucesso");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao alterar senha: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}


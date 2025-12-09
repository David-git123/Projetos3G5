package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import com.projetos3G5.demo.service.CadastroService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            response.put("message", "Email e senha sao obrigatorios");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean isAutenticado = cadastroService.autenticarUsuario(email, senha);

        if (isAutenticado) {
            Pessoa pessoa = repoCadastro.findByEmail(email);
            if (pessoa != null) {
                session.setAttribute("usuarioId", pessoa.getId());
                session.setAttribute("usuarioNome", pessoa.getNome());
                session.setAttribute("usuarioTipo", pessoa.getTipoAcesso());

                response.put("success", true);
                response.put("message", "Login realizado com sucesso");
                response.put("sessionId", session.getId());
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
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> data, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        String nome = data.get("nome") != null ? data.get("nome").trim() : null;
        String email = data.get("email") != null ? data.get("email").trim() : null;
        String senha = data.get("password") != null ? data.get("password").trim() : null;
        String confirmSenha = data.get("confirmPassword") != null ? data.get("confirmPassword").trim() : null;
        String tipo = data.get("tipo");

        if (nome == null || nome.isEmpty() || email == null || email.isEmpty() || senha == null || senha.isEmpty()) {
            response.put("success", false);
            response.put("message", "Todos os campos sao obrigatorios");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (confirmSenha != null && !confirmSenha.equals(senha)) {
            response.put("success", false);
            response.put("message", "Senhas nao conferem");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean isAdminLogado = "ADMINISTRADOR".equals(session.getAttribute("usuarioTipo"));
        String tipoAcesso = "CLIENTE";
        if (tipo != null && isAdminLogado) {
            if (tipo.equalsIgnoreCase("empresa") || tipo.equalsIgnoreCase("administrador")) {
                tipoAcesso = "ADMINISTRADOR";
            } else if (tipo.equalsIgnoreCase("cliente")) {
                tipoAcesso = "CLIENTE";
            }
        }

        Pessoa pessoa = new Pessoa(nome, email, senha, tipoAcesso);
        boolean sucesso = cadastroService.salvarUsuario(pessoa);

        if (sucesso) {
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
            response.put("message", "Erro ao cadastrar. Email ja pode estar em uso.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");

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
            response.put("message", "Usuario nao autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuario nao encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();

        if (data.containsKey("nome")) {
            String novoNome = data.get("nome");
            if (novoNome != null && !novoNome.trim().isEmpty()) {
                pessoa.setNome(novoNome.trim());
            }
        }

        if (data.containsKey("email")) {
            String novoEmail = data.get("email");
            if (novoEmail != null && !novoEmail.trim().isEmpty()) {
                Pessoa pessoaComEmail = repoCadastro.findByEmail(novoEmail.trim());
                if (pessoaComEmail != null && !pessoaComEmail.getId().equals(usuarioId)) {
                    response.put("success", false);
                    response.put("message", "Este email ja esta em uso por outro usuario");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
                pessoa.setEmail(novoEmail.trim());
            }
        }

        try {
            repoCadastro.save(pessoa);

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
            response.put("message", "Usuario nao autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        String senhaAtual = data.get("senhaAtual");
        String novaSenha = data.get("novaSenha");

        if (senhaAtual == null || novaSenha == null || novaSenha.length() < 6) {
            response.put("success", false);
            response.put("message", "Senha invalida. A nova senha deve ter pelo menos 6 caracteres.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Optional<Pessoa> pessoaOpt = repoCadastro.findById(usuarioId);
        if (pessoaOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Usuario nao encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Pessoa pessoa = pessoaOpt.get();

        if (!cadastroService.autenticarUsuario(pessoa.getEmail(), senhaAtual)) {
            response.put("success", false);
            response.put("message", "Senha atual incorreta");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            pessoa.setSenha(novaSenha);
            cadastroService.salvarUsuario(pessoa);

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

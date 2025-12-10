package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    @Autowired
    private RepoCadastro repoCadastro;

    private Map<String, Object> toUserMap(Pessoa p) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId());
        m.put("nome", p.getNome());
        m.put("email", p.getEmail());
        m.put("role", p.getTipoAcesso() != null ? p.getTipoAcesso().toLowerCase() : "cliente");
        m.put("empresa", p.getEmpresaNome() != null ? p.getEmpresaNome() : "");
        return m;
    }

    private boolean isAdmin(HttpSession session) {
        String tipo = (String) session.getAttribute("usuarioTipo");
        return tipo != null && tipo.equalsIgnoreCase("ADMINISTRADOR");
    }

    @GetMapping("/usuarios")
    public ResponseEntity<Map<String, Object>> listarUsuarios(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("success", false);
            res.put("message", "Apenas administradores podem acessar.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }
        List<Map<String, Object>> usuarios = repoCadastro.findAll().stream()
                .map(this::toUserMap)
                .collect(Collectors.toList());
        res.put("success", true);
        res.put("usuarios", usuarios);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/promover-empresa")
    public ResponseEntity<Map<String, Object>> promover(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("success", false);
            res.put("message", "Apenas administradores podem promover.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }
        String email = body.getOrDefault("email", "").trim();
        String empresa = body.getOrDefault("empresa", "").trim();
        if (email.isEmpty()) {
            res.put("success", false);
            res.put("message", "Email é obrigatório.");
            return ResponseEntity.badRequest().body(res);
        }
        Pessoa p = repoCadastro.findByEmail(email);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Usuário não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        p.setTipoAcesso("EMPRESA");
        p.setEmpresaNome(empresa);
        repoCadastro.save(p);
        res.put("success", true);
        res.put("message", "Usuário promovido para empresa.");
        res.put("user", toUserMap(p));
        return ResponseEntity.ok(res);
    }

    @PostMapping("/despromover-empresa")
    public ResponseEntity<Map<String, Object>> despromover(
            @RequestBody Map<String, String> body,
            HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("success", false);
            res.put("message", "Apenas administradores podem despromover.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }
        String email = body.getOrDefault("email", "").trim();
        if (email.isEmpty()) {
            res.put("success", false);
            res.put("message", "Email é obrigatório.");
            return ResponseEntity.badRequest().body(res);
        }
        Pessoa p = repoCadastro.findByEmail(email);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Usuário não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        p.setTipoAcesso("CLIENTE");
        p.setEmpresaNome(null);
        repoCadastro.save(p);
        res.put("success", true);
        res.put("message", "Usuário despromovido para cliente.");
        res.put("user", toUserMap(p));
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/usuarios/{email}")
    public ResponseEntity<Map<String, Object>> apagar(
            @PathVariable String email,
            HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        if (!isAdmin(session)) {
            res.put("success", false);
            res.put("message", "Apenas administradores podem apagar usuários.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(res);
        }
        Pessoa p = repoCadastro.findByEmail(email);
        if (p == null) {
            res.put("success", false);
            res.put("message", "Usuário não encontrado.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(res);
        }
        repoCadastro.delete(p);
        res.put("success", true);
        res.put("message", "Usuário removido.");
        return ResponseEntity.ok(res);
    }
}

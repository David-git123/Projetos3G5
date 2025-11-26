package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.repositories.RepoCadastro;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UsuarioRestController {

    @Autowired
    private RepoCadastro repoCadastro;

    @GetMapping("/api/usuarios")
    public ResponseEntity<Map<String, Object>> buscarUsuarios(
            @RequestParam(value = "q", required = false) String q,
            HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            response.put("success", false);
            response.put("message", "Usuário não autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        List<Pessoa> pessoas;
        if (q == null || q.isBlank()) {
            pessoas = repoCadastro.findAll();
        } else {
            pessoas = repoCadastro.findByNomeContainingIgnoreCaseOrEmailContainingIgnoreCase(q, q);
        }

        response.put("success", true);
        response.put("usuarios", pessoas.stream().map(p -> Map.of(
                "id", p.getId(),
                "nome", p.getNome(),
                "email", p.getEmail(),
                "role", p.getTipoAcesso()
        )).collect(Collectors.toList()));
        return ResponseEntity.ok(response);
    }
}

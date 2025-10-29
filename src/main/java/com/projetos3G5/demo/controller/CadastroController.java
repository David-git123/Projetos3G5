package com.projetos3G5.demo.controller;

import com.projetos3G5.demo.entities.Pessoa;
import com.projetos3G5.demo.service.CadastroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/")
public class CadastroController {

    @Autowired
    CadastroService cadastroService;

    @GetMapping()
    public String paginaLogin(Model model){
        return "login";
    }
    @PostMapping()
    public String processarLogin(@RequestParam String email, @RequestParam String senha){
        boolean isAutenticado = cadastroService.autenticarUsuario(email,senha);
        if(isAutenticado){
            return "dashboard";
        }
        else{
            return "login";
        }
    }

    @PostMapping("cadastro/")
    public String receberFormularioCadastro(@ModelAttribute Pessoa pessoa, Model model){
        if(cadastroService.salvarUsuario(pessoa)){
            model.addAttribute("mensagem","Sucesso");
            return "login";
        }
        else{
            model.addAttribute("mensagem","Falha no cadastro");
            model.addAttribute("pessoa",new Pessoa());
            return "telaCadastro";
        }
    }
    @GetMapping("cadastro/")
    public String paginaCadastro(Model model){
        model.addAttribute("pessoa", new Pessoa());
        return "telaCadastro";
    }

}

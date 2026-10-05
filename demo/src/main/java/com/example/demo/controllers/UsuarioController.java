package com.example.demo.controllers;

import com.example.demo.dtos.UsuarioDto;
import com.example.demo.services.UsuarioService;
import com.example.demo.sessoes.SessaoDto;
import com.example.demo.sessoes.SessaoUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/usuarios/salvar")
    public String salvar(@ModelAttribute UsuarioDto usuarioDto,
                         RedirectAttributes redirectAttributes,
                         HttpSession session) {

        if (usuarioDto.getNome() == null || usuarioDto.getNome().isBlank()
                || usuarioDto.getEmail() == null || usuarioDto.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("erroUsuario", "Nome e e-mail sao obrigatorios.");
            return "redirect:/usuarios";
        }

        if (usuarioDto.getId() == null
                && (usuarioDto.getSenha() == null || usuarioDto.getSenha().isBlank())) {
            redirectAttributes.addFlashAttribute("erroUsuario", "A senha e obrigatoria para novo usuario.");
            return "redirect:/usuarios";
        }

        SessaoDto sessao = SessaoUtil.ObterSessao(session);
        if (sessao == null) {
            return "redirect:/login";
        }
        boolean isAdmin = sessao.getUserRole() != null && sessao.getUserRole() == 1;
        if (!isAdmin) {
            redirectAttributes.addFlashAttribute("erroUsuario", "Apenas administradores podem cadastrar ou editar usuários.");
            return "redirect:/usuarios";
        }
        try {
            UsuarioDto salvo = usuarioService.salvar(usuarioDto);

            if (salvo == null) {
                redirectAttributes.addFlashAttribute("erroUsuario", "Usuario nao encontrado.");
                return "redirect:/usuarios";
            }

            redirectAttributes.addFlashAttribute("mensagemUsuario", "Usuario salvo com sucesso.");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute("erroUsuario", "Ja existe um usuario com este e-mail.");
        }

        return "redirect:/usuarios";
    }

    @DeleteMapping("/usuarioexcluir/{id}")
    @ResponseBody
    public ResponseEntity<Void> excluir(@PathVariable Long id, HttpSession session) {
        SessaoDto sessao = SessaoUtil.ObterSessao(session);
        if (sessao == null || sessao.getUserRole() == null || sessao.getUserRole() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (usuarioService.buscarPorId(id) == null) {
            return ResponseEntity.notFound().build();
        }

        usuarioService.excluir(id);
        return ResponseEntity.ok().build();
    }


}

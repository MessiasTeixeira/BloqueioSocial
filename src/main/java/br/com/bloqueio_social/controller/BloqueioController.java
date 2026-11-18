package br.com.bloqueio_social.controller;

import br.com.bloqueio_social.model.Bloqueio;
import br.com.bloqueio_social.service.BloqueioService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.bloqueio_social.model.HistoricoSenha;
import br.com.bloqueio_social.dto.IniciarBloqueioDTO;

import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Optional;

@Controller
public class BloqueioController {

    private final BloqueioService bloqueioService;

    public BloqueioController(BloqueioService bloqueioService) {
        this.bloqueioService = bloqueioService;
    }

    @GetMapping("/")
    public String paginaInicial(Model model) {

        Optional<Bloqueio> resultado = bloqueioService.buscarBloqueio();

        if (resultado.isEmpty()) {
            return "index";
        }

        Bloqueio bloqueio = resultado.get();

        if (bloqueioService.estaBloqueado()) {
            model.addAttribute("bloqueio", bloqueio);
            return "bloqueio";
        }

        if (bloqueio.isIniciado()) {
            model.addAttribute("senha", bloqueioService.obterSenhaLiberada()
            );
            return "senha-liberada";
        }

        return "index";
    }

    @GetMapping("/inicio")
    public String visualizarInicio() {
        return "index";
    }

    @PostMapping("/gerar-senha")
    public String gerarSenha(HttpSession session, Model model) {

        if (bloqueioService.estaBloqueado()) {
            return "redirect:/";
        }

        String senha = bloqueioService.gerarSenha();

        session.setAttribute("senhaTemporaria", senha);

        model.addAttribute("senha", senha);

        return "configurar";
    }

    @PostMapping("/iniciar")
    public String iniciarBloqueio(
            @Valid @ModelAttribute IniciarBloqueioDTO dto,
            BindingResult resultado,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String senha = (String) session.getAttribute("senhaTemporaria");

        if (senha == null) {
            redirectAttributes.addFlashAttribute(
                    "erro",
                    "Não foi possível iniciar o bloqueio. Gere uma nova senha."
            );
            return "redirect:/";
        }

        if (resultado.hasErrors()) {
            model.addAttribute("senha", senha);
            model.addAttribute("erro", resultado.getFieldError("dataFinal").getDefaultMessage());
            return "configurar";
        }

        try {
            bloqueioService.iniciarBloqueio(senha, dto.getDataFinal());

            session.removeAttribute("senhaTemporaria");

            return "redirect:/";

        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("senha", senha);
            model.addAttribute("erro", e.getMessage());
            return "configurar";
        }
    }

    @GetMapping("/historico")
    public String mostrarHistorico(Model model) {

        try {
            List<HistoricoSenha> historico = bloqueioService.buscarHistorico();

            model.addAttribute("historico", historico);

            return "historico";

        } catch (IllegalStateException e) {
            return "redirect:/";
        }
    }

    @GetMapping("/historico/{id}")
    public String recuperarSenhaHistorica(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Model model
    ) {

        try {
            String senha = bloqueioService.obterSenhaHistorico(id);

            model.addAttribute("senha", senha);

            return "senha-historica";

        } catch (IllegalStateException | IllegalArgumentException e) {
            return "redirect:/historico";
        }
    }

    @PostMapping("/historico/{id}/excluir")
    public String excluirSenhaHistorica(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        try {
            bloqueioService.excluirSenhaHistorica(id);

            redirectAttributes.addFlashAttribute(
                    "mensagem",
                    "Registro excluído com sucesso."
            );

        } catch (IllegalStateException | IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/historico";
    }
    
    @PostMapping("/historico/excluir-todos")
    public String excluirTodoHistorico(RedirectAttributes redirectAttributes) {

        try {
            bloqueioService.excluirTodoHistorico();

            redirectAttributes.addFlashAttribute(
                    "mensagem",
                    "Todo o histórico foi excluído."
            );

        } catch (IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/historico";
    }

    @PostMapping("/emergencia")
        public String encerrarPorEmergencia(@RequestParam String confirmacao,RedirectAttributes redirectAttributes) {
            try {
                bloqueioService.encerrarPorEmergencia(confirmacao);

                return "redirect:/";

            } catch (IllegalArgumentException | IllegalStateException e) {

                redirectAttributes.addFlashAttribute("erro", e.getMessage());

                return "redirect:/";
            }
        }
}

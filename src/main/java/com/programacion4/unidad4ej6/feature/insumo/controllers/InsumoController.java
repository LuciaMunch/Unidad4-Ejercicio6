package com.programacion4.unidad4ej6.feature.insumo.controllers;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lombok.AllArgsConstructor;

import com.programacion4.unidad4ej6.config.exceptions.ConflictException;
import com.programacion4.unidad4ej6.feature.insumo.dtos.request.InsumoCreateDTO;
import com.programacion4.unidad4ej6.feature.insumo.services.interfaces.domain.IInsumoCreateService;
import com.programacion4.unidad4ej6.feature.insumo.services.interfaces.domain.IInsumoListService;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@AllArgsConstructor
public class InsumoController {

    private final IInsumoCreateService insumoCreateService;
    private final IInsumoListService   insumoListService;

    // ── Pantalla Principal ──────────────────────────────────────────────────
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ── Listado de Insumos ──────────────────────────────────────────────────
    @GetMapping("/insumos")
    public String listado(Model model) {
        model.addAttribute("insumos", insumoListService.listInsumos());
        return "insumos/listado";
    }

    // ── Formulario de Creación ──────────────────────────────────────────────
    @GetMapping("/insumos/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("insumo", new InsumoCreateDTO());
        return "insumos/form";
    }

    @PostMapping("/insumos")
    public String guardar(
            @Valid @ModelAttribute("insumo") InsumoCreateDTO dto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Errores de validación Bean Validation (@NotBlank, @Pattern, @Positive…)
        if (bindingResult.hasErrors()) {
            List<String> errores = bindingResult.getFieldErrors().stream()
                    .map(fe -> fe.getDefaultMessage())
                    .collect(Collectors.toList());
            model.addAttribute("errores", errores);
            return "insumos/form";
        }

        // Error de negocio: código interno duplicado
        try {
            insumoCreateService.createInsumo(dto);
        } catch (ConflictException e) {
            model.addAttribute("mensajeError", e.getMessage());
            return "insumos/form";
        }

        redirectAttributes.addFlashAttribute("mensajeExito", "Insumo registrado correctamente.");
        return "redirect:/insumos";
    }
}

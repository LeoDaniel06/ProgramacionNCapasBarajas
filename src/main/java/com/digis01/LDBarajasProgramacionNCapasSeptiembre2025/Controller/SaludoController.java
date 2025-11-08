package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SaludoController {

    @GetMapping("saludoNombre")
    public String saludoNombre(@RequestParam(name = "nombre", required = false, defaultValue = "Usuario") String nombre,
                               Model model) {
        model.addAttribute("nombre", nombre);
        return "SaludoNombre";
    }
}

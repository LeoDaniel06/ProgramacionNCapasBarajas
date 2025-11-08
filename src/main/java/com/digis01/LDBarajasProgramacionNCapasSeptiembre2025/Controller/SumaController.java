package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SumaController {

    @GetMapping("sumar")
    public String sumarNumeros(
            @RequestParam(name = "num1", required = false, defaultValue = "0") int num1,
            @RequestParam(name = "num2", required = false, defaultValue = "0") int num2,
            Model model) {

        int resultado = num1 + num2;
        model.addAttribute("num1", num1);
        model.addAttribute("num2", num2);
        model.addAttribute("resultado", resultado);

        return "Suma"; // nombre del HTML
    }
}


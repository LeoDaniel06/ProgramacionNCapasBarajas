package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("demo")
public class DemoControler {

    @GetMapping("saludo/{Hola}")
    public String Saludo(@PathVariable("Hola") String HolaMundo, Model model) {
        model.addAttribute("HolaMundo", HolaMundo);
        return "HolaMundo";
    }
}


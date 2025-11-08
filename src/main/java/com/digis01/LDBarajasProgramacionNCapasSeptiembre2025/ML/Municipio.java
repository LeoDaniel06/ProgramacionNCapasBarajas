package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class Municipio {
    private int IdMunicipio;
    
    private String Nombre;

    @NotNull(message = "Debe seleccionar un estado")
    @Valid
    public Estado estado = new Estado();

    public int getIdMunicipio() {
        return IdMunicipio;
    }

    public void setIdMunicipio(int IdMunicipio) {
        this.IdMunicipio = IdMunicipio;
    }

    public String getNombre() {
        return Nombre;
    }

    public void setNombre(String Nombre) {
        this.Nombre = Nombre;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    
    public Municipio(){
    }


    
}

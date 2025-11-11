package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="DIRECCION")
public class DireccionJPA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="iddireccion")
    private Integer IdDIreccion;
    
    @Column(name="calle")
    private String Calle;
    
    @Column(name="numerointerior")
    private String NumeroInterior;
    
    @Column(name="numeroexterior")
    private String NumeroExterior;
    
    @ManyToOne
    @JoinColumn(name="idcolonia")
    public Colonia Colonia;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="idusuario")
    public UsuarioJPA UsuarioJPA;

    public Integer getIdDIreccion() {
        return IdDIreccion;
    }

    public void setIdDIreccion(Integer IdDIreccion) {
        this.IdDIreccion = IdDIreccion;
    }

    public String getCalle() {
        return Calle;
    }

    public void setCalle(String Calle) {
        this.Calle = Calle;
    }

    public String getNumeroInterior() {
        return NumeroInterior;
    }

    public void setNumeroInterior(String NumeroInterior) {
        this.NumeroInterior = NumeroInterior;
    }

    public String getNumeroExterior() {
        return NumeroExterior;
    }

    public void setNumeroExterior(String NumeroExterior) {
        this.NumeroExterior = NumeroExterior;
    }

    public Colonia getColonia() {
        return Colonia;
    }

    public void setColonia(Colonia Colonia) {
        this.Colonia = Colonia;
    }

    public UsuarioJPA getUsuarioJPA() {
        return UsuarioJPA;
    }

    public void setUsuarioJPA(UsuarioJPA UsuarioJPA) {
        this.UsuarioJPA = UsuarioJPA;
    }
    
    
}

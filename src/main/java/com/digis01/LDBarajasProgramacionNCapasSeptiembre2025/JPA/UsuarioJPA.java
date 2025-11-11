package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name= "Usuario")
public class UsuarioJPA {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="idusuario")
    private int idUsuario;
    
    @Column(name="username")
    private String UserName;
    
    @Column(name="nombre")
    private String Nombre;
    
    @Column(name="apellidopat")
    private String ApellidoPat;
    
    @Column(name="apellidomat")
    private String ApellidoMat;
    
    @Column(name="email")
    private String Email;
    
    @Column(name="password")
    private String Password;
    
    @Column(name="fechanacimiento")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date FechaNacimiento;
    
    @Column(name="sexo")
    private String Sexo;
    
    @Column(name="telefono")
    private String Telefono;
    
    @Column(name="celular")
    private String Celular;
    
    @Column(name="curp")
    private String Curp;
    
    @Column(name="imagen")
    private String Imagen;
    
    @ManyToOne
    @JoinColumn(name="idrol")
    public RolJPA RolJPA;
    
    
}

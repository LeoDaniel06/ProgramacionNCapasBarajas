package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Direccion;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Usuario;
import java.util.List;

public interface IUsuarioDAO {
    Result GETALL();
    Result GETBYID(int idUsuario);
    Result ADD(Usuario usuario);
    Result UsuarioUPDATE(Usuario usuario);
    Result DireccionUPDATE(Direccion direccion, int idUsuario);
    Result DireccionADD(Direccion direccion, int usuario);
    Result UsuarioDELETE(int idUsuario);
    Result GetDireccionByIdDireccion(int idDireccion);
    Result DireccionDELETE(int idDireccion);
    Result UsuarioUPDATEImagen(int idUsuario, String imagen);
    Result BusquedaDinamica(Usuario usuario);
    Result AddALL(List<Usuario> usuarios);
}

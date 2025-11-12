
package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Direccion;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Usuario;



public interface IUsuarioJPA{
    
    Result GetAll();
    Result Add(Usuario usuarioML);
    Result Update(Usuario usuarioML);
    Result DireccionUPDATE(Direccion direccionML, int idUsuario);
    Result AddDireccion(Direccion direccionML, int idUsuario);
}

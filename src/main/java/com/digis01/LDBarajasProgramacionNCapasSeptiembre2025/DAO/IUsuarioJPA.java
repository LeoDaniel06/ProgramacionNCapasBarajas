
package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA.UsuarioJPA;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import org.springframework.data.jpa.repository.JpaRepository;


public interface IUsuarioJPA{
    
    Result GetAll();
}

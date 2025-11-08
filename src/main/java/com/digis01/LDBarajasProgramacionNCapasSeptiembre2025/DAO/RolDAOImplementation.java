package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Rol;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
public class RolDAOImplementation implements IRolDAO {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Override
    public Result GETALL(){
        Result result = jdbcTemplate.execute("{CALL RolGETALL(?)}",(CallableStatementCallback<Result>)callableStatement ->{
            Result resultSP = new Result();
            try {
                callableStatement.registerOutParameter(1,java.sql.Types.REF_CURSOR);
                callableStatement.execute();
                ResultSet resulSet = (ResultSet) callableStatement.getObject(1);
                resultSP.objects = new ArrayList<>();
                while(resulSet.next()){
                Rol rol = new Rol();
                rol.setIdRol(resulSet.getInt("IdRol"));
                rol.setNombre(resulSet.getString("Nombre"));
                resultSP.objects.add(rol);
                }
                resultSP.correct = true;
            } catch (Exception ex) {
                resultSP.correct = false;
                resultSP.errorMessage = ex.getLocalizedMessage();
                resultSP.ex = ex;
            }
            
            return resultSP;
        });
    return result;
    }
}

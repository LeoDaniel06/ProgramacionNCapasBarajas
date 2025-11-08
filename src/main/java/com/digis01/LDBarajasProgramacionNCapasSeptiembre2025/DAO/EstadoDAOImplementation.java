package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Estado;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EstadoDAOImplementation implements IEstadoDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result GetByIdPais(int IdPais) {
        Result result = jdbcTemplate.execute("{CALL EstadoGETBYIDPais(?, ?)}", (CallableStatementCallback<Result>) callableStatement -> {
            Result resultSP = new Result();
            try {
                callableStatement.setInt(1, IdPais);
                callableStatement.registerOutParameter(2, java.sql.Types.REF_CURSOR);
                callableStatement.execute();
                ResultSet rs = (ResultSet) callableStatement.getObject(2);
                resultSP.objects = new ArrayList<>();
                while(rs.next()){
                    Estado estado = new Estado();
                    estado.setIdEstado(rs.getInt("IdEstado"));
                    estado.setNombre(rs.getString("Nombre"));
                    resultSP.objects.add(estado);
                }
                resultSP.correct = true;
            } catch(Exception ex) {
                resultSP.correct = false;
                resultSP.errorMessage = ex.getLocalizedMessage();
                resultSP.ex = ex;
            }
            return resultSP;
        });
        return result;
    }
}

package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Colonia;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ColoniaDAOImplementation implements IColoniaDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result GetByIdMunicipio(int IdMunicipio) {
        Result result = jdbcTemplate.execute("{CALL ColoniaGETBYMunicipio(?, ?)}", (CallableStatementCallback<Result>) callableStatement -> {
            Result resultSP = new Result();
            try {
                callableStatement.setInt(1, IdMunicipio);
                callableStatement.registerOutParameter(2, java.sql.Types.REF_CURSOR);
                callableStatement.execute();
                ResultSet rs = (ResultSet) callableStatement.getObject(2);
                resultSP.objects = new ArrayList<>();
                while(rs.next()){
                    Colonia colonia = new Colonia();
                    colonia.setIdColonia(rs.getInt("IdColonia"));
                    colonia.setNombre(rs.getString("Nombre"));
                    resultSP.objects.add(colonia);
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


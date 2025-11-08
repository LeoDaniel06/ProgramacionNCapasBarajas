package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Pais;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import java.sql.ResultSet;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PaisDAOImplementation implements IPaisDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Result GETALL() {
        Result result = jdbcTemplate.execute("{CALL PaisGETALL(?)}", (CallableStatementCallback<Result>) callableStatement -> {
            Result resultSP = new Result();
            try {
                callableStatement.registerOutParameter(1, java.sql.Types.REF_CURSOR);
                callableStatement.execute();
                ResultSet rs = (ResultSet) callableStatement.getObject(1);
                resultSP.objects = new ArrayList<>();
                while(rs.next()){
                    Pais pais = new Pais();
                    pais.setIdPais(rs.getInt("IdPais"));
                    pais.setNombre(rs.getString("Nombre"));
                    resultSP.objects.add(pais);
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

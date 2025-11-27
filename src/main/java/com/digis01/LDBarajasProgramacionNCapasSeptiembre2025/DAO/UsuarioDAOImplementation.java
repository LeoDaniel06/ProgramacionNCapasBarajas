package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Colonia;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Direccion;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Estado;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Municipio;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Pais;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Rol;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Usuario;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UsuarioDAOImplementation implements IUsuarioDAO {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ColoniaDAOImplementation coloniaDAOImplementation;

//---------------------------CARGA MASIVA-------------------------------------------------
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result AddALL(List<Usuario> usuarios) {
        Result result = new Result();

        try {
            jdbcTemplate.batchUpdate("{CALL CargaMasiva(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    usuarios,
                    usuarios.size(),
                    (callableStatement, usuario) -> {

                        callableStatement.setString(1, usuario.getUserName());
                        callableStatement.setString(2, usuario.getNombreUsuario());
                        callableStatement.setString(3, usuario.getApellidoPat());
                        callableStatement.setString(4, usuario.getApellidoMat());
                        callableStatement.setString(5, usuario.getEmail());
                        callableStatement.setString(6, usuario.getPassword());

                        if (usuario.getFechaNacimiento() != null) {
                            callableStatement.setDate(7, new java.sql.Date(usuario.getFechaNacimiento().getTime()));
                        } else {
                            callableStatement.setNull(7, java.sql.Types.DATE);
                        }

                        callableStatement.setString(8, usuario.getSexo());
                        callableStatement.setString(9, usuario.getTelefono());
                        callableStatement.setString(10, usuario.getCelular());
                        callableStatement.setString(11, usuario.getCurp());
                        callableStatement.setInt(12, usuario.getRol() != null ? usuario.getRol().getIdRol() : 0);

                    });

            result.correct = true;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }
//---------------------------------------------------GETALL--------------------------------------------------------------------------

    @Override
    public Result GETALL() {
        Result result = jdbcTemplate.execute("{CALL DireccionGETALL(?)}", (CallableStatementCallback<Result>) callableStatement -> {
            Result resultSP = new Result();
            try {
                callableStatement.registerOutParameter(1, Types.REF_CURSOR);
                callableStatement.execute();

                ResultSet resultSet = (ResultSet) callableStatement.getObject(1);
                resultSP.objects = new ArrayList<>();

                int idUsuario = 0;

                while (resultSet.next()) {

                    idUsuario = resultSet.getInt("idUsuario");
                    if (!resultSP.objects.isEmpty() && idUsuario == ((Usuario) (resultSP.objects.get(resultSP.objects.size() - 1))).getIdUsuario()) {

                        Direccion direccion = new Direccion();
                        direccion.setCalle(resultSet.getString("Calle"));
                        direccion.setNumeroInterior(resultSet.getString("NumeroInterior"));
                        direccion.setNumeroExterior(resultSet.getString("NumeroExterior"));
                        Usuario usuario = ((Usuario) (resultSP.objects.get(resultSP.objects.size() - 1)));
                        usuario.Direcciones.add(direccion);
                    } else {
                        Usuario usuario = new Usuario();
                        usuario.setIdUsuario(resultSet.getInt("idUsuario"));
                        usuario.setuserName(resultSet.getString("UserName"));
                        usuario.setNombreUsuario(resultSet.getString("NombreUsuario"));
                        usuario.setApellidoPat(resultSet.getString("ApellidoPat"));
                        usuario.setApellidoMat(resultSet.getString("ApellidoMat"));
                        usuario.setSexo(resultSet.getString("Sexo"));
                        usuario.setCurp(resultSet.getString("Curp"));
                        usuario.setFechaNacimiento(resultSet.getDate("FechaNacimiento"));
                        usuario.setEmail(resultSet.getString("Email"));
                        usuario.setTelefono(resultSet.getString("Telefono"));
                        usuario.setCelular(resultSet.getString("Celular"));
                        usuario.Direcciones = new ArrayList<>();

                        Direccion direccion = new Direccion();

                        direccion.setCalle(resultSet.getString("Calle"));
                        direccion.setNumeroInterior(resultSet.getString("NumeroInterior"));
                        direccion.setNumeroExterior(resultSet.getString("NumeroExterior"));

                        usuario.Direcciones.add(direccion);
                        resultSP.objects.add(usuario);
                    }
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

//    --------------------------------------------GETBYID------------------------------------------------------------------------------
    @Override
    public Result GETBYID(int idUsuario) {
        Result result = new Result();
        try {
            jdbcTemplate.execute(con -> {
                CallableStatement callableStatement = con.prepareCall("{CALL DireccionGETBYID(?, ?, ?)}");
                callableStatement.setInt(1, idUsuario);
                callableStatement.registerOutParameter(2, Types.REF_CURSOR); // Cursor para Usuario
                callableStatement.registerOutParameter(3, Types.REF_CURSOR); // Cursor para Direcciones
                return callableStatement;
            }, (CallableStatementCallback<Object>) CallableStatement -> {
                CallableStatement.execute();

                ResultSet rsUsuario = (ResultSet) CallableStatement.getObject(2);
                Usuario usuario = new Usuario();

                if (rsUsuario.next()) {
                    usuario.setIdUsuario(rsUsuario.getInt("idUsuario"));
                    usuario.setuserName(rsUsuario.getString("UserName"));
                    usuario.setNombreUsuario(rsUsuario.getString("NombreUsuario"));
                    usuario.setApellidoPat(rsUsuario.getString("ApellidoPat"));
                    usuario.setApellidoMat(rsUsuario.getString("ApellidoMat"));
                    usuario.setFechaNacimiento(rsUsuario.getDate("FechaNacimiento"));
                    usuario.setEmail(rsUsuario.getString("Email"));
                    usuario.setTelefono(rsUsuario.getString("Telefono"));
                    usuario.setCelular(rsUsuario.getString("Celular"));
                    usuario.setCurp(rsUsuario.getString("Curp"));
                    usuario.setSexo(rsUsuario.getString("Sexo"));
                    usuario.setImagen(rsUsuario.getString("Imagen"));
                    usuario.setPassword(rsUsuario.getString("Password"));

                    Rol rol = new Rol();
                    rol.setIdRol(rsUsuario.getInt("IdRol"));
                    rol.setNombre(rsUsuario.getString("NombreRol"));
                    usuario.setRol(rol);
                }

                // Cursor 2: Direcciones
                ResultSet rsDireccion = (ResultSet) CallableStatement.getObject(3);

                while (rsDireccion.next()) {
                    Direccion direccion = new Direccion();
                    direccion.setIdDireccion(rsDireccion.getInt("IdDireccion"));
                    direccion.setCalle(rsDireccion.getString("Calle"));
                    direccion.setNumeroInterior(rsDireccion.getString("NumeroInterior"));
                    direccion.setNumeroExterior(rsDireccion.getString("NumeroExterior"));

                    Colonia colonia = new Colonia();
                    colonia.setIdColonia(rsDireccion.getInt("IdColonia"));
                    colonia.setNombre(rsDireccion.getString("NombreColonia"));
                    colonia.setCodigoPostal(rsDireccion.getString("CodigoPostal"));

                    Municipio municipio = new Municipio();
                    municipio.setIdMunicipio(rsDireccion.getInt("IdMunicipio"));
                    municipio.setNombre(rsDireccion.getString("NombreMunicipio"));

                    Estado estado = new Estado();
                    estado.setIdEstado(rsDireccion.getInt("IdEstado"));
                    estado.setNombre(rsDireccion.getString("NombreEstado"));

                    Pais pais = new Pais();
                    pais.setIdPais(rsDireccion.getInt("IdPais"));
                    pais.setNombre(rsDireccion.getString("NombrePais"));

                    estado.setPais(pais);
                    municipio.setEstado(estado);
                    colonia.setMunicipio(municipio);
                    direccion.setColonia(colonia);

                    usuario.addDireccion(direccion);
                }

                rsUsuario.close();
                rsDireccion.close();

                result.object = usuario;
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }
//------------------------------------------------------USUARIO ADD----------------------------------------------

    @Override
    public Result ADD(Usuario usuario) {
        Result result = new Result();

        try {
            result.correct = jdbcTemplate.execute("CALL UsuarioADD(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", (CallableStatementCallback<Boolean>) callableStatement -> {

                callableStatement.setString(1, usuario.getUserName());
                callableStatement.setString(2, usuario.getNombreUsuario());
                callableStatement.setString(3, usuario.getApellidoPat());
                callableStatement.setString(4, usuario.getApellidoMat());
                callableStatement.setString(5, usuario.getEmail());
                callableStatement.setString(6, usuario.getPassword());
                callableStatement.setDate(7, new java.sql.Date(usuario.getFechaNacimiento().getTime()));
                callableStatement.setString(8, usuario.getSexo());
                callableStatement.setString(9, usuario.getTelefono());
                callableStatement.setString(10, usuario.getCelular());
                callableStatement.setString(11, usuario.getCurp());
                callableStatement.setInt(12, usuario.Rol.getIdRol());
                callableStatement.setString(13, usuario.Direcciones.get(0).getCalle());
                callableStatement.setString(14, usuario.Direcciones.get(0).getNumeroInterior());
                callableStatement.setString(15, usuario.Direcciones.get(0).getNumeroExterior());
                callableStatement.setInt(16, usuario.getDirecciones().get(0).getColonia().getIdColonia());
                callableStatement.setString(17, usuario.getImagen());
                callableStatement.execute();
                return true;
            });
        } catch (Exception ex) {
            result.errorMessage = ex.getLocalizedMessage();
        }

        return result;
    }
//    --------------------------------------------DIRECCION ADD---------------------------------------------------------------

    @Override
    public Result DireccionADD(Direccion direccion, int usuario) {
        Result result = new Result();

        try {
            jdbcTemplate.execute(con -> {
                CallableStatement callableStatement = con.prepareCall("{CALL DireccionADD(?, ?, ?, ?, ?)}");
                callableStatement.setString(1, direccion.getCalle());
                callableStatement.setString(2, direccion.getNumeroInterior());
                callableStatement.setString(3, direccion.getNumeroExterior());
                callableStatement.setInt(4, direccion.getColonia().getIdColonia());
                callableStatement.setInt(5, usuario);
                return callableStatement;
            }, (CallableStatementCallback<Object>) callableStatement -> {
                callableStatement.execute();
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }

//    --------------------------------------------UPDATE USUARIO------------------------------------------------------------------------------
    @Override
    public Result UsuarioUPDATE(Usuario usuario) {
        Result result = new Result();

        try {
            jdbcTemplate.execute(con -> {
                CallableStatement callableStatement = con.prepareCall("{CALL UsuarioUpdate(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}");
                callableStatement.setInt(1, usuario.getIdUsuario());
                callableStatement.setString(2, usuario.getUserName());
                callableStatement.setString(3, usuario.getNombreUsuario());
                callableStatement.setString(4, usuario.getApellidoPat());
                callableStatement.setString(5, usuario.getApellidoMat());
                callableStatement.setString(6, usuario.getEmail());
                callableStatement.setDate(7, new java.sql.Date(usuario.getFechaNacimiento().getTime())); // si usas Date
                callableStatement.setString(8, usuario.getSexo());
                callableStatement.setString(9, usuario.getTelefono());
                callableStatement.setString(10, usuario.getCelular());
                callableStatement.setString(11, usuario.getCurp());
                callableStatement.setInt(12, usuario.getRol().getIdRol());
                return callableStatement;
            }, (CallableStatementCallback<Object>) callableStatement -> {
                callableStatement.execute();
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }
//    --------------------------------------------UPDATE DIRECCION------------------------------------------------------------------------------

    @Override
    public Result DireccionUPDATE(Direccion direccion, int idUsuario) {
        Result result = new Result();

        try {
            jdbcTemplate.execute(con -> {
                // Ahora el CALL tiene 6 parámetros
                CallableStatement callableStatement = con.prepareCall("{CALL DireccionUPDATE(?, ?, ?, ?, ?, ?)}");

                // 1. IdDireccion
                callableStatement.setInt(1, direccion.getIdDireccion());
                // 2. IdUsuario
                callableStatement.setInt(2, idUsuario);
                // 3. Calle
                callableStatement.setString(3, direccion.getCalle());
                // 4. NumeroInterior
                callableStatement.setString(4, direccion.getNumeroInterior());
                // 5. NumeroExterior
                callableStatement.setString(5, direccion.getNumeroExterior());
                // 6. IdColonia
                callableStatement.setInt(6, direccion.getColonia().getIdColonia());

                return callableStatement;
            }, (CallableStatementCallback<Object>) callableStatement -> {
                callableStatement.execute();
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }
//--------------------------------------------------USUARIO DELETE---------------------------------------------------------

    @Override
    public Result UsuarioDELETE(int idUsuario) {
        Result result = new Result();

        try {
            jdbcTemplate.execute(con -> {
                CallableStatement callableStatement = con.prepareCall("{CALL UsuarioDELETE(?)}");
                callableStatement.setInt(1, idUsuario);
                return callableStatement;
            }, (CallableStatementCallback<Object>) callableStatement -> {
                callableStatement.execute();
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }
//-------------------------------------DireccionGETBYIDDirecion----------------------------------------------------------

    @Override
    public Result GetDireccionByIdDireccion(int idDireccion) {
        Result result = new Result();

        try {
            result = jdbcTemplate.execute("{call GETBYIDDireccion(?,?,?,?,?,?)}",
                    (CallableStatementCallback<Result>) cs -> {
                        cs.setInt(1, idDireccion);
                        cs.registerOutParameter(2, Types.VARCHAR); // Calle
                        cs.registerOutParameter(3, Types.VARCHAR); // Número Interior
                        cs.registerOutParameter(4, Types.VARCHAR); // Número Exterior
                        cs.registerOutParameter(5, Types.INTEGER); // IdColonia
                        cs.registerOutParameter(6, Types.INTEGER); // IdUsuario

                        cs.execute();

                        Direccion direccion = new Direccion();
                        direccion.setIdDireccion(idDireccion);
                        direccion.setCalle(cs.getString(2));
                        direccion.setNumeroInterior(cs.getString(3));
                        direccion.setNumeroExterior(cs.getString(4));

                        int idColonia = cs.getInt(5);
                        if (!cs.wasNull()) {
                            // Asignar colonia básica
                            Colonia colonia = new Colonia();
                            colonia.setIdColonia(idColonia);
                            direccion.setColonia(colonia);

                            // Obtener colonia completa si existe DAO
                            try {
                                Result resultColonia = coloniaDAOImplementation.GetByIdMunicipio(idDireccion);
                                if (resultColonia.correct && resultColonia.object != null) {
                                    direccion.setColonia((Colonia) resultColonia.object);
                                }
                            } catch (Exception e) {
                                System.out.println("⚠️ No se pudo obtener la colonia completa: " + e.getMessage());
                            }
                        }

                        Result res = new Result();
                        res.object = direccion;
                        res.correct = true;
                        return res;
                    });

        } catch (Exception e) {
            result.correct = false;
            result.errorMessage = "Error al ejecutar GETBYIDDireccion: " + e.getMessage();
        }

        return result;
    }
//------------------------------------------DIRECCION DELETE-------------------------------------------------------------------

    @Override
    public Result DireccionDELETE(int idDireccion) {
        Result result = new Result();
        try {
            jdbcTemplate.execute(con -> {
                CallableStatement cs = con.prepareCall("{CALL DireccionDELETE(?)}");
                cs.setInt(1, idDireccion);
                return cs;
            }, (CallableStatementCallback<Object>) cs -> {
                cs.execute();
                result.correct = true; // Si no hubo excepción, se eliminó
                return result;
            });
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }
        return result;
    }
//--------------------------------------------------Update Imagen------------------------------------------------------------------

    @Override
    public Result UsuarioUPDATEImagen(int idUsuario, String imagenBase) {
        Result result = new Result();

        try {
            jdbcTemplate.execute(con -> {
                CallableStatement callableStatement = con.prepareCall("{CALL UsuarioUpdateImagen(?, ?)}");

                // 1. IdUsuario
                callableStatement.setInt(1, idUsuario);

                // 2. Imagen (en Base64)
                callableStatement.setString(2, imagenBase);

                return callableStatement;
            }, (CallableStatementCallback<Object>) callableStatement -> {
                callableStatement.execute();
                result.correct = true;
                return result;
            });

        } catch (Exception ex) {
            result.correct = true;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }

//----------------------------------------BUSQUEDA DINAMICA FILTRADA----------------------------------------------------
    @Override
    public Result BusquedaDinamica(Usuario usuario) {
        return jdbcTemplate.execute("CALL UsuarioBusquedaDinamica(?,?,?,?,?)", (CallableStatementCallback<Result>) callableStatement -> {
            Result result = new Result();
            try {
                callableStatement.setString(1, usuario.getNombreUsuario());
                callableStatement.setString(2, usuario.getApellidoPat());
                callableStatement.setString(3, usuario.getApellidoMat());
                callableStatement.setInt(4, usuario.Rol.getIdRol());
                callableStatement.registerOutParameter(5, java.sql.Types.REF_CURSOR);
                callableStatement.execute();

                ResultSet resultSet = (ResultSet) callableStatement.getObject(5);
                result.objects = new ArrayList<>();
                int IdUsuario = 0;

                while (resultSet.next()) {
                    IdUsuario = resultSet.getInt("IdUsuario");
                    if (!result.objects.isEmpty() && IdUsuario == ((Usuario) (result.objects.get(result.objects.size() - 1))).getIdUsuario()) {

                        Direccion direccion = new Direccion();
                        direccion.setIdDireccion(resultSet.getInt("IdDireccion"));
                        direccion.setCalle(resultSet.getString("Calle"));
                        direccion.setNumeroInterior(resultSet.getString("NumeroInterior"));
                        direccion.setNumeroExterior(resultSet.getString("NumeroExterior"));

                        Colonia colonia = new Colonia();
                        colonia.setIdColonia(resultSet.getInt("IdColonia"));
                        colonia.setNombre(resultSet.getString("NombreColonia"));
                        colonia.setCodigoPostal(resultSet.getString("CodigoPostal"));

                        Municipio municipio = new Municipio();
                        municipio.setIdMunicipio(resultSet.getInt("IdMunicipio"));
                        municipio.setNombre(resultSet.getString("NombreMunicipio"));

                        Estado estado = new Estado();
                        estado.setIdEstado(resultSet.getInt("IdEstado"));
                        estado.setNombre(resultSet.getString("NombreEstado"));

                        Pais pais = new Pais();
                        pais.setIdPais(resultSet.getInt("IdPais"));
                        pais.setNombre(resultSet.getString("NombrePais"));

                        estado.setPais(pais);
                        municipio.setEstado(estado);
                        colonia.setMunicipio(municipio);
                        direccion.setColonia(colonia);

                        ((Usuario) (result.objects.get(result.objects.size() - 1))).Direcciones.add(direccion);
                    } else {
                        Usuario usuario2 = new Usuario();

                        usuario2.setIdUsuario(resultSet.getInt("idUsuario"));
                        usuario2.setuserName(resultSet.getString("UserName"));
                        usuario2.setNombreUsuario(resultSet.getString("NombreUsuario"));
                        usuario2.setApellidoPat(resultSet.getString("ApellidoPat"));
                        usuario2.setApellidoMat(resultSet.getString("ApellidoMat"));
                        usuario2.setEmail(resultSet.getString("Email"));
                        usuario2.setPassword(resultSet.getString("Password"));
                        usuario2.setFechaNacimiento(resultSet.getDate("FechaNacimiento"));
                        usuario2.setSexo(resultSet.getString("Sexo"));
                        usuario2.setTelefono(resultSet.getString("Telefono"));
                        usuario2.setCelular(resultSet.getString("Celular"));
                        usuario2.setCurp(resultSet.getString("Curp"));
                        usuario2.setImagen(resultSet.getString("Imagen"));

                        usuario2.Rol = new Rol();
                        usuario2.Rol.setIdRol(resultSet.getInt("IdRol"));
                        usuario2.Rol.setNombre(resultSet.getString("NombreRol"));

                        usuario2.Direcciones = new ArrayList<>();

                        Direccion direccion = new Direccion();
                        direccion.setIdDireccion(resultSet.getInt("IdDireccion"));
                        direccion.setCalle(resultSet.getString("Calle"));
                        direccion.setNumeroInterior(resultSet.getString("NumeroInterior"));
                        direccion.setNumeroExterior(resultSet.getString("NumeroExterior"));

                        Colonia colonia = new Colonia();
                        colonia.setIdColonia(resultSet.getInt("IdColonia"));
                        colonia.setNombre(resultSet.getString("NombreColonia"));
                        colonia.setCodigoPostal(resultSet.getString("CodigoPostal"));

                        Municipio municipio = new Municipio();
                        municipio.setIdMunicipio(resultSet.getInt("IdMunicipio"));
                        municipio.setNombre(resultSet.getString("NombreMunicipio"));

                        Estado estado = new Estado();
                        estado.setIdEstado(resultSet.getInt("IdEstado"));
                        estado.setNombre(resultSet.getString("NombreEstado"));

                        Pais pais = new Pais();
                        pais.setIdPais(resultSet.getInt("IdPais"));
                        pais.setNombre(resultSet.getString("NombrePais"));

                        estado.setPais(pais);
                        municipio.setEstado(estado);
                        colonia.setMunicipio(municipio);
                        direccion.setColonia(colonia);

                        usuario2.Direcciones.add(direccion);
                        result.objects.add(usuario2);
                    }

                }
                result.correct = true;
            } catch (Exception ex) {
                result.correct = false;
                result.errorMessage = ex.getLocalizedMessage();
                result.ex = ex;
            }
            return result;
        });
    }
}

package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA.ColoniaJPA;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA.DireccionJPA;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA.UsuarioJPA;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Direccion;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Result;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.stream.Collectors;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class UsuarioJPADAOImplementation implements IUsuarioJPA {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ModelMapper modelMapper;
//-----------------------------------------------------GETALL----------------------------------------------------------------------

    @Override
    public Result GetAll() {

        Result result = new Result();
        try {
            TypedQuery<UsuarioJPA> queryUsuario = entityManager.createQuery("FROM UsuarioJPA", UsuarioJPA.class);
            List<UsuarioJPA> usuariosJPA = queryUsuario.getResultList();
            List<Usuario> usuariosML = usuariosJPA.stream().map(usuario -> modelMapper.map(usuario, Usuario.class)).collect(Collectors.toList());
            result.objects = (List<Object>) (List<?>) usuariosML;
            result.correct = true;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }
        return result;
    }

//------------------------------------------------ADD---------------------------------------
    @Override
    @Transactional
    public Result Add(Usuario usuarioML) {
        Result result = new Result();
        try {
            UsuarioJPA usuarioJPA = modelMapper.map(usuarioML, UsuarioJPA.class);
            if (usuarioJPA.getDireccionesJPA() != null && !usuarioJPA.getDireccionesJPA().isEmpty()) {
                for (DireccionJPA direccion : usuarioJPA.getDireccionesJPA()) {
                    direccion.setUsuarioJPA(usuarioJPA);
                }
            }
            entityManager.persist(usuarioJPA);
            entityManager.flush();
            result.correct = true;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }

        return result;
    }
//    -------------------------------------------ADDDIRECCION----------------------------------------------------------
    @Override
    @Transactional
    public Result AddDireccion(Direccion direccionML, int idUsuario){
        Result result = new Result();
        
        try {
            //Mapea ml a jpa
            DireccionJPA direccionJPA = modelMapper.map(direccionML,DireccionJPA.class);
            //Busca y asiga idusuario
            UsuarioJPA usuarioJPA = entityManager.find(UsuarioJPA.class, idUsuario);
            direccionJPA.setUsuarioJPA(usuarioJPA);
            //busca y asigana colonia
            if(direccionML.getColonia() != null && direccionML.getColonia().getIdColonia()>0){
                ColoniaJPA coloniaJPA = entityManager.find(ColoniaJPA.class,direccionML.getColonia().getIdColonia());
                direccionJPA.setColoniaJPA(coloniaJPA);
            }
            //guaradar la direccion
            entityManager.persist(direccionJPA);
            entityManager.flush();
            result.correct = true;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getLocalizedMessage();
            result.ex = ex;
        }
        
        return result;
    }

//    -----------------------------------------UPDATE------------------------------------------------------------------
    @Override
    @Transactional
    public Result Update(Usuario usuarioML) {
        Result result = new Result();

        try {
            UsuarioJPA usuarioBase = entityManager.find(UsuarioJPA.class, usuarioML.getIdUsuario());
            if (usuarioBase == null) {
                result.correct = false;
                result.errorMessage = "Usuario no encontrado";
                return result;
            }
            UsuarioJPA usuarioJPA = modelMapper.map(usuarioML, UsuarioJPA.class);
            usuarioJPA.setImagen(usuarioBase.getImagen());
            usuarioJPA.setDireccionesJPA(usuarioBase.getDireccionesJPA());
            if (usuarioML.getRol() != null) {
                usuarioJPA.setRolJPA(usuarioJPA.getRolJPA());
            } else {
                usuarioJPA.setRolJPA(usuarioBase.getRolJPA());
            }
            entityManager.merge(usuarioJPA);
            entityManager.flush();
            result.correct = true;
        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }

//    --------------------------UPDATEDIRECCION ------------------------------------
    @Override
    @Transactional
    public Result DireccionUPDATE(Direccion direccionML, int idUsuario) {
        Result result = new Result();

        try {
            // Buscar la dirección existente
            DireccionJPA direccionBase = entityManager.find(DireccionJPA.class, direccionML.getIdDireccion());

            if (direccionBase == null) {
                result.correct = false;
                result.errorMessage = "Dirección no encontrada";
                return result;
            }

            // Mapear los nuevos datos del formulario
            DireccionJPA direccionJPA = modelMapper.map(direccionML, DireccionJPA.class);

            // Mantener la relación con el usuario
            UsuarioJPA usuarioJPA = entityManager.find(UsuarioJPA.class, idUsuario);
            direccionJPA.setUsuarioJPA(usuarioJPA);

            // Mantener el mismo idDireccion (clave primaria)
            direccionJPA.setIdDireccion(direccionBase.getIdDireccion());

            // Actualizar la colonia si fue seleccionada
            if (direccionML.getColonia() != null && direccionML.getColonia().getIdColonia() > 0) {
                ColoniaJPA coloniaJPA = entityManager.find(ColoniaJPA.class, direccionML.getColonia().getIdColonia());
                direccionJPA.setColoniaJPA(coloniaJPA);
            } else {
                direccionJPA.setColoniaJPA(direccionBase.getColoniaJPA());
            }

            // Guardar cambios
            entityManager.merge(direccionJPA);
            entityManager.flush();

            result.correct = true;

        } catch (Exception ex) {
            result.correct = false;
            result.errorMessage = ex.getMessage();
            result.ex = ex;
        }

        return result;
    }
    
//    -------------------------------------DELETE USUARIO---------------------------------------------------
    

}

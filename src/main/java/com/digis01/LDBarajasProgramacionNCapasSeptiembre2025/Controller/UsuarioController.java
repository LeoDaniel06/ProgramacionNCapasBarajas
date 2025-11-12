package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Controller;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.DAO.*;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.*;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Service.ValidationService;
import jakarta.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioDAOImplementation usuarioDAOImplementation;
    @Autowired
    private RolDAOImplementation rolDAOImplementation;
    @Autowired
    private PaisDAOImplementation paisDAOImplementation;
    @Autowired
    private EstadoDAOImplementation estadoDAOImplementation;
    @Autowired
    private MunicipioDAOImplementation municipioDAOImplementation;
    @Autowired
    private ColoniaDAOImplementation coloniaDAOImplementation;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private UsuarioJPADAOImplementation usuarioJPADAOImplementation;
//------------------------------------------------------USUARIO INDEX-----------------------------------------------------

    @GetMapping
    public String Index(Model model) {
        Result result = usuarioDAOImplementation.GETALL();
        Result resultJPA = usuarioJPADAOImplementation.GetAll();
        model.addAttribute("usuarios", resultJPA.objects);
        model.addAttribute("Roles", rolDAOImplementation.GETALL().objects);
        model.addAttribute("Usuario", new Usuario());
        return "UsuarioIndex";
    }
//----------------------CARGA MASIVA--------------------------

    @GetMapping("/carga")
    public String mostrarCargaMasiva(Model model) {
        return "UsuarioCargaMasiva";
    }

    @PostMapping("/procesar")
    public String procesarUsuarios(HttpSession session, Model model) {

        // Recuperar solo el nombre del archivo guardado en la carga
        String nombreArchivo = (String) session.getAttribute("nombreArchivoTemp");

        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            model.addAttribute("mensajeError", "No se encontró el archivo a procesar. Primero debe cargarlo.");
            return "UsuarioCargaMasiva";
        }

        List<Usuario> usuarios = new ArrayList<>();

        try {
            String pathBase = System.getProperty("user.dir");
            String pathArchivo = pathBase + "/src/main/resources/ArchivosCarga/" + nombreArchivo;
            File archivo = new File(pathArchivo);

            if (!archivo.exists()) {
                model.addAttribute("mensajeError", "El archivo no existe en el servidor: " + nombreArchivo);
                return "UsuarioCargaMasiva";
            }

            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();

            switch (extension) {
                case "txt" ->
                    usuarios = leerArchivoTXT(archivo);
                case "xlsx" ->
                    usuarios = leerArchivoXLSX(archivo);
                default -> {
                    model.addAttribute("mensajeError", "Extensión no soportada: " + extension);
                    return "UsuarioCargaMasiva";
                }
            }

            if (usuarios.isEmpty()) {
                model.addAttribute("mensajeError", "El archivo no contiene usuarios válidos para insertar.");
                return "UsuarioCargaMasiva";
            }

            // Insertar directamente
            Result result = usuarioDAOImplementation.AddALL(usuarios);

            if (result.correct) {
                model.addAttribute("mensajeExito", "Usuarios procesados correctamente desde el archivo '" + nombreArchivo + "'.");
            } else {
                model.addAttribute("mensajeError", "Error al insertar los datos: " + result.errorMessage);
            }

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensajeError", "Error al procesar el archivo: " + e.getMessage());
        }

        // Limpiar la sesión (opcional)
        session.removeAttribute("nombreArchivoTemp");

        return "UsuarioCargaMasiva";
    }

    @PostMapping("/carga")
    public String procesarCarga(@RequestParam("archivo") MultipartFile archivo,
            Model model,
            HttpSession session) throws IOException {

        if (archivo == null || archivo.isEmpty()) {
            model.addAttribute("mensajeError", "Debe seleccionar un archivo para cargar.");
            return "UsuarioCargaMasiva";
        }

        String nombreArchivo = archivo.getOriginalFilename();
        if (nombreArchivo == null) {
            model.addAttribute("mensajeError", "El archivo cargado no es válido.");
            return "UsuarioCargaMasiva";
        }

        String extension = nombreArchivo.contains(".")
                ? nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1)
                : "";

        // Ruta destino
        String pathBase = System.getProperty("user.dir");
        String pathCarpeta = "src/main/resources/ArchivosCarga";
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        File carpetaDestino = new File(pathBase + "/" + pathCarpeta);
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();
        }

        String nombreFinal = fecha + "_" + nombreArchivo;
        String pathDefinitivo = pathBase + "/" + pathCarpeta + "/" + nombreFinal;

        archivo.transferTo(new File(pathDefinitivo));

        List<Usuario> usuarios = new ArrayList<>();
        List<ErrorCarga> errores = new ArrayList<>();

        switch (extension.toLowerCase()) {
            case "txt" -> {
                usuarios = leerArchivoTXT(new File(pathDefinitivo));
                errores = validarDatosArchivo(usuarios);
            }
            case "xlsx" -> {
                usuarios = leerArchivoXLSX(new File(pathDefinitivo));
                errores = validarDatosArchivo(usuarios);
            }
            default -> {
                model.addAttribute("mensajeError", "Extensión no soportada. Solo se permiten archivos .txt o .xlsx");
                return "UsuarioCargaMasiva";
            }
        }

        // Si hay errores: mostrar en vista, limpiar sesión
        if (!errores.isEmpty()) {
            session.removeAttribute("usuariosTemp");
            session.removeAttribute("nombreArchivoTemp");

            model.addAttribute("errores", errores);
            model.addAttribute("mensajeError", "Se encontraron errores en la validación del archivo.");
        } // Si todo está bien: guardar en sesión
        else {
            session.setAttribute("usuariosTemp", usuarios);
            session.setAttribute("nombreArchivoTemp", nombreFinal);

            model.addAttribute("usuarios", usuarios);
            model.addAttribute("mensajeExito", "Archivo cargado correctamente. Puede procesar los datos.");
        }

        model.addAttribute("nombreArchivo", nombreFinal);
        return "UsuarioCargaMasiva";
    }

    public List<ErrorCarga> validarDatosArchivo(List<Usuario> usuarios) {
        List<ErrorCarga> erroresCarga = new ArrayList<>();
        if (usuarios == null) {
            return erroresCarga;
        }

        int lineaError = 0;
        for (Usuario usuario : usuarios) {
            lineaError++;
            BindingResult bindingResult = validationService.validateObject(usuario);
            List<ObjectError> errors = bindingResult.getAllErrors();

            for (ObjectError error : errors) {
                if (error instanceof FieldError fieldError) {
                    ErrorCarga errorCarga = new ErrorCarga();
                    errorCarga.setCampo(fieldError.getField());
                    errorCarga.setDescripcion(fieldError.getDefaultMessage());
                    errorCarga.setLinea(lineaError);
                    erroresCarga.add(errorCarga);
                }
            }
        }
        return erroresCarga;
    }

    public List<Usuario> leerArchivoTXT(File archivo) {
        List<Usuario> usuarios = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] campos = linea.split("\\|");

                // Validar número de campos
                if (campos.length < 12) {
                    continue;
                }

                Usuario usuario = new Usuario();
                usuario.setUserName(campos[0]);
                usuario.setNombreUsuario(campos[1]);
                usuario.setApellidoPat(campos[2]);
                usuario.setApellidoMat(campos[3]);
                usuario.setEmail(campos[4]);
                usuario.setPassword(campos[5]);

                // Parsear fecha
                Date fechaFormateada = new SimpleDateFormat("yyyy/MM/dd").parse(campos[6]);
                usuario.setFechaNacimiento(fechaFormateada);

                usuario.setSexo(campos[7]);
                usuario.setTelefono(campos[8]);
                usuario.setCelular(campos[9]);
                usuario.setCurp(campos[10]);

                Rol rol = new Rol();
                rol.setIdRol(Integer.parseInt(campos[11]));
                usuario.setRol(rol);

                usuarios.add(usuario);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return usuarios;
    }

    public List<Usuario> leerArchivoXLSX(File archivo) {
        List<Usuario> usuarios = new ArrayList<>();

        try (InputStream fileImputStream = new FileInputStream(archivo); XSSFWorkbook workbook = new XSSFWorkbook(fileImputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            boolean encabezadoDetectado = false;

            int rowIndex = 0;
            for (Row row : sheet) {
                if (row == null) {
                    rowIndex++;
                    continue;
                }
                // Detectar si es encabezado (solo la primera fila)
                if (rowIndex == 0 && esEncabezado(row)) {
                    encabezadoDetectado = true;
                    rowIndex++;
                    continue;
                }
                Usuario usuario = new Usuario();

                usuario.setUserName(getCellValue(row.getCell(0)));
                usuario.setNombreUsuario(getCellValue(row.getCell(1)));
                usuario.setApellidoPat(getCellValue(row.getCell(2)));
                usuario.setApellidoMat(getCellValue(row.getCell(3)));
                usuario.setEmail(getCellValue(row.getCell(4)));
                usuario.setPassword(getCellValue(row.getCell(5)));

                Cell cellFecha = row.getCell(6);
                if (cellFecha != null) {
                    if (DateUtil.isCellDateFormatted(cellFecha)) {
                        usuario.setFechaNacimiento(cellFecha.getDateCellValue());
                    } else {
                        try {
                            String fechaStr = getCellValue(cellFecha);
                            if (!fechaStr.isBlank()) {
                                LocalDate fecha = LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                                usuario.setFechaNacimiento(java.sql.Date.valueOf(fecha));
                            }
                        } catch (Exception e) {
                            usuario.setFechaNacimiento(null);
                        }
                    }
                }

                usuario.setSexo(getCellValue(row.getCell(7)));
                usuario.setTelefono(getCellValue(row.getCell(8)));
                usuario.setCelular(getCellValue(row.getCell(9)));
                usuario.setCurp(getCellValue(row.getCell(10)));

                Rol rol = new Rol();
                String rolStr = getCellValue(row.getCell(11));
                try {
                    rol.setIdRol(Integer.parseInt(rolStr));
                } catch (Exception e) {
                    rol.setIdRol(0);
                }
                usuario.setRol(rol);

                // Evitar filas completamente vacías
                if (!estaFilaVacia(usuario)) {
                    usuarios.add(usuario);
                }

                rowIndex++;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return usuarios;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING ->
                cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    double value = cell.getNumericCellValue();
                    yield (value == Math.floor(value)) ? String.valueOf((long) value) : String.valueOf(value);
                }
            }
            case BOOLEAN ->
                String.valueOf(cell.getBooleanCellValue());
            case FORMULA ->
                cell.getCellFormula();
            default ->
                "";
        };
    }

    private boolean esEncabezado(Row row) {
        String[] posiblesCampos = {
            "username", "nombre", "apellidopat", "apellidomat",
            "email", "password", "fecha", "sexo", "telefono", "celular", "curp", "rol"
        };

        // Leer las primeras celdas como texto y comparar
        for (int i = 0; i < 5 && i < row.getLastCellNum(); i++) {
            String valor = getCellValue(row.getCell(i)).toLowerCase();
            for (String campo : posiblesCampos) {
                if (valor.contains(campo)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean estaFilaVacia(Usuario usuario) {
        return (usuario.getUserName() == null || usuario.getUserName().isBlank())
                && (usuario.getNombreUsuario() == null || usuario.getNombreUsuario().isBlank())
                && (usuario.getEmail() == null || usuario.getEmail().isBlank());
    }

//------------------------------------------------------USUARIODETAIL----------------------------------------------------------
    @GetMapping("/detail/{id}")
    public String getUsuarioDetail(@PathVariable int id, Model model) {
        Result result = usuarioDAOImplementation.GETBYID(id);

        if (result.correct && result.object != null) {
            Usuario usuario = (Usuario) result.object;
            model.addAttribute("rol", rolDAOImplementation.GETALL().objects);
            model.addAttribute("usuario", usuario);
            model.addAttribute("direccion", new Direccion());
        } else {
            model.addAttribute("usuario", new Usuario());
        }
        model.addAttribute("paises", paisDAOImplementation.GETALL().objects);
        return "UsuarioDetail";
    }

//---------------------------------------------------------USUARIOFORM-----------------------------------------------------------
    @GetMapping("/add")
    public String ADD(Model model) {
        Usuario usuario = new Usuario();
//        usuario.setIdUsuario(0);
//        usuario.Direcciones = new ArrayList<>();
//        usuario.Direcciones.add(new Direccion());

        model.addAttribute("Usuario", usuario);
        model.addAttribute("roles", rolDAOImplementation.GETALL().objects);
        model.addAttribute("paises", paisDAOImplementation.GETALL().objects);

        return "UsuarioForm";
    }

    @PostMapping("/add")
    public String ADD(@Valid
            @ModelAttribute("Usuario") Usuario usuario,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes,
            @RequestParam("imagenFile") MultipartFile imagenFile
    ) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", rolDAOImplementation.GETALL().objects);
            model.addAttribute("paises", paisDAOImplementation.GETALL().objects);

            if (usuario.Direcciones.get(0).getColonia().getMunicipio().getEstado().getPais().getIdPais() > 0) {
                model.addAttribute("estados", estadoDAOImplementation.GetByIdPais(
                        usuario.Direcciones.get(0).getColonia().getMunicipio().getEstado().getPais().getIdPais()).objects);

                if (usuario.Direcciones.get(0).getColonia().getMunicipio().getEstado().getIdEstado() > 0) {
                    model.addAttribute("municipios", municipioDAOImplementation.GetByIdEstado(
                            usuario.Direcciones.get(0).getColonia().getMunicipio().getEstado().getIdEstado()
                    ).objects);

                    if (usuario.Direcciones.get(0).getColonia().getMunicipio().getIdMunicipio() > 0) {
                        model.addAttribute("colonias", coloniaDAOImplementation.GetByIdMunicipio(
                                usuario.Direcciones.get(0).getColonia().getMunicipio().getIdMunicipio()
                        ).objects);
                    }
                }
            }

            return "UsuarioForm";
        }

        try {
            if (imagenFile != null && !imagenFile.isEmpty()) {
                byte[] bytes = imagenFile.getBytes();
                String base64Image = Base64.getEncoder().encodeToString(bytes);
                usuario.setImagen(base64Image);
            } else {
                usuario.setImagen(null);
            }
            Result result = usuarioJPADAOImplementation.Add(usuario);

        } catch (IOException ex) {
            Logger.getLogger(UsuarioController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return "redirect:/usuario";
    }
//---------------------------------------------------------DDLS DIRECCION-----------------------------------------------------------------

    @GetMapping("/Estados/{IdPais}")
    @ResponseBody
    public Result EstadosGETBYIDPais(@PathVariable int IdPais) {
        Result result = estadoDAOImplementation.GetByIdPais(IdPais);
        if (result.objects == null) {
            result.objects = new ArrayList<>();
        }
        return result;
    }

    @GetMapping("/Municipios/{IdEstado}")
    @ResponseBody
    public Result getMunicipiosByEstado(@PathVariable int IdEstado) {
        Result result = municipioDAOImplementation.GetByIdEstado(IdEstado);
        if (result.objects == null) {
            result.objects = new ArrayList<>();
        }
        return result;
    }

    @GetMapping("/Colonias/{IdMunicipio}")
    @ResponseBody
    public Result getColoniasByMunicipio(@PathVariable int IdMunicipio) {
        Result result = coloniaDAOImplementation.GetByIdMunicipio(IdMunicipio);
        if (result.objects == null) {
            result.objects = new ArrayList<>();
        }
        return result;
    }
//-----------------------------------------------------UPDATE USUARIO-----------------------------------------------------------------

    @PostMapping("/update")
    public String updateUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        Result resultUsuario = usuarioDAOImplementation.UsuarioUPDATE(usuario);

        if (resultUsuario.correct) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Se actualizaron los datos de " + usuario.getUserName());
        } else {
            redirectAttributes.addFlashAttribute("mensajeExito", "Error al actualizar datos: " + resultUsuario.errorMessage);
        }

        return "redirect:/usuario/detail/" + usuario.getIdUsuario();
    }
//    -------------------------------------------------------------ADD DIRECCION-------------------------------------------------

    @PostMapping("/direccion/add/{idUsuario}")
    public String addDireccion(@PathVariable int idUsuario,
            @ModelAttribute("direccion") Direccion direccion,
            RedirectAttributes redirectAttributes) {

        // Llamar al DAO para guardar la dirección
        Result result = usuarioDAOImplementation.DireccionADD(direccion, idUsuario);

        if (result.correct) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Dirección agregada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al agregar la dirección: " + result.errorMessage);
        }

        // Redirige de nuevo al detalle del usuario
        return "redirect:/usuario/detail/" + idUsuario;
    }
//--------------------------------------------------------UPDATE DIRECCION-------------------------------------------------

    @PostMapping("/direccion/update/{idUsuario}")
    public String updateDireccion(
            @PathVariable int idUsuario,
            @ModelAttribute("direccion") Direccion direccion,
            RedirectAttributes redirectAttributes) {

        if (direccion.getIdDireccion() <= 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se pudo actualizar la dirección: ID inválido.");
            return "redirect:/usuario/detail/" + idUsuario;
        }

        Result result = usuarioDAOImplementation.DireccionUPDATE(direccion, idUsuario);

        if (result.correct) {
            redirectAttributes.addFlashAttribute("mensajeExito", "Dirección actualizada correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar la dirección: " + result.errorMessage);
        }

        return "redirect:/usuario/detail/" + idUsuario;
    }

//------------------------------------------------------ELIMINAR USUARIO------------------------------------------------------
    @PostMapping("/delete")
    public String eliminarUsuario(@RequestParam int idUsuario, RedirectAttributes redirectAttributes) {
        Result result = usuarioDAOImplementation.UsuarioDELETE(idUsuario);

        if (result.correct) {
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + result.errorMessage);
        }

        return "redirect:/usuario";
    }

//    ---------------------------------------------------GETDIRECCIONBYIDDIRECCION---------------------------------------------------
    @GetMapping("direccion/{idDireccion}")
    @ResponseBody
    public Result GetDireccionByIdDireccion(@PathVariable("idDireccion") int idDireccion) {
        return usuarioDAOImplementation.GetDireccionByIdDireccion(idDireccion);
    }

//    ----------------------------------------------DIRECCION DELETE-------------------------------------------------------
    @PostMapping("/direccion/delete/{idUsuario}")
    public String deleteDireccion(@PathVariable int idUsuario, @RequestParam int idDireccion, RedirectAttributes redirectAttributes) {
        Result result = usuarioDAOImplementation.DireccionDELETE(idDireccion);

        if (result.correct) {
            redirectAttributes.addFlashAttribute("MensajeExito", "Dirección eliminada correctamente");
        } else {
            redirectAttributes.addFlashAttribute("MensajeError", "No se pudo eliminar la dirección: " + result.errorMessage);
        }

        return "redirect:/usuario/detail/" + idUsuario;
    }

//-------------------------------------IMAGEN UPDATE----------------------------------------------------------
    @PostMapping("/update-imagen")
    public String UpdateImagen(
            @RequestParam("idUsuario") int idUsuario,
            @RequestParam("imagen") MultipartFile file) {

        try {
            if (!file.isEmpty()) {
                byte[] bytes = file.getBytes();
                String imagenBase64 = Base64.getEncoder().encodeToString(bytes);

                Result result = usuarioDAOImplementation.UsuarioUPDATEImagen(idUsuario, imagenBase64);

                if (!result.correct) {
                    // Si falla, podrías enviar un mensaje de error o log
                    System.out.println("Error al actualizar la imagen: " + result.errorMessage);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/usuario/detail/" + idUsuario;
    }
//--------------------BUCADOR DINAMICO-----------------------------

    @PostMapping()
    public String BuscarUsuario(@ModelAttribute("Usuario") Usuario usuario, Model model) {
        Result result = usuarioDAOImplementation.BusquedaDinamica(usuario);
        model.addAttribute("usuarios", result.objects);
        model.addAttribute("Roles", rolDAOImplementation.GETALL().objects);
        model.addAttribute("Usuario", usuario);
        return "UsuarioIndex";
    }

}

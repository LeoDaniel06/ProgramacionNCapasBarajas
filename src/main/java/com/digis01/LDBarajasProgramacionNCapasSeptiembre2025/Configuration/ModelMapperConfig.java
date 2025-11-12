package com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.Configuration;

import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.JPA.UsuarioJPA;
import com.digis01.LDBarajasProgramacionNCapasSeptiembre2025.ML.Usuario;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.typeMap(Usuario.class, UsuarioJPA.class)
                .addMappings(mapper -> mapper.map(Usuario::getDirecciones,UsuarioJPA::setDireccionesJPA))
                .addMappings(mapper -> mapper.map(Usuario::getRol, UsuarioJPA::setRolJPA));
        return modelMapper;
    }
}
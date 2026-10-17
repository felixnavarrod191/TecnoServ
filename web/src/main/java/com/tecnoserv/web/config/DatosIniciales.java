package com.tecnoserv.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.tecnoserv.web.seguridad.Usuario;
import com.tecnoserv.web.seguridad.UsuarioRepository;


/** Al primer arranque crea los usuarios básicos para poder ingresar al sistema. */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DatosIniciales(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        try {
            if (usuarios.count() == 0) {
                crear("admin", "admin123", "Administrador del taller", "ADMINISTRADOR");
                crear("recepcion", "recepcion123", "Recepción", "RECEPCIONISTA");
                log.info("Usuarios iniciales creados: admin / recepcion");
            }
        } catch (Exception e) {
            log.warn("No se pudieron crear los usuarios iniciales: {}", e.getMessage());
        }
    }

    private void crear(String username, String clave, String nombre, String rol) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(clave));
        u.setNombre(nombre);
        u.setRol(rol);
        usuarios.save(u);
    }
}

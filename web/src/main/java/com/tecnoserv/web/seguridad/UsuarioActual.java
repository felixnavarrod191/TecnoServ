package com.tecnoserv.web.seguridad;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.tecnoserv.web.util.Fmt;

/** Usuario autenticado: además de las credenciales guarda su nombre, rol y el id al que está ligado. */
public class UsuarioActual extends User {

    private final String idUsuario;
    private final String nombre;
    private final String rol;
    private final String referenciaId;

    public UsuarioActual(Usuario u) {
        super(u.getUsername(), u.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_" + u.getRol())));
        this.idUsuario = u.getId();
        this.nombre = u.getNombre();
        this.rol = u.getRol();
        this.referenciaId = u.getReferenciaId();
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getRol() {
        return rol;
    }

    public String getReferenciaId() {
        return referenciaId;
    }

    public String getRolEtiqueta() {
        return new Fmt().rol(rol);
    }
}

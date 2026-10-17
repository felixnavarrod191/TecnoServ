package com.tecnoserv.ordenes.service;

import java.util.List;
import java.net.http.HttpClient;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnoserv.ordenes.error.ApiException;

/**
 * Cliente REST hacia los otros microservicios. Este servicio NUNCA lee las bases de datos
 * de los demas: solo les pregunta por HTTP (comunicacion RESTful entre servicios).
 */
@Component
public class Remotos {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {
    };

    private final RestClient clientes;
    private final RestClient tecnicos;
    private final RestClient repuestos;

    public Remotos(@Value("${servicios.clientes.url}") String urlClientes,
                   @Value("${servicios.tecnicos.url}") String urlTecnicos,
                   @Value("${servicios.repuestos.url}") String urlRepuestos) {
        this.clientes = crear(urlClientes);
        this.tecnicos = crear(urlTecnicos);
        this.repuestos = crear(urlRepuestos);
    }

    private static RestClient crear(String baseUrl) {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new ApiException(HttpStatus.valueOf(response.getStatusCode().value()), mensaje(response.getBody()));
                })
                .build();
    }

    private static String mensaje(java.io.InputStream cuerpo) {
        try {
            JsonNode nodo = MAPPER.readTree(cuerpo);
            if (nodo != null && nodo.hasNonNull("error")) {
                return nodo.get("error").asText();
            }
        } catch (Exception e) {
            // se ignora: se devuelve el mensaje generico
        }
        return "Error al consultar otro microservicio";
    }

    public Map<String, Object> cliente(String id) {
        return clientes.get().uri("/api/clientes/{id}", id).retrieve().body(MAPA);
    }

    public Map<String, Object> equipo(String id) {
        return clientes.get().uri("/api/equipos/{id}", id).retrieve().body(MAPA);
    }

    public Map<String, Object> tecnico(String id) {
        return tecnicos.get().uri("/api/tecnicos/{id}", id).retrieve().body(MAPA);
    }

    /** Tecnicos disponibles cuya especialidad coincide con el tipo de reparacion. */
    public List<Map<String, Object>> tecnicosDisponibles(String especialidad) {
        List<Map<String, Object>> lista = tecnicos.get()
                .uri("/api/tecnicos?especialidad={e}&disponible=true", especialidad)
                .retrieve().body(LISTA);
        return lista == null ? List.of() : lista;
    }

    /** Descuenta stock; si no hay existencias suficientes responde 409 (ApiException). */
    public Map<String, Object> descontarRepuesto(String repuestoId, int cantidad) {
        return repuestos.post()
                .uri("/api/repuestos/{id}/descontar", repuestoId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("cantidad", cantidad))
                .retrieve().body(MAPA);
    }
}

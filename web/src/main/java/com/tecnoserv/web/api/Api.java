package com.tecnoserv.web.api;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Pequeño cliente REST hacia UN microservicio. Los datos viajan como mapas (JSON),
 * así la web no necesita duplicar todas las clases del dominio.
 */
public class Api {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {
    };

    private final RestClient rest;

    public Api(String baseUrl) {
        this.rest = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    int codigo = response.getStatusCode().value();
                    if (codigo == 502 || codigo == 503 || codigo == 504) {
                        throw new ApiError("Un microservicio no está disponible en este momento. Intenta de nuevo en unos segundos.");
                    }
                    throw new ApiError(mensaje(response.getBody()));
                })
                .build();
    }

    public List<Map<String, Object>> lista(String uri, Object... vars) {
        List<Map<String, Object>> r = rest.get().uri(uri, vars).retrieve().body(LISTA);
        return r == null ? List.of() : r;
    }

    public Map<String, Object> obtener(String uri, Object... vars) {
        return rest.get().uri(uri, vars).retrieve().body(MAPA);
    }

    public Map<String, Object> crear(String uri, Object cuerpo, Object... vars) {
        return rest.post().uri(uri, vars).contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(MAPA);
    }

    public Map<String, Object> actualizar(String uri, Object cuerpo, Object... vars) {
        return rest.put().uri(uri, vars).contentType(MediaType.APPLICATION_JSON).body(cuerpo).retrieve().body(MAPA);
    }

    /** POST sin cuerpo (por ejemplo, registrar una entrega). */
    public Map<String, Object> accion(String uri, Object... vars) {
        return rest.post().uri(uri, vars).retrieve().body(MAPA);
    }

    public void eliminar(String uri, Object... vars) {
        rest.delete().uri(uri, vars).retrieve().toBodilessEntity();
    }

    private static String mensaje(InputStream cuerpo) {
        try {
            JsonNode nodo = MAPPER.readTree(cuerpo);
            if (nodo != null && nodo.hasNonNull("error")) {
                return nodo.get("error").asText();
            }
        } catch (Exception e) {
            // se ignora: mensaje genérico
        }
        return "El servicio respondió con un error";
    }
}

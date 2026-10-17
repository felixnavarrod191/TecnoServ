package com.tecnoserv.reportes.service;

import java.io.InputStream;
import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tecnoserv.reportes.error.ApiException;
import com.tecnoserv.reportes.model.Reporte;
import com.tecnoserv.reportes.model.Resumen;
import com.tecnoserv.reportes.repository.ReporteRepository;

/**
 * Regla 3: CostoTotal = (HorasTrabajadas x TarifaTecnico) + suma(cantidad x precioUnitario de cada repuesto).
 * Los datos de la orden se piden por REST a ms-ordenes (no se accede a su base de datos).
 */
@Service
public class ReporteService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {
    };
    private static final ParameterizedTypeReference<List<Map<String, Object>>> LISTA = new ParameterizedTypeReference<>() {
    };

    private final RestClient ordenes;
    private final ReporteRepository reportes;

    public ReporteService(@Value("${servicios.ordenes.url}") String urlOrdenes, ReporteRepository reportes) {
        this.reportes = reportes;
        this.ordenes = RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory(
                        HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build()))
                .baseUrl(urlOrdenes)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new ApiException(HttpStatus.valueOf(response.getStatusCode().value()), mensaje(response.getBody()));
                })
                .build();
    }

    public Reporte reporteDeOrden(String ordenId) {
        Map<String, Object> orden = ordenes.get().uri("/api/ordenes/{id}", ordenId).retrieve().body(MAPA);
        return calcular(orden);
    }

    public Resumen resumen() {
        List<Map<String, Object>> todas = ordenes.get().uri("/api/ordenes").retrieve().body(LISTA);
        List<Reporte> lista = (todas == null ? List.<Map<String, Object>>of() : todas).stream()
                .map(this::calcular)
                .toList();
        double total = lista.stream().mapToDouble(Reporte::getTotal).sum();
        return new Resumen(lista, redondear(total));
    }

    private Reporte calcular(Map<String, Object> orden) {
        String ordenId = String.valueOf(orden.get("id"));
        double horas = numero(orden.get("horasTrabajadas"));
        double tarifa = numero(orden.get("tarifaHora"));

        double repuestos = 0;
        if (orden.get("repuestosUsados") instanceof List<?> usados) {
            for (Object item : usados) {
                if (item instanceof Map<?, ?> r) {
                    repuestos += numero(r.get("cantidad")) * numero(r.get("precioUnitario"));
                }
            }
        }
        double manoObra = horas * tarifa;

        Reporte r = reportes.findByOrdenId(ordenId).orElseGet(Reporte::new);
        r.setOrdenId(ordenId);
        r.setClienteNombre(texto(orden.get("clienteNombre")));
        r.setEquipo(texto(orden.get("equipoDescripcion")));
        r.setTecnicoNombre(texto(orden.get("tecnicoNombre")));
        r.setEstado(texto(orden.get("estado")));
        r.setHorasTrabajadas(horas);
        r.setTarifaHora(tarifa);
        r.setManoObra(redondear(manoObra));
        r.setCostoRepuestos(redondear(repuestos));
        r.setTotal(redondear(manoObra + repuestos));
        r.setFechaGeneracion(LocalDateTime.now());
        return reportes.save(r);
    }

    private static double numero(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }

    private static String texto(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String mensaje(InputStream cuerpo) {
        try {
            JsonNode nodo = MAPPER.readTree(cuerpo);
            if (nodo != null && nodo.hasNonNull("error")) {
                return nodo.get("error").asText();
            }
        } catch (Exception e) {
            // se ignora: mensaje generico
        }
        return "Error al consultar el servicio de ordenes";
    }
}

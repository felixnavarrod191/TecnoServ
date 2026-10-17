package com.tecnoserv.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API Gateway: unica puerta de entrada a las APIs. La aplicacion web llama solo a este
 * servicio y el, segun la ruta, reenvia la peticion al microservicio correspondiente.
 */
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

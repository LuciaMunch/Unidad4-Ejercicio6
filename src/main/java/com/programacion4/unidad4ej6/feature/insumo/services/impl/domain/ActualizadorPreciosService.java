package com.programacion4.unidad4ej6.feature.insumo.services.impl.domain;

import com.programacion4.unidad4ej6.feature.insumo.models.Insumo;
import com.programacion4.unidad4ej6.feature.insumo.repositories.IInsumoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ActualizadorPreciosService {

    private static final Logger logger = LoggerFactory.getLogger(ActualizadorPreciosService.class);

    // Endpoint de Argentina Datos — dólar oficial
    private static final String API_DOLAR_URL =
            "https://api.argentinadatos.com/v1/cotizaciones/dolares/oficial";

    private final IInsumoRepository insumoRepository;
    private final RestTemplate restTemplate;

    public ActualizadorPreciosService(IInsumoRepository insumoRepository,
                                      RestTemplate restTemplate) {
        this.insumoRepository = insumoRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Cron: cada hora, de lunes a viernes (L-V).
     * Expresión: segundo minuto hora día-mes mes día-semana
     *   0 0 * * * MON-FRI  →  al minuto 0 de cada hora, lunes a viernes
     */
    @Scheduled(cron = "0 0 * * * MON-FRI")
    @Transactional
    public void actualizarPrecios() {
        logger.info(">>> Iniciando actualización automática de precios (dólar oficial)");

        Double valorDolarActual = obtenerValorDolarOficial();
        if (valorDolarActual == null) {
            logger.error("No se pudo obtener el valor del dólar. Se cancela la actualización.");
            return;
        }

        logger.info("Valor dólar oficial obtenido: {}", valorDolarActual);

        List<Insumo> insumos = insumoRepository.findAll();
        int actualizados = 0;

        for (Insumo insumo : insumos) {
            if (!valorDolarActual.equals(insumo.getValorDolarReferencia())) {
                insumo.setValorDolarReferencia(valorDolarActual);
                insumo.setPrecioEnPesos(insumo.getPrecioEnDolares() * valorDolarActual);
                insumoRepository.save(insumo);
                actualizados++;
                logger.info("Insumo '{}' actualizado → precioEnPesos: {}",
                        insumo.getNombre(), insumo.getPrecioEnPesos());
            }
        }

        logger.info("<<< Actualización finalizada. Insumos actualizados: {}/{}", actualizados, insumos.size());
    }

    /**
     * Llama a la API de Argentina Datos y extrae el valor de venta del dólar oficial.
     * Retorna null si ocurre algún error.
     */
    public Double obtenerValorDolarOficial() {
        try {
            // La API devuelve un array; tomamos el último elemento (más reciente)
            Map[] respuesta = restTemplate.getForObject(API_DOLAR_URL, Map[].class);
            if (respuesta == null || respuesta.length == 0) {
                logger.warn("La API de Argentina Datos devolvió una respuesta vacía.");
                return null;
            }
            Map ultimaCotizacion = respuesta[respuesta.length - 1];
            Object venta = ultimaCotizacion.get("venta");
            if (venta == null) {
                logger.warn("No se encontró el campo 'venta' en la respuesta de la API.");
                return null;
            }
            return Double.parseDouble(venta.toString());
        } catch (Exception e) {
            logger.error("Error al consultar la API de Argentina Datos: {}", e.getMessage());
            return null;
        }
    }
}

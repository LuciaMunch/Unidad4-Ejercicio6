package com.programacion4.unidad4ej6.services;

import com.programacion4.unidad4ej6.feature.insumo.models.Insumo;
import com.programacion4.unidad4ej6.feature.insumo.repositories.IInsumoRepository;
import com.programacion4.unidad4ej6.feature.insumo.services.impl.domain.ActualizadorPreciosService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualizadorPreciosServiceTest {

    @Mock
    private IInsumoRepository insumoRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ActualizadorPreciosService service;

    private Insumo insumoConPrecioViejo;

    @BeforeEach
    void setUp() {
        insumoConPrecioViejo = new Insumo();
        insumoConPrecioViejo.setId(1L);
        insumoConPrecioViejo.setNombre("Tornillo A");
        insumoConPrecioViejo.setPrecioEnDolares(10.0);
        insumoConPrecioViejo.setValorDolarReferencia(800.0);   // valor viejo
        insumoConPrecioViejo.setPrecioEnPesos(8000.0);
    }

    // ─────────────────────────────────────────────
    //  Tests de obtenerValorDolarOficial()
    // ─────────────────────────────────────────────

    @Test
    void obtenerValorDolarOficial_debeRetornarValorCorrecto() {
        Map<String, Object> cotizacion = Map.of("venta", 1050.0);
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(new Map[]{cotizacion});

        Double resultado = service.obtenerValorDolarOficial();

        assertEquals(1050.0, resultado);
    }

    @Test
    void obtenerValorDolarOficial_cuandoAPIDevuelveNull_retornaNull() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(null);

        Double resultado = service.obtenerValorDolarOficial();

        assertNull(resultado);
    }

    @Test
    void obtenerValorDolarOficial_cuandoAPILanzaExcepcion_retornaNull() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenThrow(new RuntimeException("Timeout de red"));

        Double resultado = service.obtenerValorDolarOficial();

        assertNull(resultado);
    }

    @Test
    void obtenerValorDolarOficial_cuandoRespuestaEsArrayVacio_retornaNull() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(new Map[]{});

        Double resultado = service.obtenerValorDolarOficial();

        assertNull(resultado);
    }

    // ─────────────────────────────────────────────
    //  Tests de actualizarPrecios()
    // ─────────────────────────────────────────────

    @Test
    void actualizarPrecios_cuandoDolarCambio_debeActualizarInsumo() {
        double nuevoDolar = 1050.0;
        Map<String, Object> cotizacion = Map.of("venta", nuevoDolar);
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(new Map[]{cotizacion});
        when(insumoRepository.findAll()).thenReturn(List.of(insumoConPrecioViejo));

        service.actualizarPrecios();

        assertEquals(nuevoDolar, insumoConPrecioViejo.getValorDolarReferencia());
        assertEquals(10.0 * nuevoDolar, insumoConPrecioViejo.getPrecioEnPesos());
        verify(insumoRepository, times(1)).save(insumoConPrecioViejo);
    }

    @Test
    void actualizarPrecios_cuandoDolarNoVario_noDebeGuardarCambios() {
        double mismoDolar = 800.0;
        Map<String, Object> cotizacion = Map.of("venta", mismoDolar);
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(new Map[]{cotizacion});
        when(insumoRepository.findAll()).thenReturn(List.of(insumoConPrecioViejo));

        service.actualizarPrecios();

        verify(insumoRepository, never()).save(any());
    }

    @Test
    void actualizarPrecios_cuandoAPIFalla_noDebeTocarLaBD() {
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(null);

        service.actualizarPrecios();

        verify(insumoRepository, never()).findAll();
        verify(insumoRepository, never()).save(any());
    }

    @Test
    void actualizarPrecios_conMultiplesInsumos_actualizaTodosLosQueCorresponden() {
        Insumo insumoActualizado = new Insumo();
        insumoActualizado.setId(2L);
        insumoActualizado.setNombre("Cable B");
        insumoActualizado.setPrecioEnDolares(5.0);
        insumoActualizado.setValorDolarReferencia(1050.0);  // ya tiene el valor nuevo
        insumoActualizado.setPrecioEnPesos(5250.0);

        double nuevoDolar = 1050.0;
        Map<String, Object> cotizacion = Map.of("venta", nuevoDolar);
        when(restTemplate.getForObject(anyString(), eq(Map[].class)))
                .thenReturn(new Map[]{cotizacion});
        when(insumoRepository.findAll())
                .thenReturn(List.of(insumoConPrecioViejo, insumoActualizado));

        service.actualizarPrecios();

        verify(insumoRepository, times(1)).save(insumoConPrecioViejo);
        verify(insumoRepository, never()).save(insumoActualizado);
    }
}

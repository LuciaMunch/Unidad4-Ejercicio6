package com.programacion4.unidad4ej6.feature.insumo.models;

import java.util.List;
import java.util.ArrayList;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "insumos")
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class Insumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String codigoInterno;

    @Column(nullable = false)
    private Double stockActual = 0.0;

    @Column(nullable = false)
    private Boolean activo = true;

    // Campos para actualización automática de precios
    @Column(nullable = false)
    private Double precioEnDolares = 0.0;

    @Column(nullable = false)
    private Double valorDolarReferencia = 0.0;

    @Column(nullable = false)
    private Double precioEnPesos = 0.0;

    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HistorialPrecio> historialPrecios = new ArrayList<>();

    @OneToMany(mappedBy = "insumo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovimientoStock> movimientosStock = new ArrayList<>();

    public void changeStatus() {
        this.activo = !this.activo;
    }
}

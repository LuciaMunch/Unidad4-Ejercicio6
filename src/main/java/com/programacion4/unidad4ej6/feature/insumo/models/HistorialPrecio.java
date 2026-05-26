package com.programacion4.unidad4ej6.feature.insumo.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "historial_precios")
@Data
@NoArgsConstructor 
@AllArgsConstructor
public class HistorialPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double precio;

    @Column(nullable = false)
    private LocalDateTime fechaEntradaVigencia;

    @ManyToOne
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;
}
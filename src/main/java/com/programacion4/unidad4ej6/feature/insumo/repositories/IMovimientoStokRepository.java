package com.programacion4.unidad4ej6.feature.insumo.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.programacion4.unidad4ej6.feature.insumo.models.MovimientoStock;

@Repository
public interface IMovimientoStokRepository extends CrudRepository<MovimientoStock, Long> {
    
}

package com.gustavoronchi.fipe_search.repositories;

import com.gustavoronchi.fipe_search.dto.ConsultaFipeDTO;
import com.gustavoronchi.fipe_search.entities.ReferenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReferenciaRepository extends JpaRepository<ReferenciaEntity, Long> {

    @Query("""
            select new com.gustavoronchi.fipe_search.dto.ConsultaFipeDTO(
                ma.nome, mo.nome, r.anoModelo, r.preco, r.mesReferencia)
            from ReferenciaEntity r
                join r.modelo mo
                join mo.marca ma
            where r.modelo.id = :modeloId and r.anoModelo = :anoModelo
            order by r.mesReferencia desc
            """)
    List<ConsultaFipeDTO> findReferencias(
            @Param("modeloId") Long modeloId,
            @Param("anoModelo") Integer anoModelo);

    @Query(value = "select pg_sleep(0.05)", nativeQuery = true)
    void simulateDelay();
}

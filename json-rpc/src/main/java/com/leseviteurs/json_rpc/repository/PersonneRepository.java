package com.leseviteurs.json_rpc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.leseviteurs.json_rpc.model.Personnes_json_rpc;

public interface PersonneRepository extends JpaRepository<Personnes_json_rpc, Long> {
    // Retourne le nombre d'éléments supprimés
    @Modifying
    @Query("DELETE FROM Personnes_json_rpc p WHERE p.id = :id")
    int deleteByIdAndReturnCount(@Param("id") Long id);
}

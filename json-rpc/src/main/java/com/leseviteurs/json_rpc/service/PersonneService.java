package com.leseviteurs.json_rpc.service;

import com.leseviteurs.json_rpc.model.Personnes_json_rpc;
import com.leseviteurs.json_rpc.repository.PersonneRepository;


import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class PersonneService {
    private final PersonneRepository repo;

    public PersonneService(PersonneRepository repo) {
        this.repo = repo;
    }

    public List<Personnes_json_rpc> findAll() {
        return repo.findAll();
    }

    public Optional<Personnes_json_rpc> findById(Long id) {
        return repo.findById(id);
    }

    public Personnes_json_rpc add(Personnes_json_rpc p) {
        // validation simple
        if (!StringUtils.hasText(p.getNom()) || !StringUtils.hasText(p.getPrenom())) {
            throw new IllegalArgumentException("Nom et prénom requis");
        }
        return repo.save(p);
    }

    public Personnes_json_rpc update(Long id, Personnes_json_rpc p) {
        Personnes_json_rpc existing = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("La personne n'existe pas avec l'id : " + id));
        existing.setNom(p.getNom());
        existing.setPrenom(p.getPrenom());
        existing.setDateNaissance(p.getDateNaissance());
        existing.setAdresse(p.getAdresse());
        existing.setTelephone(p.getTelephone());
        return repo.save(existing);
    }

    public void delete(Long id) {
        // Vérifier d'abord si l'entité existe
        if (!repo.existsById(id)) {
            throw new RuntimeException("Personne non trouvé avec l'id : " + id);
        }
        
        // Supprimer avec la méthode standard
        repo.deleteById(id);
    }
}

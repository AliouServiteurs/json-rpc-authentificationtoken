package com.leseviteurs.json_rpc.controller;

import com.leseviteurs.json_rpc.service.PersonneService;

import jakarta.persistence.EntityNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.leseviteurs.json_rpc.model.Personnes_json_rpc;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/rpc")
public class JsonRpcController {

    private final PersonneService personService;
    private final ObjectMapper mapper;

    public JsonRpcController(PersonneService personService, ObjectMapper mapper) {
        this.personService = personService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<?> handle(@RequestBody Map<String, Object> request, Authentication authentication) {
        ObjectNode resp = mapper.createObjectNode();
        resp.put("jsonrpc", "2.0");
        Object id = request.get("id");
        if (id == null)
            resp.putNull("id");
        else
            resp.set("id", mapper.valueToTree(id));

        // validation basique
        String method = (String) request.get("method");
        Object params = request.get("params");

        if (method == null) {
            resp.set("error", errorNode(-32600, "La requête est invalid ou inconnue !"));
            return ResponseEntity.badRequest().body(resp);
        }

        try {
            switch (method) {
                case "listPersons": {
                    var list = personService.findAll();
                    resp.set("result", mapper.valueToTree(list));
                    return ResponseEntity.ok(resp);
                }
                case "getPersonById": {
                    Map<String, Object> p = (Map<String, Object>) params;
                    Long pid = ((Number) p.get("id")).longValue();
                    var person = personService.findById(pid).orElse(null);
                    resp.set("result", mapper.valueToTree(person));
                    return ResponseEntity.ok(resp);
                }
                case "addPerson": {
                    // require authentication + ADMIN
                    if (!isAdmin(authentication))
                        return forbidden(resp, "Accés refusé !");
                    Map<String, Object> mp = (Map<String, Object>) params;
                    PersonDTO dto = mapper.convertValue(mp, PersonDTO.class);
                    var person = personService.add(dto.toPerson());
                    resp.set("result", mapper.valueToTree(person));
                    return ResponseEntity.ok(resp);
                }
                case "updatePerson": {
                    if (!isAdmin(authentication))
                        return forbidden(resp, "Accés refusé !");
                    Map<String, Object> mp = (Map<String, Object>) params;
                    Long pid = ((Number) mp.get("id")).longValue();
                    PersonDTO dto = mapper.convertValue(mp, PersonDTO.class);
                    var person = personService.update(pid, dto.toPerson());
                    resp.set("result", mapper.valueToTree(person));
                    return ResponseEntity.ok(resp);
                }
                case "deletePerson": {
                    try {
                        if (!isAdmin(authentication))
                            return forbidden(resp, "Accés no permise !");
                        Map<String, Object> mp = (Map<String, Object>) params;
                        Long pid = ((Number) mp.get("id")).longValue();

                        personService.delete(pid);
                        resp.set("result", mapper.valueToTree("Suppression réuissit avec succés !"));
                        return ResponseEntity.ok(resp);

                    } catch (EntityNotFoundException ex) {
                        System.out.println("Il s'est produit une erreur : " );
                        resp.set("error", errorNode(-32001, ex.getMessage()));
                        return ResponseEntity.status(404).body(resp);
                    }
                }
                default:
                    resp.set("error", errorNode(-32601, "Méthode inexistant !"));
                    return ResponseEntity.status(404).body(resp);
            }
        } catch (IllegalArgumentException ex) {
            resp.set("error", errorNode(-32602, ex.getMessage()));
            return ResponseEntity.badRequest().body(resp);
        } catch (Exception ex) {
            resp.set("error", errorNode(-32603, "Ereeur interne : " + ex.getMessage()));
            return ResponseEntity.status(500).body(resp);
        }
    }

    private boolean isAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
    }

    private ResponseEntity<ObjectNode> forbidden(ObjectNode resp, String msg) {
        resp.set("error", errorNode(-32000, msg));
        return ResponseEntity.status(403).body(resp);
    }

    private ObjectNode errorNode(int code, String message) {
        ObjectNode e = mapper.createObjectNode();
        e.put("code", code);
        e.put("message", message);
        return e;
    }

    // DTO interne pour mapper params -> Person
    public static class PersonDTO {
        public String nom;
        public String prenom;
        public String dateNaissance; // format ISO yyyy-MM-dd
        public String adresse;
        public String telephone;
        public Long id;

        public Personnes_json_rpc toPerson() {
            Personnes_json_rpc p = new Personnes_json_rpc();
            p.setNom(nom);
            p.setPrenom(prenom);
            if (dateNaissance != null)
                p.setDateNaissance(LocalDate.parse(dateNaissance));
            p.setAdresse(adresse);
            p.setTelephone(telephone);
            if (id != null)
                p.setId(id);
            return p;
        }
    }
}

package org.example;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.PropertyValue;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class OntologyController {
    private static final Logger logger = LoggerFactory.getLogger(OntologyController.class);
    private OntologyManager ontologyManager;

    public OntologyController(OntologyManager ontologyManager) {
        this.ontologyManager = ontologyManager;
    }
    @GetMapping("/api/classes/properties")
    public List<ClassPropertiesResponse> getAllClassProperties() {
        logger.info("Fetching all class properties...");
        List<ClassPropertiesResponse> response = new ArrayList<>();

        // Obtain all classes and their properties from the ontology
        ResIterator classIterator = ontologyManager.getModel().listSubjectsWithProperty(RDF.type, OWL.Class);

        while (classIterator.hasNext()) {
            Resource classResource = classIterator.nextResource();
            String className = classResource.getLocalName();
            List<String> properties = ontologyManager.getProperties(className); // Updated to List<String>

            ClassPropertiesResponse classProperties = new ClassPropertiesResponse(className, properties);
            response.add(classProperties);
            logger.info("Added properties for class: {}", className); // Log each class processed
        }

        return response;
    }
    // Endpoint to get data properties for a specific class
    @GetMapping("/data-properties/{className}")
    public String[] getDataProperties(@PathVariable String className) {
        String[] properties = ontologyManager.getDataProperties(className);
        return properties;
    }
    // Endpoint to check if a class exists in the ontology
    @GetMapping("/api/classes/exist")
    public ResponseEntity<Boolean> checkClassExists(@RequestParam String className) {
        logger.info("Checking if class exists: {}", className);

        boolean exists = ontologyManager.classExists(className); // Call the existing method

        logger.info("Class {} exists: {}", className, exists);
        return ResponseEntity.ok(exists);
    }
    @GetMapping("/api/classes/individuals")
    public ResponseEntity<List<String>> listClassesAndIndividuals() {
        logger.info("Listing all classes and their individuals...");

        List<String> response = ontologyManager.listClassesAndIndividuals(); // Call the existing method that returns a simple structure

        return ResponseEntity.ok(response);
    }
    @PostMapping("/individuals")
    public ResponseEntity<String> createIndividual(
            @RequestParam String className,
            @RequestParam String individualName,
            @RequestBody Map<String, String> properties) {
        try {
            ontologyManager.createIndividual(className, individualName, properties);
            return ResponseEntity.ok("Individual created: " + individualName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating individual: " + e.getMessage());
        }
    }
    @GetMapping("/individuals/{className}")
    public ResponseEntity<List<Map<String, Object>>> readIndividuals(@PathVariable String className) {
        List<Map<String, Object>> individualsList = new ArrayList<>();

        if (!ontologyManager.classExists(className)) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            // Call the existing readIndividuals method to get the individuals
            List<Map<String, Object>> individuals = ontologyManager.readIndividuals(className);
            individualsList.addAll(individuals);

            return ResponseEntity.ok(individualsList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
    @PutMapping("update/{className}/{individualName}")
    public ResponseEntity<String> updateIndividual(
            @PathVariable String className,
            @PathVariable String individualName,
            @RequestParam String propertyName,
            @RequestParam String newValue) {

        try {
            ontologyManager.updateIndividual(className, individualName, propertyName, newValue);
            return ResponseEntity.ok("Individual updated successfully: " + individualName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating individual: " + e.getMessage());
        }
    }
    @DeleteMapping("delete/individuals/{individualName}")
    public ResponseEntity<String> deleteIndividual(@PathVariable String individualName) {
        try {
            ontologyManager.deleteIndividual(individualName);
            return ResponseEntity.ok("Individu supprimé : " + individualName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur lors de la suppression de l'individu : " + e.getMessage());
        }
    }
    @PostMapping("/property")
    public ResponseEntity<String> addOrUpdateObjectProperty(
            @RequestParam String className,
            @RequestParam String individualName,
            @RequestParam String propertyName,
            @RequestParam String targetIndividualName,
            @RequestParam boolean isAdd) {
        try {
            // Delegate the logic to the service layer
            OntologyManager.addOrUpdateObjectProperty(className, individualName, propertyName, targetIndividualName, isAdd);
            return ResponseEntity.ok("Propriété d'objet " + (isAdd ? "ajoutée" : "mise à jour") + " avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'ajout ou de la mise à jour de la propriété d'objet : " + e.getMessage());
        }
    }

    @GetMapping("/objectprop/{className}")
    public ResponseEntity<List<String>> getObjectPropertiesForClass(@PathVariable String className) {
        List<String> objectProperties;
        try {
            objectProperties = OntologyManager.getObjectPropertiesForClass(className); // Call your method here
            if (objectProperties.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(objectProperties); // No properties found
            }
            return ResponseEntity.ok(objectProperties); // Return found properties
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Internal server error
        }
    }

    @GetMapping("/seeproperties")
    public ResponseEntity<List<String>> getAllPropertiesForIndividual(
            @RequestParam String className,
            @RequestParam String individualName) {
        // Validate parameters
        if (className == null || className.isEmpty() || individualName == null || individualName.isEmpty()) {
            return ResponseEntity.badRequest().body(List.of("Class name and individual name are required."));
        }

        try {
            // Call the service to get properties for the individual
            List<String> properties = OntologyManager.getAllPropertiesForIndividual(className, individualName);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(List.of("Error retrieving properties: " + e.getMessage()));
        }
    }

}

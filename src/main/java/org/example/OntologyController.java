package org.example;

import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.springframework.beans.PropertyValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
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

}

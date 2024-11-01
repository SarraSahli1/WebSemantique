package org.example;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.update.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class OntologyManager {
    private static Scanner scanner = new Scanner(System.in); // Initialize the scanner

    private static final String ONTOLOGY_PATH = "src/main/resources/web_semantique.owl";
    private static final String NAMESPACE = "http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#";
    private static Model model;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        loadAndDisplayOntology();

        // List classes and individuals before performing CRUD operations
        listClassesAndIndividuals();

        // Affichez les propriétés de toutes les classes
        displayAllClassProperties();

        while (true) {
            System.out.println("\nSelect an operation:");
            System.out.println("1. Create Individual");
            System.out.println("2. Read Individuals");
            System.out.println("3. Update Individual");
            System.out.println("4. Delete Individual");
            System.out.println("5. Add Object Property");
            System.out.println("6. Update Object Property");
            System.out.println("7. Delete Object Property");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            switch (choice) {
                case 1:
                    // Create Individual using a method that receives parameters
                    String createClass = "ClassName"; // Example value, replace with actual class name
                    String createName = "IndividualName"; // Example value, replace with actual individual name
                    Map<String, String> properties = new HashMap<>(); // Initialize properties map

                    // You can populate the properties map with necessary values
                    properties.put("property1", "value1"); // Example property
                    properties.put("property2", "value2"); // Example property

                    // Call the createIndividual method with the properties map
                    createIndividual(createClass, createName, properties);
                    break;

            case 2:
                    // Read Individuals
                    System.out.print("Enter class name: ");
                    String readClass = scanner.nextLine();
                    readIndividuals(readClass);
                    break;

                case 3:
                    // Update Individual
                    System.out.print("Enter class name: ");
                    String updateName = scanner.nextLine();
                    System.out.print("Enter individual name: ");
                    String updateClass = scanner.nextLine();
                    System.out.print("Enter property name to update: ");
                    String propertyName = scanner.nextLine();
                    System.out.print("Enter new value: ");
                    String newValue = scanner.nextLine();
                    updateIndividual(updateName, updateClass, propertyName, newValue);
                    break;

                case 4:
                    // Delete Individual
                    System.out.print("Enter individual name: ");
                    String deleteName = scanner.nextLine();
                    deleteIndividual(deleteName);
                    break;





                case 7:
                    deleteObjectProperty(scanner);
                    break;
                case 8:
                    // Exit
                    System.out.println("Exiting...");
                    scanner.close();
                    return;
                default:
                    System.out.println("Option invalide, essayez encore.");
            }
        }
    }
    public OntologyManager() {
        model = ModelFactory.createDefaultModel();
        // Load your ontology file
        try (InputStream in = FileManager.get().open(ONTOLOGY_PATH)) {
            if (in == null) {
                throw new IllegalArgumentException("File: " + ONTOLOGY_PATH + " not found");
            }
            model.read(in, null);
            System.out.println("Ontology loaded successfully from: " + ONTOLOGY_PATH);
        } catch (Exception e) {
            System.err.println("Error loading ontology: " + e.getMessage());
        }
    }


    // Méthode pour afficher les propriétés de toutes les classes
    private static void displayAllClassProperties() {
        // Obtenez toutes les classes de l'ontologie
        ResIterator classIterator = model.listSubjectsWithProperty(RDF.type, OWL.Class);

        // Pour chaque classe, affichez ses propriétés
        while (classIterator.hasNext()) {
            Resource classResource = classIterator.nextResource();
            String className = classResource.getLocalName(); // Récupère le nom local de la classe
            System.out.println("Affichage des propriétés pour la classe : " + className);
            displayProperties(className);
            System.out.println(); // Ligne vide pour séparer les sorties
        }
    }
    public Model getModel() {
        return model;
    }
    // Example of a method to get properties of a class
    public List<String> getProperties(String className) {
        List<String> propertyValues = new ArrayList<>();

        // Get the class resource
        Resource classResource = model.getResource(NAMESPACE + className);

        // Retrieve Data Properties
        StmtIterator dataPropertyIterator = model.listStatements(null, RDF.type, OWL.DatatypeProperty);
        while (dataPropertyIterator.hasNext()) {
            Statement stmt = dataPropertyIterator.nextStatement();
            String propertyName = stmt.getSubject().getLocalName(); // Get the property name
            propertyValues.add(propertyName); // Add property name directly
        }

        // Retrieve Object Properties
        StmtIterator objectPropertyIterator = model.listStatements(null, RDF.type, OWL.ObjectProperty);
        while (objectPropertyIterator.hasNext()) {
            Statement stmt = objectPropertyIterator.nextStatement();
            String propertyName = stmt.getSubject().getLocalName(); // Get the property name
            propertyValues.add(propertyName); // Add property name directly
        }

        return propertyValues;
    }

    public static void loadAndDisplayOntology() {
        model = ModelFactory.createDefaultModel();
        try (InputStream in = FileManager.get().open(ONTOLOGY_PATH)) {
            model.read(in, null);
            System.out.println("Ontology loaded successfully!");
            model.write(System.out, "RDF/XML");  // Display the ontology
        } catch (Exception e) {
            System.err.println("Error loading ontology: " + e.getMessage());
        }
    }

    // Retrieve data properties for a specific class
    public static String[] getDataProperties(String className) {
        // Créez une requête SPARQL pour obtenir les propriétés de données pour la classe spécifiée
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?property WHERE { " +
                        "  ?property rdf:type owl:DatatypeProperty . " +
                        "  ?property rdfs:domain <" + NAMESPACE + className + "> . " +
                        "}";

        List<String> datatypeProperties = new ArrayList<>();

        // Exécutez la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Resource property = solution.getResource("property");
                datatypeProperties.add(property.getLocalName());
            }
        }

        // Debug: Print retrieved datatype properties
        System.out.println("Retrieved datatype properties for class " + className + ": " + datatypeProperties);
        return datatypeProperties.toArray(new String[0]);
    }

    public static String[] getObjectProperties(String className) {
        // Créez une requête SPARQL pour obtenir les propriétés d'objet pour la classe spécifiée
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?property WHERE { " +
                        "  ?property rdf:type owl:ObjectProperty . " +
                        "  ?property rdfs:domain <" + NAMESPACE + className + "> . " +
                        "}";

        List<String> objectProperties = new ArrayList<>();

        // Exécutez la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            ResultSet results = queryExecution.execSelect();
            while (results.hasNext()) {
                QuerySolution solution = results.nextSolution();
                Resource property = solution.getResource("property");
                objectProperties.add(property.getLocalName());
            }
        }

        // Debug: Print retrieved object properties
        System.out.println("Retrieved object properties for class " + className + ": " + objectProperties);
        return objectProperties.toArray(new String[0]);
    }


    // Check if a class exists in the ontology
    public static boolean classExists(String className) {
        // Créez une requête SPARQL pour vérifier l'existence de la classe
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "ASK { " +
                        "  <" + NAMESPACE + className + "> rdf:type owl:Class . " +
                        "}";

        // Exécutez la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution queryExecution = QueryExecutionFactory.create(query, model)) {
            return queryExecution.execAsk();
        }
    }


    public static void saveOntology() {
        try (OutputStream out = new FileOutputStream(ONTOLOGY_PATH)) {
            model.write(out, "RDF/XML");
            System.out.println("Ontology saved successfully!");
        } catch (Exception e) {
            System.err.println("Error saving ontology: " + e.getMessage());
        }
    }

    // Method to list all classes and their individuals with properties
    public static List<String> listClassesAndIndividuals() {
        List<String> result = new ArrayList<>();

        // Requête SPARQL pour récupérer toutes les classes
        String classQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?class WHERE { " +
                        "  ?class rdf:type owl:Class ." +
                        "}";

        // Exécutez la requête pour obtenir les classes
        try (QueryExecution classQueryExecution = QueryExecutionFactory.create(classQuery, model)) {
            ResultSet classResults = classQueryExecution.execSelect();
            while (classResults.hasNext()) {
                QuerySolution classSolution = classResults.nextSolution();
                Resource cls = classSolution.getResource("class");
                String className = cls.getLocalName();
                result.add("Class: " + className);

                // Requête SPARQL pour récupérer les individus de cette classe
                String individualQuery =
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "SELECT ?individual WHERE { " +
                                "  ?individual rdf:type <" + cls.getURI() + "> ." +
                                "}";

                // Exécutez la requête pour obtenir les individus
                try (QueryExecution individualQueryExecution = QueryExecutionFactory.create(individualQuery, model)) {
                    ResultSet individualResults = individualQueryExecution.execSelect();
                    while (individualResults.hasNext()) {
                        QuerySolution individualSolution = individualResults.nextSolution();
                        Resource individual = individualSolution.getResource("individual");
                        result.add("  Individual: " + individual.getLocalName());

                        // Requête SPARQL pour lister les propriétés de l'individu
                        String propertyQuery =
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                        "SELECT ?predicate ?object WHERE { " +
                                        "  <" + individual.getURI() + "> ?predicate ?object ." +
                                        "}";

                        // Exécutez la requête pour obtenir les propriétés
                        try (QueryExecution propertyQueryExecution = QueryExecutionFactory.create(propertyQuery, model)) {
                            ResultSet propertyResults = propertyQueryExecution.execSelect();
                            while (propertyResults.hasNext()) {
                                QuerySolution propertySolution = propertyResults.nextSolution();
                                String predicate = propertySolution.getResource("predicate").getLocalName();
                                RDFNode objectNode = propertySolution.get("object");
                                result.add("    " + predicate + ": " + objectNode.toString());
                            }
                        }
                    }
                }
            }
        }

        return result;
    }


    public static void displayProperties(String className) {
        System.out.println("Data Properties for class " + className + ":");

        // Requête SPARQL pour obtenir les propriétés de données pour la classe spécifiée
        String dataPropertyQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT ?property WHERE { " +
                        "  ?property rdf:type owl:DatatypeProperty . " +
                        "  ?property rdfs:domain <" + NAMESPACE + className + "> ." +
                        "}";

        // Exécutez la requête pour obtenir les propriétés de données
        try (QueryExecution dataPropertyQueryExecution = QueryExecutionFactory.create(dataPropertyQuery, model)) {
            ResultSet dataPropertyResults = dataPropertyQueryExecution.execSelect();
            while (dataPropertyResults.hasNext()) {
                QuerySolution dataPropertySolution = dataPropertyResults.nextSolution();
                Resource property = dataPropertySolution.getResource("property");
                System.out.println("- " + property.getLocalName());
            }
        }

        System.out.println("Object Properties for class " + className + ":");

        // Requête SPARQL pour obtenir les propriétés d'objet pour la classe spécifiée
        String objectPropertyQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT ?property WHERE { " +
                        "  ?property rdf:type owl:ObjectProperty . " +
                        "  ?property rdfs:domain <" + NAMESPACE + className + "> ." +
                        "}";

        // Exécutez la requête pour obtenir les propriétés d'objet
        try (QueryExecution objectPropertyQueryExecution = QueryExecutionFactory.create(objectPropertyQuery, model)) {
            ResultSet objectPropertyResults = objectPropertyQueryExecution.execSelect();
            while (objectPropertyResults.hasNext()) {
                QuerySolution objectPropertySolution = objectPropertyResults.nextSolution();
                Resource property = objectPropertySolution.getResource("property");
                System.out.println("- " + property.getLocalName());
            }
        }
    }


    // CREATE: Add a new individual
// CREATE: Add a new individual
    public static String createIndividual(String className, String individualName, Map<String, String> properties) {
        if (!classExists(className)) {
            System.out.println("La classe " + className + " n'existe pas dans l'ontologie.");
            return className;
        }

        // Créer un Dataset à partir du modèle existant
        Dataset dataset = DatasetFactory.create(model);

        // Création d'un objet UpdateRequest
        UpdateRequest updateRequest = UpdateFactory.create();

        // Ajouter l'individu avec son type
        String insertIndividual = "PREFIX ns: <" + NAMESPACE + "> " +
                "INSERT { " +
                "  ns:" + individualName + " a ns:" + className + " . " +
                "} WHERE { }";
        updateRequest.add(insertIndividual);

        // Récupérer les propriétés de données pour la classe donnée
        String[] dataProperties = getDataProperties(className);

        // Utiliser les propriétés fournies dans la requête
        for (String property : dataProperties) {
            String value = properties.get(property); // Get the value from the properties map

            if (value != null) {
                // Ajouter chaque propriété à la requête
                String insertProperty = "PREFIX ns: <" + NAMESPACE + "> " +
                        "INSERT { " +
                        "  ns:" + individualName + " ns:" + property + " \"" + value + "\" . " +
                        "} WHERE { }";
                updateRequest.add(insertProperty);
            }
        }

        // Exécuter la requête de mise à jour sur le dataset
        UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
        processor.execute();

        // Optionnel : sauvegarder l'ontologie après la création de l'individu
        saveOntology();

        System.out.println("Individual created: " + individualName);
        return className;
    }

    // UPDATE: Update properties of an existing individual


    public static void updateIndividual(String className, String individualName, String propertyName, String newValue) {
        // Vérification de l'existence de la classe
        Resource classResource = model.getResource(NAMESPACE + className);
        if (classResource == null) {
            System.out.println("La classe " + className + " n'existe pas dans l'ontologie.");
            return;
        }

        // Vérification de l'existence de l'individu
        Resource individual = model.getResource(NAMESPACE + individualName);
        if (individual == null) {
            System.out.println("L'individu " + individualName + " n'existe pas.");
            return;
        }

        // Vérification que l'individu appartient bien à la classe
        if (!model.contains(individual, RDF.type, classResource)) {
            System.out.println("L'individu " + individualName + " n'appartient pas à la classe " + className + ".");
            return;
        }

        // Afficher les propriétés actuelles
        System.out.println("Propriétés actuelles pour l'individu " + individualName + ":");
        displayIndividualProperties(individual);

        // Vérification de l'existence de la propriété
        if (!Arrays.asList(getDataProperties(className)).contains(propertyName)) {
            System.out.println("La propriété " + propertyName + " n'existe pas pour la classe " + className + ".");
            return;
        }

        // Création de la requête SPARQL DELETE pour supprimer l'ancienne valeur
        String sparqlDelete = "PREFIX ns: <" + NAMESPACE + "> " +
                "DELETE { ns:" + individualName + " ns:" + propertyName + " ?o } " +
                "WHERE { ns:" + individualName + " ns:" + propertyName + " ?o }";

        // Création de la requête SPARQL INSERT pour ajouter la nouvelle valeur
        String sparqlInsert = "PREFIX ns: <" + NAMESPACE + "> " +
                "INSERT { ns:" + individualName + " ns:" + propertyName + " \"" + newValue + "\" } " +
                "WHERE { }";

        // Création de la requête de mise à jour
        UpdateRequest updateRequest = UpdateFactory.create();
        updateRequest.add(sparqlDelete);
        updateRequest.add(sparqlInsert);

        // Exécution de la requête de mise à jour
        Dataset dataset = DatasetFactory.create(model); // Conversion du modèle en Dataset
        UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
        processor.execute();

        saveOntology(); // Sauvegarder l'ontologie après la mise à jour

        System.out.println("Individu mis à jour : " + individualName);
    }
    // DELETE: Remove an individual
    public static void deleteIndividual(String individualName) {
        Resource individual = model.getResource(NAMESPACE + individualName);
        if (individual == null) {
            System.out.println("L'individu " + individualName + " n'existe pas dans l'ontologie.");
            return;
        }

        // Affichez les propriétés actuelles avant la suppression
        System.out.println("Propriétés actuelles de l'individu " + individualName + ":");
        displayIndividualProperties(individual);



        // Créer la requête SPARQL DELETE
        String sparqlDelete = "PREFIX ns: <" + NAMESPACE + "> " +
                "DELETE { ?s ?p ?o } " +
                "WHERE { " +
                "  ?s ?p ?o . " +
                "  FILTER(?s = ns:" + individualName + ") " +
                "}";

        // Créer la requête de mise à jour
        UpdateRequest updateRequest = UpdateFactory.create(sparqlDelete);

        // Créer un Dataset à partir du modèle
        Dataset dataset = DatasetFactory.create(model);

        // Exécuter la requête de mise à jour
        UpdateProcessor processor = UpdateExecutionFactory.create(updateRequest, dataset);
        processor.execute();

        System.out.println("Individu supprimé : " + individualName);

        saveOntology();  // Sauvegardez l'ontologie après la suppression
    }    // Method to display properties of an individual
    public static void displayIndividualProperties(Resource individual) {
        StmtIterator propertiesIterator = model.listStatements(individual, null, (RDFNode) null);
        while (propertiesIterator.hasNext()) {
            Statement stmt = propertiesIterator.nextStatement();
            System.out.println(stmt.getPredicate().getLocalName() + ": " + stmt.getObject().toString());
        }
    }

    // READ: Read individuals of a specific class
    public static List<Map<String, Object>> readIndividuals(String className) {
        List<Map<String, Object>> individualsList = new ArrayList<>();

        if (!classExists(className)) {
            throw new RuntimeException("La classe " + className + " n'existe pas dans l'ontologie.");
        }

        String queryString = String.format(
                "PREFIX ns: <%s> " +
                        "SELECT ?individual WHERE { " +
                        "?individual a ns:%s . }", NAMESPACE, className);

        try {
            Query query = QueryFactory.create(queryString);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    QuerySolution soln = results.nextSolution();
                    Resource individual = soln.getResource("individual");

                    // Create a map to hold individual data
                    Map<String, Object> individualData = new HashMap<>();
                    individualData.put("individualName", individual.getLocalName());

                    // Retrieve properties for the individual
                    String[] properties = getDataProperties(className); // Assuming this method returns property names

                    // Loop through properties and fetch their values
                    for (String property : properties) {
                        // Query to get the property value
                        String propertyValueQuery = String.format(
                                "PREFIX ns: <%s> " +
                                        "SELECT ?value WHERE { " +
                                        "  ns:%s ns:%s ?value . }",
                                NAMESPACE, individual.getLocalName(), property);

                        Query propertyQuery = QueryFactory.create(propertyValueQuery);
                        try (QueryExecution propertyQexec = QueryExecutionFactory.create(propertyQuery, model)) {
                            ResultSet propertyResults = propertyQexec.execSelect();
                            if (propertyResults.hasNext()) {
                                QuerySolution propertySoln = propertyResults.nextSolution();
                                individualData.put(property, propertySoln.get("value").toString());
                            }
                        }
                    }

                    // Add the individual data map to the list
                    individualsList.add(individualData);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage());
        }
        return individualsList; // Return the list of individuals and their properties
    }




    public static void addOrUpdateObjectProperty(String selectedClass, String selectedIndividual,
                                                 String selectedProperty, String selectedTargetIndividual,
                                                 boolean isAdd) {
        try {
            // Ajout ou mise à jour de la propriété d'objet avec SPARQL
            Dataset dataset = DatasetFactory.create(model); // Conversion du modèle en Dataset

            // Si c'est une mise à jour, supprimer l'ancienne relation
            if (!isAdd) {
                String sparqlDelete = "PREFIX ns: <" + NAMESPACE + "> " +
                        "DELETE { ns:" + selectedIndividual + " ns:" + selectedProperty + " ?o } " +
                        "WHERE { ns:" + selectedIndividual + " ns:" + selectedProperty + " ?o }";

                UpdateRequest deleteRequest = UpdateFactory.create(sparqlDelete);
                UpdateProcessor deleteProcessor = UpdateExecutionFactory.create(deleteRequest, dataset);
                deleteProcessor.execute();

                System.out.println("Ancienne propriété d'objet supprimée pour " + selectedIndividual);
            }

            // Ajout de la nouvelle relation
            String sparqlInsert = "PREFIX ns: <" + NAMESPACE + "> " +
                    "INSERT { ns:" + selectedIndividual + " ns:" + selectedProperty + " ns:" + selectedTargetIndividual + " } " +
                    "WHERE { }";

            UpdateRequest insertRequest = UpdateFactory.create(sparqlInsert);
            UpdateProcessor insertProcessor = UpdateExecutionFactory.create(insertRequest, dataset);
            insertProcessor.execute();

            saveOntology(); // Sauvegarder l'ontologie après la mise à jour ou l'ajout

            System.out.println("Propriété d'objet " + (isAdd ? "ajoutée" : "mise à jour") +
                    " entre " + selectedIndividual + " et " + selectedTargetIndividual);
        } catch (Exception e) {
            System.out.println("Une erreur s'est produite : " + e.getMessage());
        }
    }


    // Exemple de méthodes à implémenter pour obtenir les données de l'ontologie

    private static List<String> getClasses() {
        List<String> classes = new ArrayList<>();
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH); // Utilisation du chemin défini

        ResIterator classIterator = model.listResourcesWithProperty(RDF.type, OWL.Class);
        while (classIterator.hasNext()) {
            Resource classResource = classIterator.nextResource();
            classes.add(classResource.getLocalName());
        }
        return classes;
    }

    private static List<String> getIndividualsInClass(String className) {
        List<String> individuals = new ArrayList<>();
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH);

        // Requête SPARQL pour récupérer tous les individus de la classe et de ses sous-classes
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?individual " +
                        "WHERE { " +
                        "  ?class rdfs:subClassOf* <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + className + "> . " +
                        "  ?individual rdf:type ?class . " +
                        "}";

        // Exécution de la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Resource individualResource = sol.getResource("individual");
                if (individualResource != null && individualResource.getLocalName() != null) {
                    individuals.add(individualResource.getLocalName());
                } else {
                    System.out.println("Erreur : URI ou nom local introuvable pour un individu.");
                }
            }
        }
        return individuals;
    }

    // Méthode pour récupérer les individus d'une classe donnée
    static List<String> getObjectPropertiesForClass(String className) {
        List<String> objectProperties = new ArrayList<>();
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH);

        // Requête SPARQL pour récupérer toutes les propriétés d'objet pour la classe spécifiée
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?property " +
                        "WHERE { " +
                        "  ?property rdf:type owl:ObjectProperty . " +
                        "  ?property rdfs:domain <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + className + "> . " +
                        "}";

        // Exécution de la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Resource propertyResource = sol.getResource("property");
                if (propertyResource != null) {
                    objectProperties.add(propertyResource.getLocalName());
                }
            }
        }

        // Affichage des propriétés d'objet trouvées
        System.out.println("Propriétés d'objet pour la classe " + className + ":");
        for (String property : objectProperties) {
            System.out.println(property);
        }

        return objectProperties;
    }

    private static String getTargetClassForObjectProperty(String selectedClass, String selectedProperty) {
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH);

        // Requête SPARQL pour récupérer la classe cible associée à la propriété d'objet sélectionnée
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?targetClass " +
                        "WHERE { " +
                        "  ?property rdf:type owl:ObjectProperty . " +
                        "  ?property rdfs:range ?targetClass . " +
                        "  FILTER (?property = <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + selectedProperty + ">) " +
                        "}";

        // Exécution de la requête SPARQL
        Query query = QueryFactory.create(sparqlQuery);
        String targetClassName = null;
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Resource targetClassResource = sol.getResource("targetClass");
                if (targetClassResource != null) {
                    targetClassName = targetClassResource.getLocalName();
                }

                // Afficher les individus de la classe cible et de ses sous-classes
                if (targetClassName != null) {
                    System.out.println("Individus de la classe " + targetClassName + " et de ses sous-classes :");
                    List<String> individuals = getIndividualsAndSubClassIndividuals(targetClassName);
                    for (String individual : individuals) {
                        System.out.println(individual);
                    }
                }
            }
        }

        return targetClassName;
    }

    // Méthode pour récupérer tous les individus d'une classe donnée et de ses sous-classes
    private static List<String> getIndividualsAndSubClassIndividuals(String selectedClass) {
        List<String> individuals = new ArrayList<>();
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH);

        // Requête SPARQL pour récupérer les individus de la classe sélectionnée
        String queryIndividuals =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT ?individual " +
                        "WHERE { " +
                        "  ?individual rdf:type <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + selectedClass + "> ." +
                        "}";

        // Exécution de la requête SPARQL pour les individus de la classe principale
        Query query = QueryFactory.create(queryIndividuals);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Resource individual = sol.getResource("individual");
                individuals.add(individual.getLocalName());
            }
        }

        // Requête SPARQL pour récupérer les sous-classes de la classe sélectionnée
        String querySubClasses =
                "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "SELECT ?subClass " +
                        "WHERE { " +
                        "  ?subClass rdfs:subClassOf <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + selectedClass + "> ." +
                        "}";

        // Exécution de la requête SPARQL pour les sous-classes
        query = QueryFactory.create(querySubClasses);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            System.out.println("Sous-classes trouvées pour " + selectedClass + ":");
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                Resource subClass = sol.getResource("subClass");
                String subClassName = subClass.getLocalName();
                System.out.println(subClassName); // Affiche le nom de la sous-classe

                // Requête SPARQL pour récupérer les individus de chaque sous-classe
                String queryIndividualsInSubClass =
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                                "SELECT ?individual " +
                                "WHERE { " +
                                "  ?individual rdf:type <http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + subClassName + "> ." +
                                "}";

                // Exécution de la requête SPARQL pour les individus de la sous-classe
                Query subClassQuery = QueryFactory.create(queryIndividualsInSubClass);
                try (QueryExecution subClassQexec = QueryExecutionFactory.create(subClassQuery, model)) {
                    ResultSet subClassResults = subClassQexec.execSelect();
                    while (subClassResults.hasNext()) {
                        QuerySolution subClassSol = subClassResults.nextSolution();
                        Resource individual = subClassSol.getResource("individual");
                        individuals.add(individual.getLocalName());
                    }
                }
            }
        }

        return individuals;
    }

    // Méthode pour récupérer les individus d'une classe donnée


    private static void deleteObjectProperty(Scanner scanner) {
        System.out.print("Enter the name of the individual: ");
        String individualName = scanner.nextLine();
        System.out.print("Enter the object property name: ");
        String propertyName = scanner.nextLine();
        System.out.print("Enter the target individual name: ");
        String targetIndividualName = scanner.nextLine();

        // Créez le modèle et les ressources
        Model model = FileManager.get().loadModel(ONTOLOGY_PATH);
        Resource individualResource = model.getResource("http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + individualName);
        Resource propertyResource = model.getResource("http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + propertyName);
        Resource targetIndividualResource = model.getResource("http://www.semanticweb.org/asus/ontologies/2024/9/untitled-ontology-13#" + targetIndividualName);

        // Créer la requête SPARQL Update pour supprimer la propriété d'objet
        String sparqlUpdate =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "DELETE { " +
                        "  <" + individualResource.getURI() + "> <" + propertyResource.getURI() + "> <" + targetIndividualResource.getURI() + "> ." +
                        "} " +
                        "WHERE { " +
                        "  <" + individualResource.getURI() + "> <" + propertyResource.getURI() + "> <" + targetIndividualResource.getURI() + "> ." +
                        "}";

        // Créez un Dataset à partir du modèle
        Dataset dataset = DatasetFactory.create(model);

        // Exécution de la requête de mise à jour
        UpdateRequest updateRequest = UpdateFactory.create(sparqlUpdate);
        UpdateExecution updateExecution = UpdateExecutionFactory.create(updateRequest, dataset);
        updateExecution.execute();

        System.out.println("Deleted object property.");
    }

    public static List<String> getAllPropertiesForIndividual(String className, String individualName) {
        List<String> properties = new ArrayList<>();

        // SPARQL query to retrieve properties of the individual
        String sparqlQuery =
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> " +
                        "SELECT ?property ?targetIndividual " +
                        "WHERE { " +
                        "  ?individual rdf:type <" + NAMESPACE + className + "> . " +
                        "  ?individual ?property ?targetIndividual . " +
                        "  FILTER (?individual = <" + NAMESPACE + individualName + ">) " +
                        "}";

        // Execute the SPARQL query
        Query query = QueryFactory.create(sparqlQuery);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
            ResultSet results = qexec.execSelect();
            if (!results.hasNext()) {
                System.out.println("No properties found for individual: " + individualName);
            }
            while (results.hasNext()) {
                QuerySolution sol = results.nextSolution();
                String propertyName = sol.getResource("property").getLocalName();
                String targetIndividualName = sol.getResource("targetIndividual").getLocalName();
                properties.add(propertyName + " " + targetIndividualName);
            }
        } catch (Exception e) {
            System.out.println("Error while executing SPARQL query: " + e.getMessage());
        }

        // Return the list of properties
        return properties;
    }

}




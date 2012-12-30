package edu.wpi.always.user.owl;

import org.joda.time.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import java.io.*;
import java.util.*;

public class OntologyHelper {

   private final OWLDataFactory factory;
   private final OWLOntologyManager manager;
   private final OWLOntology ontology;
   private final PrefixManager pm;
   private final Ontology ontologyData;

   public OntologyHelper (Ontology ont) {
      this.ontologyData = ont;
      factory = ont.getFactory();
      manager = ont.getManager();
      ontology = ont.getOntology();
      pm = ont.getPm();
   }

   public Ontology getOntologyDataObject () {
      return ontologyData;
   }

   public OWLReasoner getReasoner () {
      return getOntologyDataObject().getReasoner();
   }

   public OWLDataFactory getFactory () {
      return factory;
   }

   public OWLOntologyManager getManager () {
      return manager;
   }

   public OWLOntology getOntology () {
      return ontology;
   }

   public PrefixManager getPm () {
      return pm;
   }

   protected String toIRIName (String name) {
      return "#" + name.replaceAll(" ", "%20");
   }

   protected String fromIRIName (String name) {
      if ( name == null )
         return null;
      if ( name.startsWith("#") )
         name = name.substring(1);
      return name.replaceAll("%20", " ");
   }

   public OWLClass getClass (String name) {
      return factory.getOWLClass(toIRIName(name), pm);
   }

   public OWLClassAssertionAxiom getClassAssertionAxiom (
         OWLClassExpression clazz, OWLIndividual individual) {
      return factory.getOWLClassAssertionAxiom(clazz, individual);
   }

   public String getName (OWLNamedIndividual individual) {
      if ( individual == null )
         return null;
      return fromIRIName(individual.getIRI().getFragment());
   }

   public OntologyIndividual getNamedIndividual (String name) {
      return new OntologyIndividual(getOntologyDataObject(),
            factory.getOWLNamedIndividual(toIRIName(name), pm));
   }

   public OWLObjectProperty getObjectProperty (String name) {
      return factory.getOWLObjectProperty(toIRIName(name), pm);
   }

   public OWLDataProperty getDataProperty (String name) {
      return factory.getOWLDataProperty(toIRIName(name), pm);
   }

   public OntologyClass declareClass (String name) {
      OWLClass clazz = getClass(name);
      addAxiom(getFactory().getOWLDeclarationAxiom(clazz));
      return new OntologyClass(getOntologyDataObject(), clazz);
   }

   public Set<OWLNamedIndividual> getAllOfClass (String className) {
      return getReasoner().getInstances(getClass(className), false)
            .getFlattened();
   }

   public OntologyValue getLiteral (String lexicalValue, OWL2Datatype datatype) {
      return new OntologyValue(getFactory().getOWLLiteral(lexicalValue,
            datatype));
   }

   public OntologyValue getLiteral (String lexicalValue, OWLDatatype datatype) {
      return new OntologyValue(getFactory().getOWLLiteral(lexicalValue,
            datatype));
   }

   public OntologyValue getLiteral (String value) {
      return new OntologyValue(getFactory().getOWLLiteral(value));
   }

   public OntologyValue getLiteral (double value) {
      return new OntologyValue(getFactory().getOWLLiteral(value));
   }

   public OntologyValue getLiteral (int value) {
      return new OntologyValue(getFactory().getOWLLiteral(value));
   }

   private final OWLDatatype XSD_GMonthDay_TYPE = new OWLDatatypeImpl(
         getFactory(), IRI.create("xsd:gMonthDay"));

   public OntologyValue getLiteral (MonthDay date) {
      return new OntologyValue(getFactory().getOWLLiteral(
            OntologyValue.XML_GMonthDay_FORMAT.print(date), XSD_GMonthDay_TYPE));
   }

   public OntologyValue getLiteral (ReadableInstant instant) {
      return new OntologyValue(getFactory().getOWLLiteral(
            OntologyValue.XML_DATE_TIME_FORMAT.print(instant),
            OWL2Datatype.XSD_DATE_TIME));
   }

   private final OWLDatatype XSD_DATE_TYPE = new OWLDatatypeImpl(getFactory(),
         IRI.create("xsd:date"));

   public OntologyValue getLiteral (LocalDate date) {
      return new OntologyValue(getFactory().getOWLLiteral(
            OntologyValue.XML_DATE_FORMAT.print(date), XSD_DATE_TYPE));
   }

   private final OWLDatatype XSD_TIME_TYPE = new OWLDatatypeImpl(getFactory(),
         IRI.create("xsd:time"));

   public OntologyValue getLiteral (LocalTime time) {
      return new OntologyValue(getFactory().getOWLLiteral(
            OntologyValue.XML_TIME_FORMAT.print(time), XSD_TIME_TYPE));
   }

   private final OWLDatatype XSD_DURATION_TYPE = new OWLDatatypeImpl(
         getFactory(), IRI.create("xsd:duration"));

   public OntologyValue getLiteral (ReadablePeriod duration) {
      return new OntologyValue(getFactory().getOWLLiteral(
            OntologyValue.XML_DURATION_FORMAT.print(duration),
            XSD_DURATION_TYPE));
   }

   public void addAxiom (OWLAxiom axiom) {
      manager.addAxiom(ontology, axiom);
   }

   public void addAxiomForClass (String className, OWLIndividual individual) {
      manager.addAxiom(ontology,
            getClassAssertionAxiom(getClass(className), individual));
   }

   public void removeAxiom (OWLAxiom axiom) {
      manager.removeAxiom(ontology, axiom);
   }

   public AddAxiom addAxiom (List<OWLOntologyChange> changeList, OWLAxiom axiom) {
      AddAxiom add = new AddAxiom(getOntology(), axiom);
      changeList.add(add);
      return add;
   }

   public RemoveAxiom removeAxiom (List<OWLOntologyChange> changeList,
         OWLAxiom axiom) {
      RemoveAxiom remove = new RemoveAxiom(getOntology(), axiom);
      changeList.add(remove);
      return remove;
   }

   public void applyChanges (List<OWLOntologyChange> axioms) {
      manager.applyChanges(axioms);
   }

   public void addAxiomsFromInputStream (InputStream inputStream) {
      try {
         OWLOntology tmpOntology = getManager()
               .loadOntologyFromOntologyDocument(inputStream);
         Set<OWLAxiom> axioms = tmpOntology.getAxioms();
         List<OWLOntologyChange> axiomChanges = new ArrayList<OWLOntologyChange>();
         for (OWLAxiom axiom : axioms)
            addAxiom(axiomChanges, axiom);
         applyChanges(axiomChanges);
         manager.removeOntology(tmpOntology);
      } catch (OWLOntologyCreationException e) {
         e.printStackTrace();
      }
   }

   public void addAxiomsFromFile (File file) {
      try {
         OWLOntology tmpOntology = getManager()
               .loadOntologyFromOntologyDocument(file);
         Set<OWLAxiom> axioms = tmpOntology.getAxioms();
         List<OWLOntologyChange> axiomChanges = new ArrayList<OWLOntologyChange>();
         for (OWLAxiom axiom : axioms)
            addAxiom(axiomChanges, axiom);
         applyChanges(axiomChanges);
         manager.removeOntology(tmpOntology);
      } catch (OWLOntologyCreationException e) {
         e.printStackTrace();
      }
   }

   /**
    * If B is initialProperty of A and C is throughProperty of B then C is
    * resultProperty of A
    */
   public OWLSubPropertyChainOfAxiom getOWLSubPropertyChainOfAxiom (
         OWLObjectProperty initialProperty,
         OWLObjectProperty transitiveProperty, OWLObjectProperty resultProperty) {
      return getOWLSubPropertyChainOfAxiom(
            Arrays.asList(initialProperty, transitiveProperty), resultProperty);
   }

   public OWLSubPropertyChainOfAxiom getOWLSubPropertyChainOfAxiom (
         List<? extends OWLObjectPropertyExpression> chain,
         OWLObjectProperty resultProperty) {
      return getFactory().getOWLSubPropertyChainOfAxiom(chain, resultProperty);
   }
}

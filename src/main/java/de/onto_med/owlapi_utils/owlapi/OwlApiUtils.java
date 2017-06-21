package de.onto_med.owlapi_utils.owlapi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxPrefixNameShortFormProvider;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;

public class OwlApiUtils {
	
	/**
	 * Returns subclasses of an OWLClass recursively.
	 * Use depth parameter to specifiy the maximum depth of subclasses.
	 * @param cls relevant OWLClass
	 * @param ontology Ontology where subclasses are searched in
	 * @param depth maximum deapth of subclasses, effects the amount of recursions.
	 * @return List of OWLClasses which are subclasses of the specified class.
	 */
	public static List<OWLClass> getSubClassesRecursive(OWLClass cls, OWLOntology ontology, int depth) {
		List<OWLClass> subClasses = new ArrayList<OWLClass>();
		
		if (depth < 0) return subClasses;
		
		for (OWLClassExpression child : EntitySearcher.getSubClasses(cls, ontology)) {
			subClasses.add(child.asOWLClass());
			subClasses.addAll(getSubClassesRecursive(child.asOWLClass(), ontology, depth - 1));
		}
		
		return subClasses;
	}
	
	/**
	 * Transferes an OWLClass from one ontology into another, with all annotations and superclass axioms.
	 * @param cls class to transfere
	 * @param source source ontology
	 * @param target target ontology
	 * @throws Exception if source and target ontologies do not share ontology manager
	 */
	public static void transfereOwlClass(OWLClass cls, OWLOntology source, OWLOntology target) throws InputMismatchException {
		OWLOntologyManager manager = source.getOWLOntologyManager();
		if (!manager.equals(target.getOWLOntologyManager()))
			throw new InputMismatchException("");
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		manager.addAxiom(target, factory.getOWLDeclarationAxiom(cls));
		manager.addAxioms(target, new HashSet<OWLAnnotationAssertionAxiom>(EntitySearcher.getAnnotationAssertionAxioms(cls, source)));
		
		for (OWLClassExpression subclass : EntitySearcher.getSuperClasses(cls, source)) {
			manager.addAxiom(target, factory.getOWLSubClassOfAxiom(cls, subclass));
		}
	}
	
	/**
	 * Converts a string to the respective class expression.
	 * @param string class expression as string
	 * @param ontology respective ontology
	 * @return class expression as OWLClassExpression object
	 */
	public static OWLClassExpression convertStringToClassExpression(String string, OWLOntology ontology) {
        ManchesterOWLSyntaxParserImpl parser = (ManchesterOWLSyntaxParserImpl) OWLManager.createManchesterParser();
        OWLEntityChecker owlEntityChecker = new ShortFormEntityChecker(getShortFormProvider(ontology));
		parser.setOWLEntityChecker(owlEntityChecker);
        parser.setDefaultOntology(ontology);

        return parser.parseClassExpression(string);
    }
	
	/**
	 * Returns a HermiT reasoner for the provided ontology.
	 * @param ontology an ontology
	 * @return the HermiT reasoner
	 */
	@SuppressWarnings("deprecation")
	public static OWLReasoner getHermiTReasoner(OWLOntology ontology) {
		return new Reasoner.ReasonerFactory().createReasoner(ontology);
	}
	
	/**
	 * Returns the label of an OWLEntity object.
	 * @param entity the ontological entity
	 * @return the label
	 */
	public static String getLabel(OWLEntity entity, OWLOntology ontology) {
		for (OWLAnnotation a : EntitySearcher.getAnnotationObjects(entity, ontology)) {
			if (a.getProperty().isLabel() && a.getValue() instanceof OWLLiteral)
				return ((OWLLiteral) a.getValue()).getLiteral();
		}
		return XMLUtils.getNCNameSuffix(entity.getIRI());
	}
	
	/**
	 * Returns the literal for a given string value and string class name.
	 * @param value string representation of the value to convert
	 * @param className string representation of the class
	 * @param manager respective OWLOntologyManager
	 * @return retrived OWLLiteral
	 */
	public static OWLLiteral getLiteralForValueAndClassName(String value, String className, OWLOntologyManager manager) {
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		switch (className) {
			case "int":     return factory.getOWLLiteral(Integer.valueOf(value));
			case "boolean": return factory.getOWLLiteral(Boolean.valueOf(value));
			case "double":  return factory.getOWLLiteral(Double.valueOf(value));
			case "float":   return factory.getOWLLiteral(Float.valueOf(value));
			case "decimal":	return factory.getOWLLiteral(Double.valueOf(value));
			default:		return factory.getOWLLiteral(value);
		}
	}
	
	/**
	 * Returns the number of entities in the specified ontology (including imports) with respective type.
	 * @param cls one of the following classes: OWLObjectProperty, OWLLOgicalAxiom, OWLIndividual, OWLDataProperty, OWLClass, OWLAxiom, OWLAnnotationProperty
	 * @param ontology the ontology
	 * @return number of entities
	 */
	public static int countEntities(Class<?> cls, OWLOntology ontology) {
		if (cls.equals(OWLObjectProperty.class)) {
			return ontology.getObjectPropertiesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLLogicalAxiom.class)) {
			return ontology.getLogicalAxiomCount(Imports.INCLUDED);
		} else if (cls.equals(OWLIndividual.class)) {
			return ontology.getIndividualsInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLDataProperty.class)) {
			return ontology.getDataPropertiesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLClass.class)) {
			return ontology.getClassesInSignature(Imports.INCLUDED).size();
		} else if (cls.equals(OWLAxiom.class)) {
			return ontology.getAxiomCount(Imports.INCLUDED);
		} else if (cls.equals(OWLAnnotationProperty.class)) {
			return ontology.getAnnotationPropertiesInSignature(Imports.INCLUDED).size();
		} else {
			return 0;
		}
	}
	
	
	
	private static BidirectionalShortFormProvider getShortFormProvider(OWLOntology ontology) {
		OWLOntologyManager manager = ontology.getOWLOntologyManager();
        return new BidirectionalShortFormProviderAdapter(
        	manager.getOntologies(),
        	new ManchesterOWLSyntaxPrefixNameShortFormProvider(manager.getOntologyFormat(ontology))
        );
    }
}
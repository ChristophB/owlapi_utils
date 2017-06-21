package de.onto_med.owlapi_utils.owlapi;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.search.EntitySearcher;

public class TestOwlApiUtils {
	private static final String fileName = "duo.owl";
	private static File file;
	private static OWLOntologyManager manager;
	private static OWLDataFactory factory;
	private static OWLOntology ontology;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ClassLoader classLoader = TestOwlApiUtils.class.getClassLoader();
		file = new File(classLoader.getResource(fileName).getPath());
		
		manager  = OWLManager.createOWLOntologyManager();
		factory  = manager.getOWLDataFactory();
		ontology = manager.loadOntologyFromOntologyDocument(file);
	}

	@Test
	public void testGetSubClassesRecursive() {
		OWLClass cls = factory.getOWLClass(IRI.create("http://www.lha.org/duo#File"));
		assertEquals(3, OwlApiUtils.getSubClassesRecursive(cls, ontology, 0).size());
		assertEquals(4, OwlApiUtils.getSubClassesRecursive(cls, ontology, 1).size());
	}
	
	@Test
	public void testTransfereOWLClass() throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		OWLOntology target = manager.createOntology(IRI.create("http://www.example.org/example"));
		OWLClass cls       = factory.getOWLClass(IRI.create("http://www.lha.org/duo#Excel"));
		
		OwlApiUtils.transfereOwlClass(cls, ontology, target);
		OWLClass cls2 = target.getClassesInSignature().iterator().next();
		
		assertEquals(cls, cls2);
		assertEquals(EntitySearcher.getAnnotations(cls, ontology), EntitySearcher.getAnnotations(cls2, target));
	}
	
	@Test
	public void testConvertStringToClassExpression() {
		OWLClass cls = factory.getOWLClass(IRI.create("http://www.lha.org/duo#Excel"));
		
		assertEquals(cls, OwlApiUtils.convertStringToClassExpression("duo:Excel", ontology));
	}
	
	@Test
	public void testGetHermiTReasoner() {
		assertTrue(OwlApiUtils.getHermiTReasoner(ontology) instanceof OWLReasoner);
	}
	
	@Test
	public void testGetLabel() {
		OWLClass excel = factory.getOWLClass(IRI.create("http://www.lha.org/duo#Excel"));
		OWLClass file  = factory.getOWLClass(IRI.create("http://www.lha.org/duo#File"));
		assertEquals("Excel", OwlApiUtils.getLabel(excel, ontology));
		assertEquals(XMLUtils.getNCNameSuffix("http://lha.org/duo#File"), OwlApiUtils.getLabel(file, ontology));
	}
	
	@Test
	public void testGetLiteralForValueAndClassName() {
		assertTrue(OwlApiUtils.getLiteralForValueAndClassName("1", "int", manager).isInteger());
		assertTrue(OwlApiUtils.getLiteralForValueAndClassName("-1.5", "double", manager).isDouble());
		assertTrue(OwlApiUtils.getLiteralForValueAndClassName("2.5", "float", manager).isFloat());
		assertTrue(OwlApiUtils.getLiteralForValueAndClassName("true", "boolean", manager).isBoolean());
	}
	
	@Test
	public void testCountEntities() {
		assertEquals(0, OwlApiUtils.countEntities(OWLObjectProperty.class, ontology));
		assertEquals(4, OwlApiUtils.countEntities(OWLLogicalAxiom.class, ontology));
		assertEquals(0, OwlApiUtils.countEntities(OWLIndividual.class, ontology));
		assertEquals(0, OwlApiUtils.countEntities(OWLDataProperty.class, ontology));
		assertEquals(8, OwlApiUtils.countEntities(OWLClass.class, ontology));
		assertEquals(49, OwlApiUtils.countEntities(OWLAxiom.class, ontology));
		assertEquals(16, OwlApiUtils.countEntities(OWLAnnotationProperty.class, ontology));
	}

}

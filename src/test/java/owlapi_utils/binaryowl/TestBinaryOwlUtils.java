package owlapi_utils.binaryowl;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentParserFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class TestBinaryOwlUtils {
	private final static String binaryFileName = "root-ontology.binary";
	private static File file;
	private static String fileUrl;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ClassLoader classLoader = TestBinaryOwlUtils.class.getClassLoader();
		file    = new File(classLoader.getResource(binaryFileName).getFile());
		fileUrl = file.getAbsolutePath();
	}
	
	@Test
	public void testGetOwlOntologyManager() {
		OWLOntologyManager manager = BinaryOwlUtils.getOwlOntologyManager();
		
		assertNotSame(null, manager);
		assertFalse(manager.getOntologyParsers().isEmpty());
		assertTrue(manager.getOntologyParsers().iterator().next() instanceof BinaryOWLOntologyDocumentParserFactory);
	}
	
	@Test
	public void testGetOntologyIriFromBinaryOwl() {
		assertEquals("http://www.lha.org/duo", BinaryOwlUtils.getOntologyIriFromBinaryOwl(fileUrl));
	}

}

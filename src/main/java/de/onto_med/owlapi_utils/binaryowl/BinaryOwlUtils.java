package de.onto_med.owlapi_utils.binaryowl;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.binaryowl.BinaryOWLOntologyDocumentPreamble;
import org.semanticweb.binaryowl.BinaryOWLParseException;
import org.semanticweb.binaryowl.owlapi.BinaryOWLOntologyDocumentParserFactory;
import org.semanticweb.binaryowl.stream.BinaryOWLInputStream;
import org.semanticweb.binaryowl.stream.BinaryOWLStreamUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLParserFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;


/**
 * This class provides some convenient class methods for BinaryOWL file handling.
 * @author Christoph Beger
 *
 */
public class BinaryOwlUtils {
	
	/**
	 * Initializes an OWLOntologyManager for BinaryOWL files.
	 * @return OWLOntologyManager with BinaryOWLOntologyDocumentParser
	 */
	public static OWLOntologyManager getOwlOntologyManager() {
		OWLOntologyManager manager            = OWLManager.createOWLOntologyManager();
		Set<OWLParserFactory> parserFactories = new HashSet<>();
		
		parserFactories.add(new BinaryOWLOntologyDocumentParserFactory());
		manager.setOntologyParsers(parserFactories);
		
		return manager;
	}
	
	/**
	 * Returns the IRI of a BinaryOWL ontology or null.
	 * @param path The path to the BinaryOWL file
	 * @return IRI as String or null
	 */
	public static String getOntologyIriFromBinaryOwl(String path) {
		DataInputStream di      = null;
		BinaryOWLInputStream bi = null;
		
		try {
			di = BinaryOWLStreamUtil.getDataInputStream(new FileInputStream(path));
			bi = new BinaryOWLInputStream(
				di,
				OWLManager.getOWLDataFactory(),
				new BinaryOWLOntologyDocumentPreamble(di).getFileFormatVersion()
			);
			
			ArrayList<String> strings = new ArrayList<>();
			
			boolean newWord = true;
			int current;
			while ((current = bi.read()) != -1 && strings.size() <= 3) {
				String string = String.valueOf((char) current);
				if (!string.matches("[\\p{Graph}]") || string.matches("\"")) {
					newWord = true;
					continue;
				}
				
				if (newWord) {
					strings.add("" + (char) current);
					newWord = false;
				} else
					strings.set(strings.size() - 1, strings.get(strings.size() - 1) + (char) current);
			}
			return strings.get(1) + strings.get(2);
		} catch (BinaryOWLParseException | IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (di != null) di.close();
				if (bi != null) bi.close();
			} catch (IOException ignored) { }
		}
		
		return null;
	}
}

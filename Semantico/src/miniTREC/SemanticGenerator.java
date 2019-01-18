package miniTREC;

import java.io.File;
import java.util.ArrayList;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class SemanticGenerator {

	public static void main(String[] args) {
		String rdf = null;
		String skos = null;
		String docs = null;
		for (int i = 0; i < args.length; i++) {
			if ("-rdf".equals(args[i])) {
				rdf = args[i + 1];
				i++;
			} else if ("-skos".equals(args[i])) {
				skos = args[i + 1];
				i++;
			} else if ("-docs".equals(args[i])) {
				docs = args[i + 1];
				i++;
			}
		}
		// Directorio con los documentos
		File dir = new File(docs);
		// Modelo final que se escribira
		Model modelo = ModelFactory.createDefaultModel();
		// Modelo de skos
		Model modeloSkos = FileManager.get().loadModel(skos);
		// Iterador sobre las tripletas del modelo skos
		StmtIterator it = modeloSkos.listStatements();
		// Añadimos las propiedades de skos al modelo final
		Property narrower = modelo.createProperty("narrower");
		Property prefLabel = modelo.createProperty("prefLabel");
		Property altLabel = modelo.createProperty("altLabel");
		Property broader = modelo.createProperty("broader");
		String aux = "";
		Resource nuevo = null;
		ArrayList<String> conceptos = new ArrayList<String>();
		while (it.hasNext()) {
			Statement st = it.next();
			if (!aux.equals(st.getSubject().getURI())) {
				nuevo = modelo.createResource(st.getSubject().getURI());
			}
			switch (st.getPredicate().toString()) {
			case "http://www.w3.org/TR/2009/NOTE-skos-primer-20090818/narrower":
				nuevo.addProperty(narrower, st.getObject());
				break;
			case "http://www.w3.org/TR/2009/NOTE-skos-primer-20090818/altLabel":
				nuevo.addProperty(altLabel, st.getObject());
				break;
			case "http://www.w3.org/TR/2009/NOTE-skos-primer-20090818/prefLabel":
				nuevo.addProperty(prefLabel, st.getObject());
				break;
			case "http://www.w3.org/TR/2009/NOTE-skos-primer-20090818/broader":
				nuevo.addProperty(broader, st.getObject());
				break;
			}
			if (st.getObject().isLiteral()) {
				conceptos.add(st.getSubject().getURI());
				conceptos.add(st.getLiteral().toString());
			}
			aux = st.getSubject().getURI();
		}
		File output = new File(rdf);
		generarRDF(dir, conceptos, output, modelo);
	}

	private static void generarRDF(File dir, ArrayList<String> conceptos, File output, Model modelo) {

	}

}

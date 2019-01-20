package miniTREC;

import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SemanticGenerator {

	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
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
		File[] file = dir.listFiles();
		String uri = "http://www.equipo12.com/";
		Model model = Modelo.generar();
		for (int i = 0; i < file.length; i++) {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			org.w3c.dom.Document doc = dBuilder.parse(file[i]);
			// Sacamos atributos
			NodeList nList = doc.getElementsByTagName("dc:date");
			Node nNode = nList.item(0);
			int anyo = Integer.parseInt(nNode.getTextContent());
			nList = doc.getElementsByTagName("dc:title");
			nNode = nList.item(0);
			String titulo = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:desciption");
			nNode = nList.item(0);
			String descripcion = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:publisher");
			nNode = nList.item(0);
			String publisher = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:rights");
			nNode = nList.item(0);
			String rights = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:type");
			nNode = nList.item(0);
			String type = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:language");
			nNode = nList.item(0);
			String language = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:format");
			nNode = nList.item(0);
			String format = nNode.getTextContent();
			nList = doc.getElementsByTagName("dc:creator");
			List<String> autores = new ArrayList<String>();
			for (int j = 0; j < nList.getLength(); j++)
				autores.add(nList.item(j).getTextContent());
			nList = doc.getElementsByTagName("dc:subject");
			List<String> temas = new ArrayList<String>();
			for (int j = 0; j < nList.getLength(); j++)
				temas.add(nList.item(j).getTextContent());
			// Convertimos los datos en un formato determinado
			titulo = deAccent(titulo).toLowerCase();
			descripcion = deAccent(descripcion).toLowerCase();
			for (String subject : temas)
				subject = deAccent(subject).toLowerCase();
			publisher = deAccent(publisher).toLowerCase();
			// Añadimos los nuevos recursos
			Resource trabajo = model.createResource(uri + file[i].getName());
			trabajo.addLiteral(model.getProperty(uri + "title"), titulo);
			trabajo.addLiteral(model.getProperty(uri + "description"), descripcion);
			trabajo.addLiteral(model.getProperty(uri + "rights"), rights);
			trabajo.addLiteral(model.getProperty(uri + "format"), format);
			trabajo.addLiteral(model.getProperty(uri + "language"), language);
			trabajo.addLiteral(model.getProperty(uri + "publisher"), publisher);
			trabajo.addLiteral(model.getProperty(uri + "type"), type);
			trabajo.addLiteral(model.getProperty(uri + "date"), anyo);
			for (String person : autores) {
				String[] aux1 = person.split(" ");
				String name = aux1[2];
				String apellido1 = aux1[0];
				String apellido2 = aux1[1].replaceAll(",", "");
				Resource autor = model.createResource(uri + name + "_" + apellido1 + "_" + apellido2);
				autor.addLiteral(model.getProperty(uri + "name"), name);
				autor.addLiteral(model.getProperty(uri + "apellido1"), apellido1);
				autor.addLiteral(model.getProperty(uri + "apellido2"), apellido2);
				trabajo.addLiteral(model.getProperty(uri + "creator"), autor);
			}
			for (String tema : temas)
				trabajo.addLiteral(model.getProperty(uri + "concept"),
						model.createResource(uri + deAccent(tema).toLowerCase()));
		}
	}

	private static String deAccent(String str) {
		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
		return pattern.matcher(nfdNormalizedString).replaceAll("");
	}
}

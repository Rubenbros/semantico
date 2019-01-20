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
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.SKOS;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class SemanticGenerator {

	@SuppressWarnings("unlikely-arg-type")
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
		Model modeloSkos = FileManager.get().loadModel(skos,"TTL");
		// Iterador sobre para identificar todos los conceptos
		ArrayList<Concept> conceptos = new ArrayList<Concept>();
		ResIterator it = modeloSkos.listResourcesWithProperty(RDF.type, SKOS.Concept);
		while (it.hasNext()) {
			Resource concept = it.next();
			String path[] = concept.getURI().split("/");
			Concept nuevo = new Concept(path[path.length - 1]);
			conceptos.add(nuevo);
		}
		// Añadimos las propiedades de skos al modelo final
		StmtIterator it1 = modeloSkos.listStatements();
		Property narrower = modelo.createProperty("narrower");
		Property prefLabel = modelo.createProperty("prefLabel");
		Property altLabel = modelo.createProperty("altLabel");
		Property broader = modelo.createProperty("broader");
		String aux = "";
		Concept nuevo = null;
		while (it1.hasNext()) {
			Statement st = it1.next();
			if (!aux.equals(st.getSubject().getURI())) {
				String[] aux2 = st.getSubject().getURI().split("/");
				String name=aux2[aux2.length-1];
				for(Concept i : conceptos)
					if(i.equals(name)) {
						nuevo = i;
						break;
					}
			}
			switch (st.getPredicate().toString()) {
			case "http://www.w3.org/2004/02/skos/core#narrower":
				String[] aux2 = st.getObject().asResource().getURI().split("/");
				for(Concept i : conceptos)
					if(i.equals(aux2[aux2.length-1])) {
						nuevo.addNarrower(i);
						break;
					}
				break;
			case "http://www.w3.org/2004/02/skos/core#altLabel":
				nuevo.addAltLabel(st.getObject().toString());
				break;
			case "http://www.w3.org/2004/02/skos/core#prefLabel":
				nuevo.addPrefLabel(st.getObject().toString());
				break;
			case "http://www.w3.org/2004/02/skos/core#broader":
				String[] aux3 = st.getObject().asResource().getURI().split("/");
				for(Concept i : conceptos)
					if(i.equals(aux3[aux3.length-1])) {
						nuevo.addNarrower(i);
						break;
					}
				break;
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
			nList = doc.getElementsByTagName("dc:description");
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

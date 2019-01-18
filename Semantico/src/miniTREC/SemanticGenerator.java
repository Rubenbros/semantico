package miniTREC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		generarRDF(modelo, conceptos, dir, output);
	}

	private static void generarRDF(Model modelo, ArrayList<String> conceptos, File dir, File output) {
		// do not try to index dirs that cannot be read
		if (dir.canRead()) {
			if (dir.isDirectory()) {
				File[] dirs = dir.listFiles();
				// an IO error could occur
				if (dirs != null) {
					for (int i = 0; i < dirs.length; i++) {
						generarRDF(modelo, conceptos, dirs[i], output);
					}
					// ESCRIBIR FICHERO TTL
					try {
						modelo.write(new FileOutputStream(output, false), "TTL");
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("FIN");
				}
			} else {
				FileInputStream fis;
				try {
					fis = new FileInputStream(dir);
				} catch (FileNotFoundException fnfe) {
					// at least on windows, some temporary dirs raise this
					// exception with an "access denied" message
					// checking if the dir can be read doesn't help
					return;
				}

				try {

					String path = dir.getPath();
					String titulo = null;
					int fecha = 0;
					String descripcion = null;
					String materias = null;
					int tipo = 0;
					String tipoTrab = null;

					ArrayList<String> creatorS = new ArrayList<String>();

					// RDF FECHA
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					org.w3c.dom.Document doc2 = dBuilder.parse(dir);

					doc2.getDocumentElement().normalize();

					NodeList nList = doc2.getElementsByTagName("dc:date");
					Node nNode = nList.item(0);
					if (nNode != null) {
						String texto = nNode.getTextContent();
						fecha = Integer.parseInt(texto);
					}

					// RDF TITULO
					nList = doc2.getElementsByTagName("dc:title");
					for (int i = 0; i < nList.getLength(); i++) {
						nNode = nList.item(i);
						titulo = nNode.getTextContent();
					}
					titulo = titulo.toLowerCase();
					titulo = cleanString(titulo);
					// RDF DESCRIPCION
					nList = doc2.getElementsByTagName("dc:description");
					for (int i = 0; i < nList.getLength(); i++) {
						nNode = nList.item(i);
						descripcion = nNode.getTextContent();
					}

					descripcion = descripcion.toLowerCase();
					descripcion = cleanString(descripcion);

					// RDF TEMA

					nList = doc2.getElementsByTagName("dc:subject");
					for (int i = 0; i < nList.getLength(); i++) {
						nNode = nList.item(i);
						materias = nNode.getTextContent();
					}
					if (materias != null) {
						materias = materias.toLowerCase();
						materias = cleanString(materias);
					} else {
						materias = "";
					}
					// RDF TIPO
					nList = doc2.getElementsByTagName("dc:type");
					for (int i = 0; i < nList.getLength(); i++) {
						nNode = nList.item(i);
						tipoTrab = nNode.getTextContent();
					}
					if (tipoTrab.equals("info:eu-repo/semantics/masterThesis")) {
						tipo = 1;
					} else if (tipoTrab.equals("info:eu-repo/semantics/bachelorThesis")) {
						tipo = 0;
					} else {
						tipo = 2;
					}
					// RDF CREADOR
					nList = doc2.getElementsByTagName("dc:creator");
					for (int i = 0; i < nList.getLength(); i++) {
						nNode = nList.item(i);
						creatorS.add(nNode.getTextContent());
					}

					// TEMA DEL DOCUMENTO
					ArrayList<String> al = new ArrayList<String>();
					for (int i = 1; i < conceptos.size(); i = i + 2) {
						if (titulo.contains(conceptos.get(i).toLowerCase())
								|| materias.contains(conceptos.get(i).toLowerCase())
								|| (descripcion.contains(conceptos.get(i).toLowerCase()))) {
							al.add(conceptos.get(i - 1));
						}
					}
					if (al.size() == 0) {
						al.add("http://www.equipo03.com/Otros");
					}
					// Crear Modelo
					Model modeloPro = generarModelo(path, al, fecha, creatorS, tipo);
					if (modeloPro.isEmpty()) {
						throw new Exception();
					}
					// Añadir modelo al modelo final
					modelo.add(modeloPro);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					fis.close();
				}
			}
		}
	}

}

package miniTREC;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.SKOS;
import org.apache.jena.vocabulary.XSD;

public class Modelo {
	private static String uri = "http://www.equipo12.com/";
	private static String skos = "skos.n12";
	public static Model generar() {
		Model model = FileManager.get().loadModel(skos,"TTL");
		Property concept = model.createProperty(uri + "concept");
		concept.addProperty(RDF.type, RDF.Property);
		concept.addProperty(RDFS.domain,model.createResource( uri + "trabajo"));
		concept.addProperty(RDFS.range, SKOS.Concept);
		Property creator = model.createProperty(uri + "creator");
		creator.addProperty(RDF.type, RDF.Property);
		creator.addProperty(RDFS.domain,model.createResource( uri + "trabajo"));
		creator.addProperty(RDFS.range,model.createResource( uri + "autor"));
		Property date = model.createProperty(uri + "date");        
		date.addProperty(RDF.type, RDF.Property);
		date.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		date.addProperty(RDFS.range, XSD.nonNegativeInteger);
		Property name = model.createProperty(uri + "name");
		name.addProperty(RDF.type, RDF.Property);
		name.addProperty(RDFS.domain,model.createResource( uri + "autor"));
		name.addProperty(RDFS.range, XSD.xstring);
		Property apellido1 = model.createProperty(uri + "apellido1");
		apellido1.addProperty(RDF.type, RDF.Property);
		apellido1.addProperty(RDFS.domain,model.createResource( uri + "autor"));
		apellido1.addProperty(RDFS.range, XSD.xstring);
		Property apellido2 = model.createProperty(uri + "apellido2");
		apellido2.addProperty(RDF.type, RDF.Property);
		apellido2.addProperty(RDFS.domain,model.createResource( uri + "autor"));
		apellido2.addProperty(RDFS.range, XSD.xstring);
		Property title = model.createProperty(uri + "title");
		title.addProperty(RDF.type, RDF.Property);
		title.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		title.addProperty(RDFS.range, XSD.xstring);
		Property rights = model.createProperty(uri + "rights");
		rights.addProperty(RDF.type, RDF.Property);
		rights.addProperty(RDFS.domain,model.createResource( uri + "trabajo"));
		rights.addProperty(RDFS.range,XSD.xstring);
		Property type = model.createProperty(uri + "type");
		type.addProperty(RDF.type, RDF.Property);
		type.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		type.addProperty(RDFS.range, XSD.xstring);
		Property format = model.createProperty(uri + "format");
		format.addProperty(RDF.type, RDF.Property);
		format.addProperty(RDFS.domain,model.createResource( uri + "trabajo"));
		format.addProperty(RDFS.range, XSD.xstring);
		Property language = model.createProperty(uri + "language");
		language.addProperty(RDF.type, RDF.Property);
		language.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		language.addProperty(RDFS.range, XSD.xstring);
		Property publisher = model.createProperty(uri + "publisher");
		publisher.addProperty(RDF.type, RDF.Property);
		publisher.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		publisher.addProperty(RDFS.range, XSD.xstring);
		Property description = model.createProperty(uri + "description");
		description.addProperty(RDF.type, RDF.Property);
		description.addProperty(RDFS.domain, model.createResource(uri + "trabajo"));
		description.addProperty(RDFS.range, XSD.xstring);
		
		Resource trabajo = model.createResource(uri+"trabajo");
		trabajo.addProperty(RDF.type, RDFS.Class);
		Resource autor = model.createResource(uri + "autor");
		autor.addProperty(RDF.type, RDFS.Class);
		model.write(System.out);
		return model;
	}
	public static void main(String[] args) {
		generar();
	}
}

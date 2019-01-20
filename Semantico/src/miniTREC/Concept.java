package miniTREC;

import java.util.ArrayList;
import java.util.List;

public class Concept {
	List<Concept> broader=new ArrayList<Concept>();
	List<Concept> narrower=new ArrayList<Concept>();
	List<String> prefLabel=new ArrayList<String>();
	List<String> altLabel=new ArrayList<String>();
	private String base;
	
	public Concept(String base) {
		this.base=base;
	}
	
	public String isRelated(String concept) {
		for(Concept i : broader)
			if(i.equals(concept)) {
				return i.getBase();
			}
		for(Concept i : narrower)
			if(i.equals(concept)) {
				return i.getBase();
			}
		if(prefLabel.contains(concept))return base;
		else if(altLabel.contains(concept))return base;
		else if(base == concept)return base;
		else return "";
	}
	
	public String getBase() {
		return base;
	}

	public void addBroader(Concept concept) {
		broader.add(concept);
	}
	public void addNarrower(Concept concept) {
		narrower.add(concept);
	}
	public void addPrefLabel(String concept) {
		prefLabel.add(concept);
	}
	public void addAltLabel(String concept) {
		altLabel.add(concept);
	}
	@Override
	public boolean equals(Object concept) {
		String cmp = (String) concept;
		return this.base.equals(cmp);
	}
}

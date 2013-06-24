package de.shop.data;

import static de.shop.ShopApp.jsonBuilderFactory;

import java.io.Serializable;

import javax.json.JsonObject;

public class Artikel implements JsonMappable, Serializable {
	private static final long serialVersionUID = 1293068472891525321L;
	
	public Long id;
	public String bezeichnung;
	public int version;
	public boolean ausgesondert = false;

	public Artikel() {
		super();
	}
	
	public Artikel(Long id, String bezeichnung) {
		super();
		this.id = id;
		this.bezeichnung = bezeichnung;
	}
	
	@Override
	public JsonObject toJsonObject() {
		return jsonBuilderFactory.createObjectBuilder()
		                         .add("id", id)
		                         .add("bezeichnung", bezeichnung)
		                         .add("version", version)
		                         .add("ausgesondert", ausgesondert)
		                         .build();
	}
	
	@Override
	public void fromJsonObject(JsonObject jsonObject) {
		id = Long.valueOf(jsonObject.getJsonNumber("id").longValue());
		bezeichnung = jsonObject.getString("bezeichnung");
		version = jsonObject.getInt("version");
		ausgesondert = jsonObject.getBoolean("ausgesondert");
	}

	@Override
	public String toString() {
		return "Artikel [id=" + id + ", name=" + bezeichnung + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((bezeichnung == null) ? 0 : bezeichnung.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Artikel other = (Artikel) obj;
		if (bezeichnung == null) {
			if (other.bezeichnung != null)
				return false;
		} else if (!bezeichnung.equals(other.bezeichnung))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public void updateVersion() {
		++version;
		
	}
}

package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class InvalidKundeException extends AbstractKundeValidationException {
	private static final long serialVersionUID = 4255133082483647701L;

	private Long id;
	private String nachname;
	private String vorname;

	public InvalidKundeException(AbstractKunde kunde,
			                     Collection<ConstraintViolation<AbstractKunde>> violations) {
		super(violations);
		if (kunde != null) {
			this.id = kunde.getId();
			this.nachname = kunde.getNachname();
			this.vorname = kunde.getVorname();
		}
	}
	
	
	public InvalidKundeException(Long id, Collection<ConstraintViolation<AbstractKunde>> violations) {
		super(violations);
		this.id = id;
	}
	
	public InvalidKundeException(String nachname, Collection<ConstraintViolation<AbstractKunde>> violations) {
		super(violations);
		this.nachname = nachname;
	}
	
	public Long getId() {
		return id;
	}
	public String getNachname() {
		return nachname;
	}
	public String getVorname() {
		return vorname;
	}
	
	@Override
	public String toString() {
		return "{nachname=" + nachname + ", vorname=" + vorname + "}";
	}
}

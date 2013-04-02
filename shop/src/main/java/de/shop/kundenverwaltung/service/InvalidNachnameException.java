package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class InvalidNachnameException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String nachname;
	private final Collection<ConstraintViolation<AbstractKunde>> violations;
	
	public InvalidNachnameException(String nachname, Collection<ConstraintViolation<AbstractKunde>> violations) {
		super("Ungueltiger Nachname: " + nachname + ", Violations: " + violations);
		this.nachname = nachname;
		this.violations = violations;
	}

	public String getNachname() {
		return nachname;
	}

	public Collection<ConstraintViolation<AbstractKunde>> getViolations() {
		return violations;
	}
}

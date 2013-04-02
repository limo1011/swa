package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class InvalidEmailException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final String email;
	private final Collection<ConstraintViolation<AbstractKunde>> violations;
	
	public InvalidEmailException(String email, Collection<ConstraintViolation<AbstractKunde>> violations) {
		super("Ungueltige Email: " + email + ", Violations: " + violations);
		this.email = email;
		this.violations = violations;
	}

	public String getEmail() {
		return email;
	}

	public Collection<ConstraintViolation<AbstractKunde>> getViolations() {
		return violations;
	}
}

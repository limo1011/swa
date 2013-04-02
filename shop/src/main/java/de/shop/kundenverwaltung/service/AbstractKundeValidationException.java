package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.AbstractKunde;

public abstract class AbstractKundeValidationException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -6924234959157503601L;
	private final Collection<ConstraintViolation<AbstractKunde>> violations;
	
	public AbstractKundeValidationException(Collection<ConstraintViolation<AbstractKunde>> violations) {
		super("Violations: " + violations);
		this.violations = violations;
	}
	
	public Collection<ConstraintViolation<AbstractKunde>> getViolations() {
		return violations;
	}
}

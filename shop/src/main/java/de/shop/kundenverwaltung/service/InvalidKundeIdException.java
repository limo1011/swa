package de.shop.kundenverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class InvalidKundeIdException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -8973151010781329074L;
	
	private final Long kundeId;
	private final Collection<ConstraintViolation<AbstractKunde>> violations;
	
	public InvalidKundeIdException(Long kundeId, Collection<ConstraintViolation<AbstractKunde>> violations) {
		super("Ungueltige Kunde-ID: " + kundeId + ", Violations: " + violations);
		this.kundeId = kundeId;
		this.violations = violations;
	}

	public Long getKundeId() {
		return kundeId;
	}

	public Collection<ConstraintViolation<AbstractKunde>> getViolations() {
		return violations;
	}
}

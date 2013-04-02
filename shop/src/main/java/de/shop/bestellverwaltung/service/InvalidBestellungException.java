package de.shop.bestellverwaltung.service;

import java.util.Collection;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import de.shop.bestellverwaltung.domain.Bestellung;

@ApplicationException(rollback = true)
public class InvalidBestellungException extends AbstractBestellungValidationException {
	private static final long serialVersionUID = -6534435819639712452L;
	
	private final Long id;
	
	public InvalidBestellungException(Bestellung bestellung,
			                          Collection<ConstraintViolation<Bestellung>> violations) {
		super(violations);
		this.id = bestellung == null ? null : bestellung.getId();
	}
	
	public Long getId() {
		return id;
	}
}

package de.shop.kundenverwaltung.domain;

import static de.shop.kundenverwaltung.domain.AbstractKunde.FIRMENKUNDE;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;


@Entity
@Inheritance
@DiscriminatorValue(FIRMENKUNDE)
@Cacheable
public class Firmenkunde extends AbstractKunde {
	private static final long serialVersionUID = -3962271877965533586L;

	public Firmenkunde() {
		super();
	}
	public Firmenkunde(String nachname, String vorname, String email, Date seit) {
		super(nachname, vorname, email, seit);
	}

	@Override
	public String toString() {
		return "Firmenkunde [" + super.toString() + ']';
	}
}

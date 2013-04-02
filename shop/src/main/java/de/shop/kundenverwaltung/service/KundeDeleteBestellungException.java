package de.shop.kundenverwaltung.service;

import javax.ejb.ApplicationException;

import de.shop.kundenverwaltung.domain.AbstractKunde;

@ApplicationException(rollback = true)
public class KundeDeleteBestellungException extends AbstractKundeServiceException {
	private static final long serialVersionUID = -2220390878237172144L;
	private final Long kundeId;
	private final int anzahlBestellungen;
	
	public KundeDeleteBestellungException(AbstractKunde kunde) {
		super("Kunde mit ID=" + kunde.getId() + " kann nicht geloescht werden: "
			  + kunde.getBestellungen().size() + " Bestellung(en)");
		this.kundeId = kunde.getId();
		this.anzahlBestellungen = kunde.getBestellungen().size();
	}

	public Long getKundeId() {
		return kundeId;
	}

	public int getAnzahlBestellungen() {
		return anzahlBestellungen;
	}
}

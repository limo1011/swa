package de.shop.bestellverwaltung.service;

import java.util.List;
import java.util.Locale;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.domain.AbstractKunde;

public interface BestellungService {
	public enum FetchType {
		NUR_BESTELLUNG,
		MIT_LIEFERUNGEN
	}

	Bestellung findBestellungById(Long id, FetchType fetch, Locale locale);
	List<Bestellung> findBestellungenByKunde(AbstractKunde kunde);
	AbstractKunde findKundeById(Long id, Locale locale);
	List<Bestellung> findBestellungenMitLieferungenByKunde(AbstractKunde kunde);
	Bestellung createBestellung(Bestellung bestellung, Long kundeId, Locale locale);
	Bestellung createBestellung(Bestellung bestellung, AbstractKunde kunde, Locale locale);
	List<Lieferung> findLieferungen(String nr);
	Lieferung createLieferung(Lieferung lieferung);
}

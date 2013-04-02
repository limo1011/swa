package de.shop.bestellverwaltung.service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Locale;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.util.Log;

@Decorator
@Log
public abstract class BestellungServiceMitGeschenkverpackung implements BestellungService {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());

	@Inject
	@Delegate
	@Any
	private BestellungService bs;

	@Override
	public Bestellung findBestellungById(Long id, FetchType fetch, Locale locale) {
		return bs.findBestellungById(id, fetch, locale);
	}

	@Override
	public List<Bestellung> findBestellungenByKunde(AbstractKunde kunde) {
		return bs.findBestellungenByKunde(kunde);
	}

	@Override
	public AbstractKunde findKundeById(Long id, Locale locale) {
		return bs.findKundeById(id, locale);
	}

	@Override
	public List<Bestellung> findBestellungenMitLieferungenByKunde(AbstractKunde kunde) {
		return bs.findBestellungenMitLieferungenByKunde(kunde);
	}

	@Override
	public Bestellung createBestellung(Bestellung bestellung, Long kundeId, Locale locale) {
		LOGGER.warn("Geschenkverpackung noch nicht implementiert");

		return bs.createBestellung(bestellung, kundeId, locale);
	}
	
	@Override
	public Bestellung createBestellung(Bestellung bestellung, AbstractKunde kunde, Locale locale) {
		LOGGER.warn("Geschenkverpackung noch nicht implementiert");

		return bs.createBestellung(bestellung, kunde, locale);
	}

	@Override
	public List<Lieferung> findLieferungen(String nr) {
		return bs.findLieferungen(nr);
	}

	@Override
	public Lieferung createLieferung(Lieferung lieferung) {
		return bs.createLieferung(lieferung);
	}
}

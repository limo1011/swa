package de.shop.bestellverwaltung.rest;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;

import de.shop.artikelverwaltung.rest.UriHelperArtikel;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.rest.UriHelperKunde;
import de.shop.util.Log;

@ApplicationScoped
@Log
public class UriHelperBestellung {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Inject
	private UriHelperKunde uriHelperKunde;
	
	@Inject
	private UriHelperArtikel uriHelperArtikel;
	
	public void updateUrlBestellung(Bestellung bestellung, UriInfo uriInfo) {
		// URI fuer Kunde setzen
		final AbstractKunde kunde = bestellung.getKunde();
		if (kunde != null) {
			final URI kundeUri = uriHelperKunde.getUriKunde(bestellung.getKunde(), uriInfo);
			bestellung.setKundeUri(kundeUri);
		}
		
		// URIs fuer Artikel in den Bestellpositionen setzen
		final List<Bestellposition> bestellpositionen = bestellung.getBestellpositionen();
		if (bestellpositionen != null && !bestellpositionen.isEmpty()) {
			for (Bestellposition bp : bestellpositionen) {
				final URI artikelUri = uriHelperArtikel.getUriArtikel(bp.getArtikel(), uriInfo);
				bp.setArtikelUri(artikelUri);
			}
		}
		
		// URI fuer Lieferungen setzen
		final URI uri = uriInfo.getBaseUriBuilder()
                               .path(BestellungResource.class)
                               .path(BestellungResource.class, "findLieferungenByBestellungId")
                               .build(bestellung.getId());
		bestellung.setLieferungenUri(uri);
		
		LOGGER.trace(bestellung);
	}

	public URI getUriBestellung(Bestellung bestellung, UriInfo uriInfo) {
		final URI uri = uriInfo.getBaseUriBuilder()
		                       .path(BestellungResource.class)
		                       .path(BestellungResource.class, "findBestellungById")
		                       .build(bestellung.getId());
		return uri;
	}
}

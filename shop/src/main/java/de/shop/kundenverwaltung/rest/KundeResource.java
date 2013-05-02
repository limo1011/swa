package de.shop.kundenverwaltung.rest;

import static de.shop.util.Constants.KEINE_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_FORM_URLENCODED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.Form;

import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.rest.UriHelperBestellung;
import de.shop.bestellverwaltung.service.BestellungService;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.kundenverwaltung.service.KundeService.FetchType;
import de.shop.kundenverwaltung.service.KundeService.OrderByType;
import de.shop.util.JsonFile;
import de.shop.util.LocaleHelper;
import de.shop.util.Log;
import de.shop.util.NotFoundException;
import de.shop.util.Transactional;


@Path("/kunden")
@Produces(APPLICATION_JSON)
@Consumes
@RequestScoped
@Transactional
@Log
public class KundeResource {
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	@Context
	private UriInfo uriInfo;
	
    @Context
    private HttpHeaders headers;
    
	@Inject
	private KundeService ks;
	
	@Inject
	private BestellungService bs;
	
	@Inject
	private UriHelperKunde uriHelperKunde;

	@Inject
	private UriHelperBestellung uriHelperBestellung;
	
	@Inject
	private LocaleHelper localeHelper;
	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	/**
	 * Mit der URL /kunden/{id} einen Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Kundendaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}")
	public AbstractKunde findKundeById(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);
		final AbstractKunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + id;
			throw new NotFoundException(msg);
		}

		// URIs innerhalb des gefundenen Kunden anpassen
		uriHelperKunde.updateUriKunde(kunde, uriInfo);
		
		return kunde;
	}
	
	@GET
	@Path("/prefix/id/{id:[1-9][0-9]*}")
	public Collection<Long> findIdsByPrefix(@PathParam("id") String idPrefix) {
		final Collection<Long> ids = ks.findIdsByPrefix(idPrefix);
		return ids;
	}
	

	/**
	 * Mit der URL /kunden werden alle Kunden ermittelt oder
	 * mit kunden?nachname=... diejenigen mit einem bestimmten Nachnamen.
	 * @return Collection mit den gefundenen Kundendaten
	 */
	@GET
	public Collection<AbstractKunde> findKundenByNachname(@QueryParam("nachname") @DefaultValue("") String nachname) {
		Collection<AbstractKunde> kunden = null;
		if ("".equals(nachname)) {
			kunden = ks.findAllKunden(FetchType.NUR_KUNDE, OrderByType.UNORDERED);
		}
		else {
			final Locale locale = localeHelper.getLocale(headers);
			kunden = ks.findKundenByNachname(nachname, FetchType.NUR_KUNDE, locale);
			if (kunden.isEmpty()) {
				final String msg = "Kein Kunde gefunden mit Nachname " + nachname;
				throw new NotFoundException(msg);
			}
		}
		
		// URIs innerhalb der gefundenen Kunden anpassen
		for (AbstractKunde kunde : kunden) {
			uriHelperKunde.updateUriKunde(kunde, uriInfo);
		}
		return kunden;
	}
	
	@GET
	@Path("/prefix/nachname/{nachname}")
	public Collection<String> findNachnamenByPrefix(@PathParam("nachname") String nachnamePrefix) {
		final Collection<String> nachnamen = ks.findNachnamenByPrefix(nachnamePrefix);
		return nachnamen;
	}
	
	/**
	 * Mit der URL kunden/{id}/bestellungen die Bestellungen zu eine Kunden ermitteln
	 * @param id ID des Kunden
	 * @return Objekt mit Bestellungsdaten, falls die ID vorhanden ist
	 */
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungen")
	public Collection<Bestellung> findBestellungenByKundeId(@PathParam("id") Long id) {
		final Locale locale = localeHelper.getLocale(headers);

		final AbstractKunde kunde = ks.findKundeById(id, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			throw new NotFoundException("Kein Kunde mit der ID " + id + " gefunden.");
		}
		final Collection<Bestellung> bestellungen = bs.findBestellungenByKunde(kunde);
		
		// URIs innerhalb der gefundenen Bestellungen anpassen
		for (Bestellung bestellung : bestellungen) {
			uriHelperBestellung.updateUrlBestellung(bestellung, uriInfo);
		}
		return bestellungen;
	}
	
	@GET
	@Path("{id:[1-9][0-9]*}/bestellungenIds")
	public Collection<Long> findBestellungenIdsByKundeId(@PathParam("id") Long kundeId) {
		final Collection<Bestellung> bestellungen = findBestellungenByKundeId(kundeId);
		if (bestellungen.isEmpty()) {
			final String msg = "Kein Kunde gefunden mit der ID " + kundeId;
			throw new NotFoundException(msg);
		}
		
		final int anzahl = bestellungen.size();
		final Collection<Long> bestellungenIds = new ArrayList<>(anzahl);
		for (Bestellung bestellung : bestellungen) {
			bestellungenIds.add(bestellung.getId());
		}
		
		return bestellungenIds;
	}
	

	/**
	 * Mit der URL /kunden einen Privatkunden per POST anlegen.
	 * @param kunde neuer Kunde
	 * @return Response-Objekt mit URL des neuen Privatkunden
	 */
	@POST
	@Consumes(APPLICATION_JSON)
	@Produces
	public Response createKunde(AbstractKunde kunde) {
		final Locale locale = localeHelper.getLocale(headers);
		
		kunde.setId(KEINE_ID);
		final Adresse adresse = kunde.getAdresse();
		if (adresse != null) {
			adresse.setKunde(kunde);
		}
		kunde.setBestellungenUri(null);
		kunde.setPasswordWdh(kunde.getPassword());
		
		kunde = ks.createKunde(kunde, locale);
		LOGGER.trace(kunde);
		
		final URI kundeUri = uriHelperKunde.getUriKunde(kunde, uriInfo);
		final Response response = Response.created(kundeUri).build();
		
		return response;
	}
	
	/**
	 * Mit der URL /kunden/form einen Privatkunden per POST anlegen wie in einem HTML-Formular.
	 * @param kundeForm Form-Objekt mit den neuen Daten
	 * @return Response-Objekt mit URL des neuen Privatkunden
	 */
	@Path("form")
	@POST
	@Consumes(APPLICATION_FORM_URLENCODED)
	@Produces
	public Response createPrivatkunde(@Form KundeForm kundeForm) {
		final Locale locale = localeHelper.getLocale(headers);
		
		AbstractKunde kunde = new Privatkunde();
		kunde.setNachname(kundeForm.getNachname());
		kunde.setVorname(kundeForm.getVorname());
		kunde.setEmail(kundeForm.getEmail());
		
		final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
		final String seitStr = kundeForm.getSeit();
		Date seitDate = null;
		if (seitStr != null) {
			try {
				seitDate = formatter.parse(seitStr);
			}
			catch (ParseException e) {
				throw new InvalidDateException(seitStr, e);
		}
		}
		kunde.setSeit(seitDate);
		
		final Adresse adresse = new Adresse();
		adresse.setPlz(kundeForm.getPlz());
		adresse.setOrt(kundeForm.getOrt());
		adresse.setStrasse(kundeForm.getStrasse());
		adresse.setHausnr(kundeForm.getHausnr());
		adresse.setKunde(kunde);
		kunde.setAdresse(adresse);
		
		kunde.setAgbAkzeptiert(kundeForm.isAgbAkzeptiert());
		
		kunde = ks.createKunde(kunde, locale);
		LOGGER.trace(kunde);
		LOGGER.trace(adresse);
		
		final URI kundeUri = uriHelperKunde.getUriKunde(kunde, uriInfo);
		final Response response = Response.created(kundeUri).build();
		
		return response;
	}	
	
	/**
	 * Mit der URL /kunden einen Kunden per PUT aktualisieren
	 * @param kunde zu aktualisierende Daten des Kunden
	 */
	@PUT
	@Consumes(APPLICATION_JSON)
	public void updateKunde(AbstractKunde kunde) {
		final Locale locale = localeHelper.getLocale(headers);

		// Vorhandenen Kunden ermitteln
		final AbstractKunde origKunde = ks.findKundeById(kunde.getId(), FetchType.NUR_KUNDE, locale);
		if (origKunde == null) {
			final String msg = "Kein Kunde gefunden mit der ID " + kunde.getId();
			throw new NotFoundException(msg);
		}
		LOGGER.tracef("Kunde vorher = %s", origKunde);
	
		// Daten des vorhandenen Kunden ueberschreiben
		origKunde.setValues(kunde);
		LOGGER.tracef("Kunde nachher = %s", origKunde);
		
		// Update durchfuehren
		ks.updateKunde(origKunde, locale, false);
	}
	
	
	/**
	 * Mit der URL /kunden{id} einen Kunden per DELETE l&ouml;schen
	 * @param kundeId des zu l&ouml;schenden Kunden
	 *         gel&ouml;scht wurde, weil es zur gegebenen id keinen Kunden gibt
	 */
	@Path("{id:[1-9][0-9]*}")
	@DELETE
	@Produces
	public void deleteKunde(@PathParam("id") long kundeId) {
		ks.deleteKundeById(kundeId);
	}
	
	
	@Path("{id:[1-9][0-9]*}/file")
	@POST
	@Consumes(APPLICATION_JSON)
	public Response upload(@PathParam("id") Long kundeId, JsonFile file) {
		final Locale locale = localeHelper.getLocale(headers);
		ks.setFile(kundeId, file.getBytes(), locale);
		final URI location = uriHelperKunde.getUriDownload(kundeId, uriInfo);
		return Response.created(location).build();
	}
	
	@Path("{id:[1-9][0-9]*}/file")
	@GET
	public JsonFile download(@PathParam("id") Long kundeId) throws IOException {
		final Locale locale = localeHelper.getLocale(headers);
		final AbstractKunde kunde = ks.findKundeById(kundeId, FetchType.NUR_KUNDE, locale);
		if (kunde.getFile() == null) {
			return new JsonFile(new byte[] {});
		}
		
		return new JsonFile(kunde.getFile().getBytes());
	}
}

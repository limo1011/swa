package de.shop.bestellverwaltung.service;


import static de.shop.util.Constants.KEINE_ID;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;

import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Lieferung;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.service.KundeService;
import de.shop.util.Log;
import de.shop.util.ValidatorProvider;


@Log
public class BestellungServiceImpl implements Serializable, BestellungService {
	private static final long serialVersionUID = 3365404106904200415L;
	
	@PersistenceContext
	private transient EntityManager em;
	
	@Inject
	private Logger logger;
	
	@Inject
	private ValidatorProvider validatorProvider;
	
	@Inject
	private KundeService ks;
	
	@PostConstruct
	private void postConstruct() {
		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}
	
	@PreDestroy
	private void preDestroy() {
		logger.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	/**
	 */
	@Override
	public Bestellung findBestellungById(Long id, FetchType fetch, Locale locale) {
		Bestellung bestellung = null;
		if (fetch == null || FetchType.NUR_BESTELLUNG.equals(fetch)) {
			bestellung = em.find(Bestellung.class, id);
		}
		else if (FetchType.MIT_LIEFERUNGEN.equals(fetch)) {
			try {
			bestellung = em.createNamedQuery(Bestellung.FIND_BESTELLUNG_BY_ID_FETCH_LIEFERUNGEN, Bestellung.class)
					       .setParameter(Bestellung.PARAM_ID, id)
					       .getSingleResult();
			}
			catch (NoResultException e) {
				return null;
			}
		}
		return bestellung;
	}

	/**
	 */
	@Override
	public List<Bestellung> findBestellungenByKunde(AbstractKunde kunde) {
		if (kunde == null) {
			return Collections.emptyList();
		}
		
		final List<Bestellung> bestellungen = em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID,
				                                                  Bestellung.class)
			                                    .setParameter(Bestellung.PARAM_KUNDEID, kunde.getId())
			                                    .getResultList();
		return bestellungen;
	}

	
	/**
	 */
	@Override
	public AbstractKunde findKundeById(Long id, Locale locale) {
		try {
			final AbstractKunde kunde = em.createNamedQuery(Bestellung.FIND_KUNDE_BY_ID, AbstractKunde.class)
					                      .setParameter(Bestellung.PARAM_ID, id)
					                      .getSingleResult();
			return kunde;
		}
		catch (NoResultException e) {
			return null;
		}
	}



	/**
	 */
	@Override
	public List<Bestellung> findBestellungenMitLieferungenByKunde(AbstractKunde kunde) {
		final List<Bestellung> bestellungen =
				               em.createNamedQuery(Bestellung.FIND_BESTELLUNGEN_BY_KUNDEID_FETCH_LIEFERUNGEN,
                                                   Bestellung.class)
                                 .setParameter(Bestellung.PARAM_KUNDEID, kunde.getId())
                                 .getResultList();
		return bestellungen;
	}

	
	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden.
	 * Der Kunde ist fuer den EntityManager bekannt, die Bestellung dagegen nicht. Das Zusammenbauen
	 * wird sowohl fuer einen Web Service aus auch fuer eine Webanwendung benoetigt.
	 */
	@Override
	public Bestellung createBestellung(Bestellung bestellung, Long kundeId, Locale locale) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		final AbstractKunde kunde = ks.findKundeById(kundeId, KundeService.FetchType.MIT_BESTELLUNGEN, locale);
		return createBestellung(bestellung, kunde, locale);
	}
	
	/**
	 * Zuordnung einer neuen, transienten Bestellung zu einem existierenden, persistenten Kunden.
	 * Der Kunde ist fuer den EntityManager bekannt, die Bestellung dagegen nicht. Das Zusammenbauen
	 * wird sowohl fuer einen Web Service aus auch fuer eine Webanwendung benoetigt.
	 */
	@Override
	public Bestellung createBestellung(Bestellung bestellung,
			                           AbstractKunde kunde,
			                           Locale locale) {
		if (bestellung == null) {
			return null;
		}

		// Den persistenten Kunden mit der transienten Bestellung verknuepfen
		if (!em.contains(kunde)) {
			kunde = ks.findKundeById(kunde.getId(), KundeService.FetchType.MIT_BESTELLUNGEN, locale);
		}
		bestellung.setKunde(kunde);
		kunde.addBestellung(bestellung);
		
		// Vor dem Abspeichern IDs zuruecksetzen:
		// IDs koennten einen Wert != null haben, wenn sie durch einen Web Service uebertragen wurden
		bestellung.setId(KEINE_ID);
		for (Bestellposition bp : bestellung.getBestellpositionen()) {
			bp.setId(KEINE_ID);
		}
		
		validateBestellung(bestellung, locale, Default.class);
		em.persist(bestellung);
		
		return bestellung;
	}
	
	private void validateBestellung(Bestellung bestellung, Locale locale, Class<?> groups) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<Bestellung>> violations =
			                                       validator.validate(bestellung, groups);
		if (violations != null && !violations.isEmpty()) {
			throw new InvalidBestellungException(bestellung, violations);
		}
	}
	
	/**
	 */
	@Override
	public List<Lieferung> findLieferungen(String nr) {
		final List<Lieferung> lieferungen =
				              em.createNamedQuery(Lieferung.FIND_LIEFERUNGEN_BY_LIEFERNR_FETCH_BESTELLUNGEN,
				            		              Lieferung.class)
                                .setParameter(Lieferung.PARAM_LIEFER_NR, nr)
                                .getResultList();
		return lieferungen;
	}
	
	/**
	 */
	@Override
	public Lieferung createLieferung(Lieferung lieferung) {
		if (lieferung == null) {
			return null;
		}
		
		lieferung.setId(KEINE_ID);
		em.persist(lieferung);
		return lieferung;
	}
}

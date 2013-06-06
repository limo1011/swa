package de.shop.artikelverwaltung.controller;

import static de.shop.util.Constants.JSF_INDEX;
import static de.shop.util.Constants.JSF_REDIRECT_SUFFIX;
import static de.shop.util.Messages.MessagesType.KUNDENVERWALTUNG;
import static javax.ejb.TransactionAttributeType.MANDATORY;
import static javax.ejb.TransactionAttributeType.REQUIRED;
import static javax.ejb.TransactionAttributeType.SUPPORTS;
import static javax.persistence.PersistenceContextType.EXTENDED;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.faces.context.Flash;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpSession;

import org.jboss.logging.Logger;
import org.richfaces.cdi.push.Push;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.artikelverwaltung.service.ArtikelService;
import de.shop.auth.controller.AuthController;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.Adresse;
import de.shop.kundenverwaltung.domain.HobbyType;
import de.shop.kundenverwaltung.domain.Privatkunde;
import de.shop.kundenverwaltung.service.EmailExistsException;
import de.shop.kundenverwaltung.service.InvalidKundeException;
import de.shop.util.AbstractShopException;
import de.shop.util.ConcurrentDeletedException;
import de.shop.util.Log;
import de.shop.util.Messages;
import de.shop.util.Transactional;


/**
 * Dialogsteuerung fuer die ArtikelService
 */
@Named("ac")
@RequestScoped
//@Log
@Stateful
@TransactionAttribute(SUPPORTS)
public class ArtikelController implements Serializable {
	private static final long serialVersionUID = 1564024850446471639L;

	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass());
	
	private static final String JSF_LIST_ARTIKEL = "/artikelverwaltung/listArtikel";
	private static final String FLASH_ARTIKEL = "artikel";
	private static final int ANZAHL_LADENHUETER = 5;
	
	private static final String JSF_ARTIKELVERWALTUNG = "/artikelverwaltung/";
	private static final String JSF_VIEW_ARTIKEL = JSF_ARTIKELVERWALTUNG + "viewArtikel";
	private static final String CLIENT_ID_CREATE_BEZEICHNUNG = "createArtikelForm:bezeichnung";
	private static final String JSF_SELECT_ARTIKEL = "/artikelverwaltung/selectArtikel";
	private static final String SESSION_VERFUEGBARE_ARTIKEL = "verfuegbareArtikel";
	private static final String MSG_KEY_CREATE_ARTIKEL_BEZEICHNUNG_EXISTS = "createArtikel.bezeichnungExists";

	@PersistenceContext(type = EXTENDED)
	private transient EntityManager em;
	
	private String bezeichnung;
	
	private List<Artikel> ladenhueter;

	@Inject
	private Messages messages;
	
	@Inject
	@Push(topic = "marketing")
	private transient Event<String> neuerArtikelEvent;
	
	@Inject
	@Push(topic = "updateArtikel")
	private transient Event<String> updateArtikelEvent;
	
	private Long artikelId;
	private Artikel artikel;
	private boolean geaendertArtikel; 
	
	@Inject
	private ArtikelService as;
	
	@Inject
	private Flash flash;
	
	@Inject
	private AuthController auth;
	
	@Inject
	private transient HttpSession session;
	
	private Artikel neuerArtikel = new Artikel();

	
	@PostConstruct
	private void postConstruct() {
		LOGGER.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
	}

	@PreDestroy
	private void preDestroy() {
		LOGGER.debugf("CDI-faehiges Bean %s wird geloescht", this);
	}
	
	@Override
	public String toString() {
		return "ArtikelController [bezeichnung=" + bezeichnung + "]";
	}

	public String getBezeichnung() {
		return bezeichnung;
	}

	public void setBezeichnung(String bezeichnung) {
		this.bezeichnung = bezeichnung;
	}


	public List<Artikel> getLadenhueter() {
		return ladenhueter;
	}

	public void createEmptyArtikel() {
		if (neuerArtikel != null) {
			return;
		}
		neuerArtikel = new Artikel();
	}
	
	@Transactional
	public String findArtikelByBezeichnung() {
		final List<Artikel> artikel = as.findArtikelByBezeichnung(bezeichnung);
		flash.put(FLASH_ARTIKEL, artikel);

		return JSF_LIST_ARTIKEL;
	}
	
	public Artikel getNeuerArtikel() {
		return neuerArtikel;
	}
	
	public void geaendert(ValueChangeEvent e) {
		if (geaendertArtikel) {
			return;
		}
	}
	
/*	private String createArtikelErrorMsg(AbstractShopException e) {
		final Class<? extends AbstractShopException> exceptionClass = e.getClass();
		if (exceptionClass.equals(EmailExistsException.class)) {
			messages.error(KUNDENVERWALTUNG, MSG_KEY_CREATE_ARTIKEL_BEZEICHNUNG_EXISTS, CLIENT_ID_CREATE_BEZEICHNUNG);
		}
		else if (exceptionClass.equals(InvalidKundeException.class)) {
			final InvalidKundeException orig = (InvalidKundeException) e;
			messages.error(orig.getViolations(), null);
		}
		
		return null;
	}*/
	
	@TransactionAttribute(REQUIRED)
	public String createArtikel() {
			neuerArtikel.setErzeugt(new Date());
			neuerArtikel = (Artikel) as.createArtikel(neuerArtikel);

		// Push-Event fuer Webbrowser
		neuerArtikelEvent.fire(String.valueOf(neuerArtikel.getId()));
		
		// Aufbereitung fuer viewKunde.xhtml
		artikelId = neuerArtikel.getId();
		setArtikel(neuerArtikel);
		neuerArtikel = null;  // zuruecksetzen
		
		return JSF_VIEW_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}
	
	@TransactionAttribute(REQUIRED)
	public String update() {
		auth.preserveLogin();
		
		if (!geaendertArtikel || artikel == null) {
			return JSF_INDEX;
		}
		
//		if (artikel.getClass().equals(Artikel.class)) {
//			final Artikel artikel = (Artikel) artikel;
//			final Set<HobbyType> hobbiesPrivatkunde = privatkunde.getHobbies();
//			hobbiesPrivatkunde.clear();
//			
//			for (String s : hobbies) {
//				hobbiesPrivatkunde.add(HobbyType.valueOf(s));				
//			}
//		}
		
		LOGGER.tracef("Aktualisierter Artikel: %s", artikel);
//		try {
			artikel = as.updateArtikel(artikel);
//		}
//		catch (EmailExistsException | InvalidKundeException
//			  | OptimisticLockException | ConcurrentDeletedException e) {
//			final String outcome = updateErrorMsg(e, kunde.getClass());
//			return outcome;
//		}

		// Push-Event fuer Webbrowser
		updateArtikelEvent.fire(String.valueOf(artikel.getId()));
		
		// ValueChangeListener zuruecksetzen
		geaendertArtikel = false;
		
		// Aufbereitung fuer viewKunde.xhtml
		artikelId = artikel.getId();
		
		return JSF_VIEW_ARTIKEL + JSF_REDIRECT_SUFFIX;
	}

	@Transactional
	public void loadLadenhueter() {
		ladenhueter = as.ladenhueter(ANZAHL_LADENHUETER);
	}
	
	@Transactional
	public String selectArtikel() {
		if (session.getAttribute(SESSION_VERFUEGBARE_ARTIKEL) != null) {
			return JSF_SELECT_ARTIKEL;
		}
		
		final List<Artikel> alleArtikel = as.findVerfuegbareArtikel();
		session.setAttribute(SESSION_VERFUEGBARE_ARTIKEL, alleArtikel);
		return JSF_SELECT_ARTIKEL;
	}

	public Artikel getArtikel() {
		return artikel;
	}

	public void setArtikel(Artikel artikel) {
		this.artikel = artikel;
	}
}

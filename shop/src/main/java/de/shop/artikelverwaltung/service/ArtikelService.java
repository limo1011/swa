package de.shop.artikelverwaltung.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;

import com.google.common.base.Strings;

import de.shop.artikelverwaltung.domain.Artikel;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.kundenverwaltung.service.EmailExistsException;
import de.shop.util.ConcurrentDeletedException;
import de.shop.util.Log;


@Log
public class ArtikelService implements Serializable {
	private static final long serialVersionUID = 5292529185811096603L;

	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private Logger logger;
	
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
	public List<Artikel> findVerfuegbareArtikel() {
		final List<Artikel> artikel = em.createNamedQuery(Artikel.FIND_VERFUEGBARE_ARTIKEL, Artikel.class)
				                        .getResultList();
		return artikel;
	}

	
	/**
	 */
	public Artikel findArtikelById(Long id) {
		final Artikel artikel = em.find(Artikel.class, id);
		return artikel;
	}
	
	/**
	 */
	public List<Artikel> findArtikelByBezeichnung(String bezeichnung) {
		if (Strings.isNullOrEmpty(bezeichnung)) {
			final List<Artikel> artikelListe = findVerfuegbareArtikel();
			return artikelListe;
		}
		
		final List<Artikel> artikel = em.createNamedQuery(Artikel.FIND_ARTIKEL_BY_BEZ, Artikel.class)
				                        .setParameter(Artikel.PARAM_BEZEICHNUNG, "%" + bezeichnung + "%")
				                        .getResultList();
		
		return artikel;
	}
	
	/**
	 */
	public List<Artikel> findArtikelByIds(List<Long> ids) {
		if (ids == null || ids.isEmpty()) {
			return Collections.emptyList();
		}
		
		/**
		 * SELECT a
		 * FROM   Artikel a
		 * WHERE  a.id = ? OR a.id = ? OR ...
		 */
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<Artikel> criteriaQuery = builder.createQuery(Artikel.class);
		final Root<Artikel> a = criteriaQuery.from(Artikel.class);

		final Path<Long> idPath = a.get("id");
		//final Path<String> idPath = a.get(Artikel_.id);   // Metamodel-Klassen funktionieren nicht mit Eclipse
			
		Predicate pred = null;
		if (ids.size() == 1) {
			// Genau 1 id: kein OR notwendig
			pred = builder.equal(idPath, ids.get(0));
		}
		else {
			// Mind. 2x id, durch OR verknuepft
			final Predicate[] equals = new Predicate[ids.size()];
			int i = 0;
			for (Long id : ids) {
				equals[i++] = builder.equal(idPath, id);
			}
				
			pred = builder.or(equals);
		}
		criteriaQuery.where(pred);
			
		final List<Artikel> artikel = em.createQuery(criteriaQuery).getResultList();
		return artikel;
	}
	
	/**
	 */
	public List<Artikel> ladenhueter(int anzahl) {
		final List<Artikel> artikel = em.createNamedQuery(Artikel.FIND_LADENHUETER, Artikel.class)
				                        .setMaxResults(anzahl)
				                        .getResultList();
		return artikel;
	}
	
	/**
	 */
	public Artikel createArtikel(Artikel artikel) {
		if (artikel == null) {
			return artikel;
		}
		
		em.persist(artikel);
		//event.fire(artikel);
		
		return artikel;
	}
	
	/**
	 */
	public Artikel updateArtikel(Artikel artikel) {
			if (artikel == null) {
				return null;
			}

			em.detach(artikel);
// Wurde das Objekt konkurrierend geloescht?
			Artikel tmp = findArtikelById(artikel.getId());
			if (tmp == null) {
				throw new ConcurrentDeletedException(artikel.getId());
			}
			em.detach(tmp);
			

			artikel = em.merge(artikel);   // OptimisticLockException


			return artikel;
	}
	
	
}

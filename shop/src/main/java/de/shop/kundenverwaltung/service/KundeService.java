package de.shop.kundenverwaltung.service;

import static javax.ejb.TransactionAttributeType.MANDATORY;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.validation.groups.Default;

import org.jboss.logging.Logger;

import com.google.common.base.Strings;

import de.shop.auth.service.jboss.AuthService;
import de.shop.bestellverwaltung.domain.Bestellposition;
import de.shop.bestellverwaltung.domain.Bestellposition_;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.bestellverwaltung.domain.Bestellung_;
import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.domain.AbstractKunde_;
import de.shop.kundenverwaltung.domain.PasswordGroup;
import de.shop.kundenverwaltung.domain.Wartungsvertrag;
import de.shop.util.ConcurrentDeletedException;
import de.shop.util.IdGroup;
import de.shop.util.Log;
import de.shop.util.File;
import de.shop.util.FileHelper;
import de.shop.util.NoMimeTypeException;
import de.shop.util.FileHelper.MimeType;
import de.shop.util.ValidatorProvider;

//@Stateless
//@TransactionAttribute(MANDATORY)
//@RolesAllowed({"admin","mitarbeiter","kunde"})
@Log
public class KundeService implements Serializable {
	private static final long serialVersionUID = 5654417703891549367L;
	
	public enum FetchType {
		NUR_KUNDE,
		MIT_BESTELLUNGEN,
		MIT_WARTUNGSVERTRAEGEN
	}
	
	public enum OrderByType {
		UNORDERED,
		ID
	}
	
	@PersistenceContext
	private transient EntityManager em;

	@Inject
	private transient Logger logger;
	
	@Inject
	private ValidatorProvider validatorProvider;
	
	@Inject
	private AuthService authService;
	
	@Inject
	private FileHelper fileHelper;

	@Inject
	@NeuerKunde
	private transient Event<AbstractKunde> event;
	
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
	public AbstractKunde findKundeById(Long id, FetchType fetch, Locale locale) {
		validateKundeId(id, locale);
		
		AbstractKunde kunde = null;
		
		try {
			switch (fetch) {
				case NUR_KUNDE:
					kunde = em.find(AbstractKunde.class, id);
					break;
				
				case MIT_BESTELLUNGEN:
					kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN, AbstractKunde.class)
					          .setParameter(AbstractKunde.PARAM_KUNDE_ID, id)
							  .getSingleResult();
					break;
					
				case MIT_WARTUNGSVERTRAEGEN:
					kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_ID_FETCH_WARTUNGSVERTRAEGE,
							                    AbstractKunde.class)
					          .setParameter(AbstractKunde.PARAM_KUNDE_ID, id)
							  .getSingleResult();
					break;
	
				default:
					kunde = em.find(AbstractKunde.class, id);
					break;
			}
		}
		catch (NoResultException e) {
			kunde = null;
		}
		return kunde;
	}


	private void validateKundeId(Long id, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations = validator.validateValue(AbstractKunde.class,
				                                                                           "id",
				                                                                           id,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidKundeIdException(id, violations);
	}
	
	/**
	 */
	public List<Long> findIdsByPrefix(String idPrefix) {
		if (Strings.isNullOrEmpty(idPrefix)) {
			return Collections.emptyList();
		}
		final List<Long> ids = em.createNamedQuery(AbstractKunde.FIND_IDS_BY_PREFIX, Long.class)
				                 .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, idPrefix + '%')
				                 .getResultList();
		return ids;
	}
	
	/**
	 */
	public List<AbstractKunde> findKundenByIdPrefix(Long id) {
		if (id == null) {
			return Collections.emptyList();
		}
		
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_ID_PREFIX,
				                                               AbstractKunde.class)
				                             .setParameter(AbstractKunde.PARAM_KUNDE_ID_PREFIX, id.toString() + '%')
				                             .getResultList();
		return kunden;
	}
	
	/**
	 */
	public List<AbstractKunde> findAllKunden(FetchType fetch, OrderByType order) {
		List<AbstractKunde> kunden;
		switch (fetch) {
			case NUR_KUNDE:
				kunden = OrderByType.ID.equals(order)
				        ? em.createNamedQuery(AbstractKunde.FIND_KUNDEN_ORDER_BY_ID, AbstractKunde.class)
				        	.getResultList()
				        : em.createNamedQuery(AbstractKunde.FIND_KUNDEN, AbstractKunde.class)
				            .getResultList();
				break;
			
			case MIT_BESTELLUNGEN:
				kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_FETCH_BESTELLUNGEN, AbstractKunde.class)
						   .getResultList();
				break;

			default:
				kunden = OrderByType.ID.equals(order)
		                ? em.createNamedQuery(AbstractKunde.FIND_KUNDEN_ORDER_BY_ID, AbstractKunde.class)
		                	.getResultList()
		                : em.createNamedQuery(AbstractKunde.FIND_KUNDEN, AbstractKunde.class)
		                    .getResultList();
				break;
		}
		
		return kunden;
	}
	

	/**
	 */
	public List<AbstractKunde> findKundenByNachname(String nachname, FetchType fetch, Locale locale) {
		validateNachname(nachname, locale);
		List<AbstractKunde> kunden;
		switch (fetch) {
			case NUR_KUNDE:
				kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME, AbstractKunde.class)
						   .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;
			
			case MIT_BESTELLUNGEN:
				kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
						                     AbstractKunde.class)
						   .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;

			default:
				kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_NACHNAME, AbstractKunde.class)
						   .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME, nachname)
                           .getResultList();
				break;
		}

		return kunden;
	}
	
	private void validateNachname(String nachname, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations = validator.validateValue(AbstractKunde.class,
				                                                                           "nachname",
				                                                                           nachname,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidNachnameException(nachname, violations);
	}
	
	/**
	 */
	public List<String> findNachnamenByPrefix(String nachnamePrefix) {
		final List<String> nachnamen = em.createNamedQuery(AbstractKunde.FIND_NACHNAMEN_BY_PREFIX, String.class)
				                         .setParameter(AbstractKunde.PARAM_KUNDE_NACHNAME_PREFIX, nachnamePrefix + '%')
				                         .getResultList();
		return nachnamen;
	}

	/**
	 */
	public AbstractKunde findKundeByEmail(String email, Locale locale) {
		validateEmail(email, locale);
		AbstractKunde kunde;
		try {
			kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_EMAIL, AbstractKunde.class)
					  .setParameter(AbstractKunde.PARAM_KUNDE_EMAIL, email)
					  .getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
		
		return kunde;
	}
	
	private void validateEmail(String email, Locale locale) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations = validator.validateValue(AbstractKunde.class,
				                                                                           "email",
				                                                                           email,
				                                                                           Default.class);
		if (!violations.isEmpty())
			throw new InvalidEmailException(email, violations);
	}


	/**
	 */
	public List<AbstractKunde> findKundenByPLZ(String plz) {
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_PLZ, AbstractKunde.class)
				                             .setParameter(AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ, plz)
				                             .getResultList();
		return kunden;
	}
	
	/**
	 */
	public List<AbstractKunde> findKundenBySeit(Date seit) {
		final List<AbstractKunde> kunden = em.createNamedQuery(AbstractKunde.FIND_KUNDEN_BY_DATE, AbstractKunde.class)
				                             .setParameter(AbstractKunde.PARAM_KUNDE_SEIT, seit)
				                             .getResultList();
		return kunden;
	}
	
	/**
	 */
	public List<AbstractKunde> findPrivatkundenFirmenkunden() {
		final List<AbstractKunde> kunden =
				                 em.createNamedQuery(AbstractKunde.FIND_PRIVATKUNDEN_FIRMENKUNDEN, AbstractKunde.class)
				                   .getResultList();
		return kunden;
	}

	
	/**
	 */
	public AbstractKunde findKundeByUserName(String userName) {
		AbstractKunde kunde;
		try {
			kunde = em.createNamedQuery(AbstractKunde.FIND_KUNDE_BY_USERNAME, AbstractKunde.class)
					  .setParameter(AbstractKunde.PARAM_KUNDE_USERNAME, userName)
					  .getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
		
		return kunde;
	}
	
	/**
	 */
	public List<AbstractKunde> findKundenByNachnameCriteria(String nachname) {
		// SELECT k
		// FROM   AbstractKunde k
		// WHERE  k.nachname = ?
				
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<AbstractKunde> criteriaQuery = builder.createQuery(AbstractKunde.class);
		final Root<AbstractKunde> k = criteriaQuery.from(AbstractKunde.class);

		final Path<String> nachnamePath = k.get(AbstractKunde_.nachname);
		
		final Predicate pred = builder.equal(nachnamePath, nachname);
		criteriaQuery.where(pred);

		final List<AbstractKunde> kunden = em.createQuery(criteriaQuery).getResultList();
		return kunden;
	}

	/**
	 */
	public List<AbstractKunde> findKundenMitMinBestMenge(short minMenge) {
		// SELECT DISTINCT k
		// FROM   AbstractKunde k
		//        JOIN k.bestellungen b
		//        JOIN b.bestellpositionen bp
		// WHERE  bp.anzahl >= ?
		
		final CriteriaBuilder builder = em.getCriteriaBuilder();
		final CriteriaQuery<AbstractKunde> criteriaQuery  = builder.createQuery(AbstractKunde.class);
		final Root<AbstractKunde> k = criteriaQuery.from(AbstractKunde.class);

		final Join<AbstractKunde, Bestellung> b = k.join(AbstractKunde_.bestellungen);
		final Join<Bestellung, Bestellposition> bp = b.join(Bestellung_.bestellpositionen);
		criteriaQuery.where(builder.gt(bp.<Short>get(Bestellposition_.anzahl), minMenge))
		             .distinct(true);
		
		final List<AbstractKunde> kunden = em.createQuery(criteriaQuery).getResultList();
		return kunden;
	}
	
	/**
	 */
	public AbstractKunde createKunde(AbstractKunde kunde, Locale locale) {
		if (kunde == null) {
			return kunde;
		}
	
		// Werden alle Constraints beim Einfuegen gewahrt?
		validateKunde(kunde, locale, Default.class, PasswordGroup.class);
	
		// Pruefung, ob ein solcher Kunde schon existiert
		final AbstractKunde tmp = findKundeByEmail(kunde.getEmail(), locale);
		if (tmp != null) {
			throw new EmailExistsException(kunde.getEmail());
		}
		
		// Password verschluesseln
		passwordVerschluesseln(kunde);
	
		em.persist(kunde);
		event.fire(kunde);
		
		return kunde;
	}
	
	private void validateKunde(AbstractKunde kunde, Locale locale, Class<?>... groups) {
		final Validator validator = validatorProvider.getValidator(locale);
		final Set<ConstraintViolation<AbstractKunde>> violations =
			                                          validator.validate(kunde, groups);
		
		if (!violations.isEmpty()) {
			throw new InvalidKundeException(kunde, violations);
		}
	}

	/**
	 */
	public AbstractKunde updateKunde(AbstractKunde kunde,
			                         Locale locale,
			                         boolean geaendertPassword) {
		if (kunde == null) {
			return null;
		}
		
		// Werden alle Constraints beim Modifizieren gewahrt?
		validateKunde(kunde, locale, Default.class, PasswordGroup.class, IdGroup.class);

		// kunde vom EntityManager trennen, weil anschliessend z.B. nach Id und Email gesucht wird
		em.detach(kunde);
		
		// Wurde das Objekt konkurrierend geloescht?
		AbstractKunde tmp = findKundeById(kunde.getId(), FetchType.NUR_KUNDE, locale);
		if (tmp == null) {
			throw new ConcurrentDeletedException(kunde.getId());
		}
		em.detach(tmp);
		
		// Gibt es ein anderes Objekt mit gleicher Email-Adresse?
		tmp = findKundeByEmail(kunde.getEmail(), locale);
		if (tmp != null) {
			em.detach(tmp);
			if (tmp.getId().longValue() != kunde.getId().longValue()) {
				// anderes Objekt mit gleichem Attributwert fuer email
				throw new EmailExistsException(kunde.getEmail());
			}
		}
		
		// Password verschluesseln
		if (geaendertPassword) {
			passwordVerschluesseln(kunde);
		}

		kunde = em.merge(kunde);   // OptimisticLockException
		kunde.setPasswordWdh(kunde.getPassword());
		
		return kunde;
	}

	/**
	 */
	public void deleteKunde(AbstractKunde kunde) {
		if (kunde == null) {
			return;
		}

		deleteKundeById(kunde.getId());
	}

	/**
	 */

	public void deleteKundeById(Long kundeId) {
		AbstractKunde kunde;
		try {
			kunde = findKundeById(kundeId, FetchType.MIT_BESTELLUNGEN, Locale.getDefault());
		}
		catch (InvalidKundeIdException e) {
			return;
		}
		if (kunde == null) {
			// Der Kunde existiert nicht oder ist bereits geloescht
			return;
		}

		final boolean hasBestellungen = hasBestellungen(kunde);
		if (hasBestellungen) {
			throw new KundeDeleteBestellungException(kunde);
		}

		// Kundendaten loeschen
		em.remove(kunde);
	}

	
	/**
	 * Ohne MIME Type fuer Upload bei RESTful WS
	 */
	public void setFile(Long kundeId, byte[] bytes, Locale locale) {
		final AbstractKunde kunde = findKundeById(kundeId, FetchType.NUR_KUNDE, locale);
		if (kunde == null) {
			return;
		}
		final MimeType mimeType = fileHelper.getMimeType(bytes);
		setFile(kunde, bytes, mimeType);
	}
	
	/**
	 * Mit MIME-Type fuer Upload bei Webseiten
	 */
	public void setFile(AbstractKunde kunde, byte[] bytes, String mimeTypeStr) {
		final MimeType mimeType = MimeType.get(mimeTypeStr);
		setFile(kunde, bytes, mimeType);
	}
	
	private void setFile(AbstractKunde kunde, byte[] bytes, MimeType mimeType) {
		if (mimeType == null) {
			throw new NoMimeTypeException();
		}
		
		final String filename = fileHelper.getFilename(kunde.getClass(), kunde.getId(), mimeType);
		
		// Gibt es noch kein (Multimedia-) File
		File file = kunde.getFile();
		if (file == null) {
			file = new File(bytes, filename, mimeType);
			kunde.setFile(file);
			em.persist(file);
		}
		else {
			file.set(bytes, filename, mimeType);
			em.merge(file);
		}
	}

	
	/**
	 */
	public List<Wartungsvertrag> findWartungsvertraege(Long kundeId) {
		final List<Wartungsvertrag> wartungsvertraege =
				                    em.createNamedQuery(Wartungsvertrag.FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID,
				                    		            Wartungsvertrag.class)
				                      .setParameter(Wartungsvertrag.PARAM_KUNDE_ID, kundeId)
				                      .getResultList();
		return wartungsvertraege;
	}

	
	/**
	 */
	public Wartungsvertrag createWartungsvertrag(Wartungsvertrag wartungsvertrag, AbstractKunde kunde, Locale locale) {
		if (wartungsvertrag == null) {
			return null;
		}
		
		findKundeById(kunde.getId(), FetchType.NUR_KUNDE, locale);
		em.persist(wartungsvertrag);
		return wartungsvertrag;
	}
	
	/**
	 */
	private boolean hasBestellungen(AbstractKunde kunde) {
		logger.debugf("hasBestellungen BEGINN: %s", kunde);
		
		boolean result = false;
		
		// Gibt es den Kunden und hat er mehr als eine Bestellung?
		// Bestellungen nachladen wegen Hibernate-Caching
		if (kunde != null && kunde.getBestellungen() != null && !kunde.getBestellungen().isEmpty()) {
			result = true;
		}
		
		logger.debugf("hasBestellungen ENDE: %s", result);
		return result;
	}

	
	private void passwordVerschluesseln(AbstractKunde kunde) {
		logger.debugf("passwordVerschluesseln BEGINN: %s", kunde);

		final String unverschluesselt = kunde.getPassword();
		final String verschluesselt = authService.verschluesseln(unverschluesselt);
		kunde.setPassword(verschluesselt);
		kunde.setPasswordWdh(verschluesselt);

		logger.debugf("passwordVerschluesseln ENDE: %s", verschluesselt);
	}
}

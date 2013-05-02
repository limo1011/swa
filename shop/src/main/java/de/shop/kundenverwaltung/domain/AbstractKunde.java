package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static de.shop.util.Constants.KEINE_ID;
import static de.shop.util.Constants.MIN_ID;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;
import static javax.persistence.FetchType.EAGER;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.hibernate.validator.constraints.ScriptAssert;
import org.jboss.logging.Logger;

import de.shop.auth.service.jboss.AuthService.RolleType;
import de.shop.bestellverwaltung.domain.Bestellung;
import de.shop.util.IdGroup;
import de.shop.util.File;


@Entity
@Table(name = "kunde")
@Inheritance  // Alternativen: strategy = SINGLE_TABLE (=default), TABLE_PER_CLASS, JOINED
@DiscriminatorColumn(name = "art", length = 1)
@NamedQueries({
	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN,
                query = "SELECT  k"
				        + " FROM AbstractKunde k"),
   	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN_FETCH_BESTELLUNGEN,
                query = "SELECT  DISTINCT k"
				        + " FROM AbstractKunde k LEFT JOIN FETCH k.bestellungen"),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN_ORDER_BY_ID,
		        query = "SELECT   k"
				        + " FROM  AbstractKunde k"
		                + " ORDER BY k.id"),
	@NamedQuery(name  = AbstractKunde.FIND_IDS_BY_PREFIX,
	            query = "SELECT   k.id"
				        + " FROM  AbstractKunde k"
				        + " WHERE CONCAT('', k.id) LIKE :" + AbstractKunde.PARAM_KUNDE_ID_PREFIX
				        + " ORDER BY k.id"),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN_BY_ID_PREFIX,
		        query = "SELECT   k"
		                + " FROM  AbstractKunde k"
		                + " WHERE CONCAT('', k.id) LIKE :" + AbstractKunde.PARAM_KUNDE_ID_PREFIX
		                + " ORDER BY k.id"),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN_BY_NACHNAME,
	            query = "SELECT   k"
				        + " FROM  AbstractKunde k"
	            		+ " WHERE UPPER(k.nachname) = UPPER(:" + AbstractKunde.PARAM_KUNDE_NACHNAME + ")"),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDE_BY_EMAIL,
                query = "SELECT   k"
			            + " FROM  AbstractKunde k"
            		    + " WHERE UPPER(k.email) = UPPER(:" + AbstractKunde.PARAM_KUNDE_EMAIL + ")"),
	@NamedQuery(name  = AbstractKunde.FIND_NACHNAMEN_BY_PREFIX,
   	            query = "SELECT   DISTINCT k.nachname"
				        + " FROM  AbstractKunde k "
   	            		+ " WHERE UPPER(k.nachname) LIKE UPPER(:"
   	            		+ AbstractKunde.PARAM_KUNDE_NACHNAME_PREFIX + ")"),
    @NamedQuery(name  = AbstractKunde.FIND_ALL_NACHNAMEN,
   	            query = "SELECT      DISTINCT k.nachname"
   				        + " FROM     AbstractKunde k"
   				        + " ORDER BY k.nachname"),
    @NamedQuery(name  = AbstractKunde.FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN,
	            query = "SELECT      DISTINCT k"
			            + " FROM     AbstractKunde k LEFT JOIN FETCH k.bestellungen"
			            + " WHERE    UPPER(k.nachname) = UPPER(:" + AbstractKunde.PARAM_KUNDE_NACHNAME + ")"
			            + " ORDER BY k.id"),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN,
	            query = "SELECT DISTINCT k"
			            + " FROM   AbstractKunde k LEFT JOIN FETCH k.bestellungen"
			            + " WHERE  k.id = :" + AbstractKunde.PARAM_KUNDE_ID),
   	@NamedQuery(name  = AbstractKunde.FIND_KUNDE_BY_ID_FETCH_WARTUNGSVERTRAEGE,
   	            query = "SELECT DISTINCT k"
   			            + " FROM   AbstractKunde k LEFT JOIN FETCH k.wartungsvertraege"
   			            + " WHERE  k.id = :" + AbstractKunde.PARAM_KUNDE_ID),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDEN_BY_PLZ,
	            query = "SELECT   k"
				        + " FROM  AbstractKunde k"
			            + " WHERE k.adresse.plz = :" + AbstractKunde.PARAM_KUNDE_ADRESSE_PLZ),
	@NamedQuery(name  = AbstractKunde.FIND_KUNDE_BY_USERNAME,
	            query = "SELECT   k"
				        + " FROM  AbstractKunde k"
	            		+ " WHERE CONCAT('', k.id) = :" + AbstractKunde.PARAM_KUNDE_USERNAME),
	@NamedQuery(name  = AbstractKunde.FIND_USERNAME_BY_USERNAME_PREFIX,
  	            query = "SELECT   CONCAT('', k.id)"
  				        + " FROM  AbstractKunde k"
   	            		+ " WHERE CONCAT('', k.id) LIKE :" + AbstractKunde.PARAM_USERNAME_PREFIX),

	@NamedQuery(name = AbstractKunde.FIND_KUNDEN_BY_DATE,
   			    query = "SELECT   k"
   			            + " FROM  AbstractKunde k"
   			    		+ " WHERE k.seit = :" + AbstractKunde.PARAM_KUNDE_SEIT),
   	@NamedQuery(name = AbstractKunde.FIND_PRIVATKUNDEN_FIRMENKUNDEN,
   			    query = "SELECT   k"
   			            + " FROM  AbstractKunde k"
   			    		+ " WHERE TYPE(k) IN (Privatkunde, Firmenkunde)")

})
@ScriptAssert(lang = "javascript",
	          script = "(_this.password == null && _this.passwordWdh == null)"
	                   + "|| (_this.password != null && _this.password.equals(_this.passwordWdh))",
	          message = "{kundenverwaltung.kunde.password.notEqual}",
	          groups = PasswordGroup.class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
	@Type(value = Privatkunde.class, name = AbstractKunde.PRIVATKUNDE),
	@Type(value = Firmenkunde.class, name = AbstractKunde.FIRMENKUNDE) })
public abstract class AbstractKunde implements Serializable, Cloneable {
	private static final long serialVersionUID = 4190012483483113483L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	private static final String NAME_PATTERN = "[A-Z\u00C4\u00D6\u00DC][a-z\u00E4\u00F6\u00FC\u00DF]+";
	private static final String PREFIX_ADEL = "(o'|von|von der|von und zu|van)?";
	public static final String NACHNAME_PATTERN = PREFIX_ADEL + NAME_PATTERN + "(-" + NAME_PATTERN + ")?";
	public static final int NACHNAME_LENGTH_MIN = 2;   // kann an der GUI abgefragt werden
	public static final int NACHNAME_LENGTH_MAX = 32;
	public static final int VORNAME_LENGTH_MAX = 32;
	public static final int USERNAME_LENGTH_MAX = 32;
	public static final int PASSWORD_LENGTH_MAX = 256;
	public static final int EMAIL_LENGTH_MAX = 32;
	public static final int BEMERKUNGEN_LENGTH_MAX = 2000;
	
	public static final String PRIVATKUNDE = "P";
	public static final String FIRMENKUNDE = "F";
	
	private static final String PREFIX = "AbstractKunde.";
	public static final String FIND_KUNDEN = PREFIX + "findKunden";
	public static final String FIND_KUNDEN_FETCH_BESTELLUNGEN = PREFIX + "findKundenFetchBestellungen";
	public static final String FIND_KUNDEN_ORDER_BY_ID = PREFIX + "findKundenOrderById";
	public static final String FIND_IDS_BY_PREFIX = PREFIX + "findIdsByIdPrefix";
	public static final String FIND_KUNDEN_BY_ID_PREFIX = PREFIX + "findKundenByIdPrefix";
	public static final String FIND_KUNDEN_BY_NACHNAME = PREFIX + "findKundenByNachname";
	public static final String FIND_KUNDE_BY_EMAIL = PREFIX + "findKundeByEmail";
	public static final String FIND_NACHNAMEN_BY_PREFIX = PREFIX + "findNachnamenByPrefix";
	public static final String FIND_ALL_NACHNAMEN = PREFIX + "findAllNachnamen";
	public static final String FIND_KUNDEN_BY_NACHNAME_FETCH_BESTELLUNGEN =
		                       PREFIX + "findKundenByNachnameFetchBestellungen";
	public static final String FIND_KUNDE_BY_ID_FETCH_BESTELLUNGEN = PREFIX + "findKundeByIdFetchBestellungen";
	public static final String FIND_KUNDE_BY_ID_FETCH_WARTUNGSVERTRAEGE =
		                       PREFIX + "findKundenByNachnameFetchWartungsvertraege";
	public static final String FIND_KUNDEN_BY_PLZ = PREFIX + "findKundenByPlz";
	public static final String FIND_KUNDE_BY_USERNAME = PREFIX + "findKundeByUsername";
	public static final String FIND_USERNAME_BY_USERNAME_PREFIX = PREFIX + "findKundeByUsernamePrefix";
	public static final String FIND_KUNDEN_BY_DATE = PREFIX + "findKundenByDate";
	public static final String FIND_PRIVATKUNDEN_FIRMENKUNDEN = PREFIX + "findPrivatkundenFirmenkunden";

	public static final String PARAM_KUNDE_ID = "id";
	public static final String PARAM_KUNDE_ID_PREFIX = "idPrefix";
	public static final String PARAM_KUNDE_NACHNAME = "nachname";
	public static final String PARAM_KUNDE_NACHNAME_PREFIX = "nachnamePrefix";
	public static final String PARAM_KUNDE_ADRESSE_PLZ = "plz";
	public static final String PARAM_KUNDE_USERNAME = "username";
	public static final String PARAM_USERNAME_PREFIX = "usernamePrefix";
	public static final String PARAM_KUNDE_EMAIL = "email";
	public static final String PARAM_KUNDE_SEIT = "seit";

	
	@Id
	@GeneratedValue
	@Column(nullable = false, updatable = false)
	@Min(value = MIN_ID, message = "{kundenverwaltung.kunde.id.min}", groups = IdGroup.class)
	private Long id = KEINE_ID;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;

	@Column(length = NACHNAME_LENGTH_MAX, nullable = false)
	@NotNull(message = "{kundenverwaltung.kunde.nachname.notNull}")
	@Size(min = NACHNAME_LENGTH_MIN,
		  max = NACHNAME_LENGTH_MAX,
		  message = "{kundenverwaltung.kunde.nachname.length}")
	@Pattern(regexp = NACHNAME_PATTERN, message = "{kundenverwaltung.kunde.nachname.pattern}")
	private String nachname = "";

	@Column(length = VORNAME_LENGTH_MAX)
	@Size(max = VORNAME_LENGTH_MAX, message = "{kundenverwaltung.kunde.vorname.length}")
	private String vorname = "";
	
	@Column(nullable = false)
	private short kategorie;
	
	@Column(nullable = false, precision = 5, scale = 4)
	private BigDecimal rabatt;
	
	@Column(nullable = false, precision = 15, scale = 3)
	private BigDecimal umsatz;
	
	@Temporal(DATE)
	@Past(message = "{kundenverwaltung.kunde.seit.past}")
	private Date seit;
	
	@Column(length = EMAIL_LENGTH_MAX, unique = true)
	@Email(message = "{kundenverwaltung.kunde.email}")
	private String email = "";
	
	private boolean newsletter = false;

	@Column(length = PASSWORD_LENGTH_MAX)
	@Size(max = PASSWORD_LENGTH_MAX, message = "{kundenverwaltung.kunde.password.length}")
	private String password;
	
	@Transient
	@JsonIgnore
	private String passwordWdh;

//
//  siehe @ScriptAssert
//	@AssertTrue(/*groups = PasswordGroup.class,*/ message = "{kundenverwaltung.kunde.password.notEqual}")
//	public boolean isPasswortEqual() {
//		if (password == null) {
//			return passwordWdh == null;
//		}
//		return password.equals(passwordWdh);
//	}
	
	@Transient
	@AssertTrue(message = "{kundenverwaltung.kunde.agb}")
	private boolean agbAkzeptiert;

	@OneToOne(mappedBy = "kunde", cascade = { PERSIST, REMOVE })
	@Valid
	@NotNull(message = "{kundenverwaltung.kunde.adresse.notNull}")
	private Adresse adresse;
	
	// Default: fetch = LAZY, keine Kaskadierungen
	// Alternativen: cascade = PERSIST, cascade = { PERSIST, REMOVE } usw.
	@OneToMany
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@JsonIgnore
	private List<Bestellung> bestellungen;
	
	@Transient
	private URI bestellungenUri;
	
	@OneToMany
	@JoinColumn(name = "kunde_fk", nullable = false)
	@OrderColumn(name = "idx", nullable = false)
	@JsonIgnore
	private List<Wartungsvertrag> wartungsvertraege;
	
	@ElementCollection(fetch = EAGER)
	@CollectionTable(name = "kunde_rolle",
	                 joinColumns = @JoinColumn(name = "kunde_fk", nullable = false),
	                 uniqueConstraints =  @UniqueConstraint(columnNames = { "kunde_fk", "rolle_fk" }))
	//@Column(table = "kunde_rolle", name = "rolle_fk", nullable = false)
	@Column(name = "rolle_fk", nullable = false)
	private Set<RolleType> rollen;
	
	@OneToOne(fetch = LAZY, cascade = { PERSIST, REMOVE })
	@JoinColumn(name = "file_fk")
	@JsonIgnore
	private File file;
	
	@Transient
	private URI fileUri;
	
	@Column
	@Size(max = BEMERKUNGEN_LENGTH_MAX)
	@SafeHtml
	private String bemerkungen;
	
	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date aktualisiert;
	
	public AbstractKunde() {
		super();
	}

	public AbstractKunde(String nachname, String vorname, String email, Date seit) {
		super();
		this.nachname = nachname;
		this.vorname = vorname;
		this.email = email;
		this.seit = seit == null ? null : (Date) seit.clone();
	}
	
	@PrePersist
	protected void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	@PostPersist
	protected void postPersist() {
		LOGGER.debugf("Neuer Kunde mit ID=%d", id);
	}

	@PreUpdate
	protected void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostUpdate
	protected void postUpdate() {
		LOGGER.debugf("Kunde mit ID=%d aktualisiert: version=%d", id, version);
	}
	
	@PostLoad
	protected void postLoad() {
		passwordWdh = password;
		agbAkzeptiert = true;
	}
	
	public void setValues(AbstractKunde k) {
		version = k.version;
		nachname = k.nachname;
		vorname = k.vorname;
		kategorie = k.kategorie;
		umsatz = k.umsatz;
		seit = k.seit;
		email = k.email;
		password = k.password;
		passwordWdh = k.password;
		agbAkzeptiert = k.agbAkzeptiert;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	public String getNachname() {
		return nachname;
	}
	public void setNachname(String nachname) {
		this.nachname = nachname;
	}

	public String getVorname() {
		return vorname;
	}
	public void setVorname(String vorname) {
		this.vorname = vorname;
	}
	
	public short getKategorie() {
		return kategorie;
	}

	public void setKategorie(short kategorie) {
		this.kategorie = kategorie;
	}
	
	public BigDecimal getRabatt() {
		return rabatt;
	}
	public void setRabatt(BigDecimal rabatt) {
		this.rabatt = rabatt;
	}

	public BigDecimal getUmsatz() {
		return umsatz;
	}

	public void setUmsatz(BigDecimal umsatz) {
		this.umsatz = umsatz;
	}

	public Date getSeit() {
		return seit == null ? null : (Date) seit.clone();
	}

	public void setSeit(Date seit) {
		this.seit = seit == null ? null : (Date) seit.clone();
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	public void setNewsletter(boolean newsletter) {
		this.newsletter = newsletter;
	}

	public boolean isNewsletter() {
		return newsletter;
	}

	
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordWdh() {
		return passwordWdh;
	}

	public void setPasswordWdh(String passwordWdh) {
		this.passwordWdh = passwordWdh;
	}
	
	public void setAgbAkzeptiert(boolean agbAkzeptiert) {
		this.agbAkzeptiert = agbAkzeptiert;
	}

	public boolean isAgbAkzeptiert() {
		return agbAkzeptiert;
	}

	public Adresse getAdresse() {
		return adresse;
	}
	public void setAdresse(Adresse adresse) {
		this.adresse = adresse;
	}

	public List<Bestellung> getBestellungen() {
		if (bestellungen == null) {
			return null;
		}
		return Collections.unmodifiableList(bestellungen);
	}
	public void setBestellungen(List<Bestellung> bestellungen) {
		if (this.bestellungen == null) {
			this.bestellungen = bestellungen;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.bestellungen.clear();
		if (bestellungen != null) {
			this.bestellungen.addAll(bestellungen);
		}
	}
	
	public AbstractKunde addBestellung(Bestellung bestellung) {
		if (bestellungen == null) {
			bestellungen = new ArrayList<>();
		}
		bestellungen.add(bestellung);
		return this;
	}

	public URI getBestellungenUri() {
		return bestellungenUri;
	}
	public void setBestellungenUri(URI bestellungenUri) {
		this.bestellungenUri = bestellungenUri;
	}

	public List<Wartungsvertrag> getWartungsvertraege() {
		if (wartungsvertraege == null) {
			return null;
		}
		
		return Collections.unmodifiableList(wartungsvertraege);
	}

	public void setWartungsvertraege(List<Wartungsvertrag> wartungsvertraege) {
		if (this.wartungsvertraege == null) {
			this.wartungsvertraege = wartungsvertraege;
			return;
		}
		
		// Wiederverwendung der vorhandenen Collection
		this.wartungsvertraege.clear();
		if (wartungsvertraege != null) {
			this.wartungsvertraege.addAll(wartungsvertraege);
		}
	}
	
	public AbstractKunde addWartungsvertrag(Wartungsvertrag wartungsvertrag) {
		if (wartungsvertraege == null) {
			wartungsvertraege = new ArrayList<>();
		}
		wartungsvertraege.add(wartungsvertrag);
		return this;
	}

	public Set<RolleType> getRollen() {
		return rollen;
	}

	public void setRollen(Set<RolleType> rollen) {
		this.rollen = rollen;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public URI getFileUri() {
		return fileUri;
	}

	public void setFileUri(URI fileUri) {
		this.fileUri = fileUri;
	}

	public String getBemerkungen() {
		return bemerkungen;
	}

	public void setBemerkungen(String bemerkungen) {
		this.bemerkungen = bemerkungen;
	}

	public Date getErzeugt() {
		return erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public void setErzeugt(Date erzeugt) {
		this.erzeugt = erzeugt == null ? null : (Date) erzeugt.clone();
	}

	public Date getAktualisiert() {
		return aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	public void setAktualisiert(Date aktualisiert) {
		this.aktualisiert = aktualisiert == null ? null : (Date) aktualisiert.clone();
	}

	@Override
	public String toString() {
		return "Kunde [id=" + id + ", version=" + version
			   + ", nachname=" + nachname + ", vorname=" + vorname + ", seit=" + seit
			   + ", email=" + email + ", kategorie=" + kategorie + ", rabatt=" + rabatt
			   + ", rollen=" + rollen + ", password=" + password + ", passwordWdh=" + passwordWdh
			   + ", erzeugt=" + erzeugt + ", aktualisiert=" + aktualisiert + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AbstractKunde other = (AbstractKunde) obj;
		if (email == null) {
			if (other.email != null) {
				return false;
			}
		}
		else if (!email.equals(other.email)) {
			return false;
		}
		return true;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		final AbstractKunde neuesObjekt = (AbstractKunde) super.clone();
		neuesObjekt.id = id;
		neuesObjekt.version = version;
		neuesObjekt.nachname = nachname;
		neuesObjekt.vorname = vorname;
		neuesObjekt.kategorie = kategorie;
		neuesObjekt.umsatz = umsatz;
		neuesObjekt.email = email;
		neuesObjekt.newsletter = newsletter;
		neuesObjekt.password = password;
		neuesObjekt.passwordWdh = passwordWdh;
		neuesObjekt.agbAkzeptiert = agbAkzeptiert;
		neuesObjekt.adresse = adresse;
		neuesObjekt.bemerkungen = bemerkungen;
		neuesObjekt.erzeugt = erzeugt;
		neuesObjekt.aktualisiert = aktualisiert;
		return neuesObjekt;
	}
}

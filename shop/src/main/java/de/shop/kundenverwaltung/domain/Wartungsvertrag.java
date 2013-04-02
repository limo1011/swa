package de.shop.kundenverwaltung.domain;

import static de.shop.util.Constants.ERSTE_VERSION;
import static javax.persistence.TemporalType.DATE;
import static javax.persistence.TemporalType.TIMESTAMP;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.Version;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.logging.Logger;


@Entity
@Table(name = "wartungsvertrag")
@NamedQueries({
	@NamedQuery(name  = Wartungsvertrag.FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID,
                query = "SELECT w"
				        + " FROM   Wartungsvertrag w"
			            + " WHERE  w.kunde.id = :" + Wartungsvertrag.PARAM_KUNDE_ID)
})
@IdClass(Wartungsvertrag.WartungsvertragId.class)
public class Wartungsvertrag implements Serializable {
	private static final long serialVersionUID = -5955263122430830600L;
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	
	public static final String PREFIX = "Wartungsvertrag.";
	public static final String FIND_WARTUNGSVERTRAEGE_BY_KUNDE_ID =
		                       PREFIX + "findWartungsvertraegeByKundeId";
	public static final String PARAM_KUNDE_ID = "kundeId";

	@Id
	@Column(nullable = false, updatable = false)
	private long nr;

	@Id
	@Column(nullable = false, updatable = false)
	@Temporal(DATE)
	private Date datum;

	@Version
	@Basic(optional = false)
	private int version = ERSTE_VERSION;
	
	@Column
	private String inhalt;

	@ManyToOne(optional = false)
	@JoinColumn(name = "kunde_fk", nullable = false, insertable = false, updatable = false)
	@JsonIgnore
	private AbstractKunde kunde;
	
	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date erzeugt;

	@Column(nullable = false)
	@Temporal(TIMESTAMP)
	@JsonIgnore
	private Date aktualisiert;
	
	public Wartungsvertrag() {
		super();
	}
	
	public Wartungsvertrag(long nr, Date datum) {
		super();
		this.nr = nr;
		this.datum = datum == null ? null : (Date) datum.clone();
	}

	@PrePersist
	private void prePersist() {
		erzeugt = new Date();
		aktualisiert = new Date();
	}
	
	@PreUpdate
	private void preUpdate() {
		aktualisiert = new Date();
	}
	
	@PostPersist
	private void postPersist() {
		LOGGER.debugf("Neuer Wartungsvertrag mit Nr=%d/Datum=%s", nr, datum);
	}
	
	@PostUpdate
	private void postUpdate() {
		LOGGER.debugf("Wartungsvertrag mit Nr=%d/Datum=%s aktualisiert: version=%d", nr, datum, version);
	}

	public long getNr() {
		return nr;
	}

	public void setNr(long nr) {
		this.nr = nr;
	}

	public Date getDatum() {
		return datum == null ? null : (Date) datum.clone();
	}

	public void setDatum(Date datum) {
		this.datum = datum == null ? null : (Date) datum.clone();
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getInhalt() {
		return inhalt;
	}

	public void setInhalt(String inhalt) {
		this.inhalt = inhalt;
	}

	public AbstractKunde getKunde() {
		return kunde;
	}

	public void setKunde(AbstractKunde kunde) {
		this.kunde = kunde;
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

	/**
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datum == null) ? 0 : datum.hashCode());
		result = prime * result + (int) (nr ^ (nr >>> 32));
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
		final Wartungsvertrag other = (Wartungsvertrag) obj;
		if (datum == null) {
			if (other.datum != null) {
				return false;
			}
		}
		else if (!datum.equals(other.datum)) {
			return false;
		}
		if (nr != other.nr) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "Wartungsvertrag [nr=" + nr + ", datum=" + datum + ", version="
		       + version + ", inhalt=" + inhalt + ']';
	}


	/**
	 */
	@Embeddable
	public static class WartungsvertragId implements Serializable {
		private static final long serialVersionUID = 9155270685609346099L;

		private long nr = -1;
		private Date datum = new Date();

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((datum == null) ? 0 : datum.hashCode());
			result = prime * result + (int) (nr ^ (nr >>> 32));
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
			final WartungsvertragId other = (WartungsvertragId) obj;
			if (nr != other.nr) {
				return false;
			}
			if (datum == null) {
				if (other.datum != null) {
					return false;
				}
			}
			else if (!datum.equals(other.datum)) {
				return false;
			}
			return true;
		}

		public long getNr() {
			return nr;
		}

		public void setNr(long nr) {
			this.nr = nr;
		}

		public Date getDatum() {
			return datum == null ? null : (Date) datum.clone();
		}

		public void setDatum(Date datum) {
			this.datum = datum == null ? null : (Date) datum.clone();
		}
	}
}

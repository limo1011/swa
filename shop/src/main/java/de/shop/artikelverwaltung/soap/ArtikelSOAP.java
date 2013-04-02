//package de.shop.artikelverwaltung.soap;
//
//import javax.annotation.PostConstruct;
//import javax.annotation.PreDestroy;
//import javax.enterprise.context.RequestScoped;
//import javax.inject.Inject;
//import javax.jws.WebParam;
//import javax.jws.WebResult;
//import javax.jws.WebService;
//import javax.jws.soap.SOAPBinding;
//
//import org.jboss.logging.Logger;
//
//import de.shop.artikelverwaltung.domain.Artikel;
//import de.shop.artikelverwaltung.service.ArtikelService;
//
///**
// * http://localhost:8080/shop/ArtikelverwaltungService/Artikelverwaltung?wsdl
// * standalone\data\wsdl\shop.war\ArtikelverwaltungService.wsdl
// */
//@WebService(name = "Artikelverwaltung",
//            targetNamespace = "urn:shop:soap:Artikelverwaltung",
//            serviceName = "ArtikelverwaltungService")
//@SOAPBinding  // default: document/literal (einzige Option fuer Integration mit .NET)
//@RequestScoped
//public class ArtikelSOAP {
//	@Inject
//	private Logger logger;
//	
//	@Inject
//	private ArtikelService as;
//	
//	@PostConstruct
//	private void postConstruct() {
//		logger.debugf("CDI-faehiges Bean %s wurde erzeugt", this);
//	}
//	
//	@PreDestroy
//	private void preDestroy() {
//		logger.debugf("CDI-faehiges Bean %s wird geloescht", this);
//	}
//	
//	@WebResult(name = "version")
//	public String getVersion() {
//		return "1.0";
//	}
//	
//	@WebResult(name = "artikel")
//	public Artikel findArtikelById(@WebParam(name = "id") long id) {
//		final Artikel artikel = as.findArtikelById(id);
//		return artikel;
//	}
//}

package de.shop.kundenverwaltung.rest;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.Response.Status.CONFLICT;

import java.util.Collection;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import de.shop.kundenverwaltung.domain.AbstractKunde;
import de.shop.kundenverwaltung.service.AbstractKundeValidationException;
import de.shop.util.Log;


@Provider
@ApplicationScoped
@Log
public class KundeValidationExceptionMapper implements ExceptionMapper<AbstractKundeValidationException> {
	private static final String NEWLINE = System.getProperty("line.separator");
	
	@Override
	public Response toResponse(AbstractKundeValidationException e) {
		final Collection<ConstraintViolation<AbstractKunde>> violations = e.getViolations();
		final StringBuilder sb = new StringBuilder();
		for (ConstraintViolation<AbstractKunde> v : violations) {
			sb.append(v.getMessage());
			sb.append(NEWLINE);
		}
		
		final String responseStr = sb.toString();
		final Response response = Response.status(CONFLICT)
		                                  .type(TEXT_PLAIN)
		                                  .entity(responseStr)
		                                  .build();
		
		return response;
	}
}

package de.shop.artikelverwaltung.rest;

import static com.jayway.restassured.RestAssured.given;
import static de.shop.util.TestConstants.ACCEPT;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH;
import static de.shop.util.TestConstants.ARTIKEL_ID_PATH_PARAM;
import static java.net.HttpURLConnection.HTTP_OK;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.StringReader;
import java.lang.invoke.MethodHandles;
import java.util.logging.Logger;

import javax.json.JsonObject;
import javax.json.JsonReader;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.jayway.restassured.response.Response;

import de.shop.util.AbstractResourceTest;



@RunWith(Arquillian.class)

public class ArtikelResourceTest extends AbstractResourceTest{
	
	private static final Logger LOGGER = Logger.getLogger(MethodHandles.lookup().lookupClass().getName());
	private static final Long ARTIKEL_ID_VORHANDEN = Long.valueOf(300);
	
	@Test
	public void findArtikelById() {
		LOGGER.finer("BEGINN");
		
		// Given
		final Long artikelId = ARTIKEL_ID_VORHANDEN;
		
		// When
		final Response response = given().header(ACCEPT, APPLICATION_JSON)
				                         .pathParameter(ARTIKEL_ID_PATH_PARAM, artikelId)
                                         .get(ARTIKEL_ID_PATH);

		// Then
		assertThat(response.getStatusCode(), is(HTTP_OK));
		
		try (final JsonReader jsonReader =
				              getJsonReaderFactory().createReader(new StringReader(response.asString()))) {
			final JsonObject jsonObject = jsonReader.readObject();
			assertThat(jsonObject.getJsonNumber("id").longValue(), is(artikelId.longValue()));
		}
		
		LOGGER.finer("ENDE");
	}

}
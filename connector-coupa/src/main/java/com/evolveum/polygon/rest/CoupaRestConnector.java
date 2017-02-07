package com.evolveum.polygon.rest;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;

import com.evolveum.polygon.rest.AbstractRestConnector;

@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = CoupaRestConfiguration.class)
public class CoupaRestConnector extends AbstractRestConnector<CoupaRestConfiguration> implements TestOp, SchemaOp {

	public static final String APPLICATION_XML = "application/xml";
	public static final String ACCEPT = "ACCEPT";
	public static final String TEST_USER_SEARCH = "/users";
	public static final String LOGIN_PARAM = "login";

	
	@Override
	public void test() {
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + TEST_USER_SEARCH);
		uriBuilder.addParameter(LOGIN_PARAM, getConfiguration().getTestUser());
		URI uri;
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		HttpGet request = new HttpGet(uri);
		request.addHeader(ACCEPT, APPLICATION_XML);
		request.addHeader(getConfiguration().getTokenName(), getConfiguration().getTokenValue());
		
		HttpResponse response = execute(request);
		
		//TODO zkontrolovat ze je obsazen user v odpovedi a je 200 OK. jinak vyhodit vyjimku nebo neco aby byl vysledek testu fail
		processResponseErrors(response);
	}

	private void processResponseErrors(HttpResponse response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(CoupaRestConnector.class);
		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		schemaBuilder.defineObjectClass(ocBuilder.build());
		return schemaBuilder.build();
	}

}

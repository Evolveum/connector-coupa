package com.evolveum.polygon.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.xml.sax.InputSource;


@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = CoupaRestConfiguration.class)
public class CoupaRestConnector extends AbstractRestConnector<CoupaRestConfiguration> implements TestOp, SchemaOp {

	public static final String APPLICATION_XML = "application/xml";
	public static final String ACCEPT = "ACCEPT";
	public static final String TEST_USER_SEARCH = "/users";
	public static final String LOGIN_PARAM = "login";
	public static final String LOGIN_TAG_NAME = "login";

	
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
		processResponseErrors((CloseableHttpResponse)response);
		try {
			checkResponseContent(response);
		} catch (UnsupportedOperationException | IOException | XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	private void checkResponseContent(HttpResponse response) throws XPathExpressionException, UnsupportedOperationException, IOException {
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			XPathFactory xpathFactory = XPathFactory.newInstance();
			XPath xpath = xpathFactory.newXPath();
			InputSource source = new InputSource(new StringReader(responseContent));
			String testUserLogin = xpath.evaluate("/users/user/login", source);
			if(testUserLogin == null){
				throw new RuntimeException("login element not found in response xml content");
			}else{
				if(!testUserLogin.equals(getConfiguration().getTestUser())){
					throw new RuntimeException("login in response was " + testUserLogin + " expected " + getConfiguration().getTestUser());
				}
			}
		}else{
			throw new RuntimeException("empty response content");
		}
	}

	@Override
	public Schema schema() {
		SchemaBuilder schemaBuilder = new SchemaBuilder(CoupaRestConnector.class);
		ObjectClassInfoBuilder ocBuilder = new ObjectClassInfoBuilder();
		schemaBuilder.defineObjectClass(ocBuilder.build());
		return schemaBuilder.build();
	}

}

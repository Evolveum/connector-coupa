package com.evolveum.polygon.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.xml.sax.InputSource;


@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = CoupaRestConfiguration.class)
public class CoupaRestConnector extends AbstractRestConnector<CoupaRestConfiguration> implements TestOp, SchemaOp, SearchOp {

	private static final Log LOG = Log.getLog(CoupaRestConnector.class);
	
	public static final String APPLICATION_XML = "application/xml";
	public static final String ACCEPT = "ACCEPT";
	public static final String TEST_USER_SEARCH = "/users";
	public static final String LOGIN_PARAM = "login";
	public static final String LOGIN_TAG_NAME = "login";
	
	public static final String USER_OBJECT_CLASS = "userObjectClass";
	public static final String ROLE_OBJECT_CLASS = "roleObjectClass";

	
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
		schemaBuilder.defineObjectClass(prepareAccountClass().build());
		schemaBuilder.defineObjectClass(prepareRoleClass().build());
		return schemaBuilder.build();
	}
	
	private ObjectClassInfoBuilder prepareAccountClass(){
		ObjectClassInfoBuilder accountClassBuilder = new ObjectClassInfoBuilder();
		accountClassBuilder.setType(USER_OBJECT_CLASS);
		accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", Integer.class).build());
		accountClassBuilder.addAttributeInfo(prepareNameAttributeBuilder("login", String.class).build());
		
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("status", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("active", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("purchasing-user", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("authentication-method", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("sso-identifier", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("email", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("firstname", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("lastname", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("roles", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("default-locale", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("default-address_location-code", String.class));
		
		return accountClassBuilder;
	}
	
	private ObjectClassInfoBuilder prepareRoleClass(){
		ObjectClassInfoBuilder accountClassBuilder = new ObjectClassInfoBuilder();
		accountClassBuilder.setType(ROLE_OBJECT_CLASS);
		accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", Integer.class).build());
		accountClassBuilder.addAttributeInfo(prepareNameAttributeBuilder("name", String.class).build());
		
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("omnipotent", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("system-role", String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build("description", String.class));
		
		return accountClassBuilder;
	}
	
	private AttributeInfoBuilder prepareUidAttributeBuilder(String nativeName, Class type){
		AttributeInfoBuilder uidAib = new AttributeInfoBuilder(Uid.NAME);
		uidAib.setNativeName(nativeName);
		uidAib.setType(type);
		uidAib.setRequired(false); // Must be optional. It is not present for create operations
		uidAib.setCreateable(false);
		uidAib.setUpdateable(false);
		uidAib.setReadable(true);
		return uidAib;
	}
	
	private AttributeInfoBuilder prepareNameAttributeBuilder(String nativeName, Class type){
		AttributeInfoBuilder nameAib = new AttributeInfoBuilder(Name.NAME);
		nameAib.setType(type);
		nameAib.setNativeName(nativeName);
		nameAib.setRequired(true);
		return nameAib;
	}

	@Override
	public FilterTranslator<CoupaFilter> createFilterTranslator(ObjectClass arg0, OperationOptions arg1) {
		return new CoupaFilterTranslator();
	}

	@Override
	public void executeQuery(ObjectClass objectClass, Object query, ResultsHandler handler, OperationOptions options) {
		LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
        	processAccount((CoupaFilter)query, handler, options);
        }
	}

	private void processAccount(CoupaFilter query, ResultsHandler handler, OperationOptions options) {
		if (query != null && query.getById() != null) {
            findAccountById(query.getById(), handler, options);
        }
        else if (query != null && query.getByLogin() != null) {
        	findAccountByLogin(query.getByLogin(), handler, options);
        } else {
            // find required page
            String pageing = processPageOptions(options);
            if (!StringUtil.isEmpty(pageing)) {
            	findPagedAccounts(pageing, handler, options);
            }
            // find all
            else {
                int pageSize = getConfiguration().getPageSize();
                int page = 0;
                while (true) {
                    pageing = processPaging(page, pageSize);
                    boolean finish = findPagedAccounts(pageing, handler, options);
                    if (finish) {
                        break;
                    }
                    page++;
                }
            }
        }
	}

	private void findAccountById(String byId, ResultsHandler handler, OperationOptions options) {
		// TODO Auto-generated method stub
		
	}

}

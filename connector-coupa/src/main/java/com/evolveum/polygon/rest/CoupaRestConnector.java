package com.evolveum.polygon.rest;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
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
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
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

import com.evolveum.polygon.rest.model.CoupaUser;
import com.evolveum.polygon.rest.model.CoupaUserList;


@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = CoupaRestConfiguration.class)
public class CoupaRestConnector extends AbstractRestConnector<CoupaRestConfiguration> implements TestOp, SchemaOp, SearchOp<CoupaFilter> {

	private static final Log LOG = Log.getLog(CoupaRestConnector.class);
	
	public static final String APPLICATION_XML = "application/xml";
	public static final String ACCEPT = "ACCEPT";
	public static final String USER_SEARCH = "/users";
	public static final String LOGIN_PARAM = "login";
	public static final String ID_PARAM = "id";
	public static final String LOGIN_TAG_NAME = "login";
	
	public static final String USER_OBJECT_CLASS = "userObjectClass";
	public static final String ROLE_OBJECT_CLASS = "roleObjectClass";
	
	public static final String USER_ATTR_ACTIVE = "active";
	public static final String USER_ATTR_PURCHASING_USER = "purchasing-user";
	public static final String USER_ATTR_AUTHENTICATION_METHOD = "authentication-method";
	public static final String USER_ATTR_SSO_IDENTIFIER = "sso-identifier";
	public static final String USER_ATTR_EMAIL = "email";
	public static final String USER_ATTR_FIRSTNAME = "firstname";
	public static final String USER_ATTR_LASTNAME = "lastname";
	public static final String USER_ATTR_DEFAULT_LOCALE = "default-locale";
	public static final String USER_ATTR_DEFAULT_ADDRESS_LOCATION_CODE = "default-address_location-code";
	public static final String USER_ATTR_ROLES = "roles";
	public static final String USER_ATTR_ID = "id";
	public static final String USER_ATTR_LOGIN = "login";

	
	@Override
	public void test() {
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + USER_SEARCH);
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
		accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", String.class).build());
		accountClassBuilder.addAttributeInfo(prepareNameAttributeBuilder("login", String.class).build());
		
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_ACTIVE, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_PURCHASING_USER, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_AUTHENTICATION_METHOD, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_SSO_IDENTIFIER, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_EMAIL, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_FIRSTNAME, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_LASTNAME, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_ROLES, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_DEFAULT_LOCALE, String.class));
		accountClassBuilder.addAttributeInfo(
		        AttributeInfoBuilder.build(USER_ATTR_DEFAULT_ADDRESS_LOCATION_CODE, String.class));
		
		return accountClassBuilder;
	}
	
	//TODO stringy do konstant
	private ObjectClassInfoBuilder prepareRoleClass(){
		ObjectClassInfoBuilder accountClassBuilder = new ObjectClassInfoBuilder();
		accountClassBuilder.setType(ROLE_OBJECT_CLASS);
		accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", String.class).build());
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
	public void executeQuery(ObjectClass objectClass, CoupaFilter query, ResultsHandler handler, OperationOptions options) {
		LOG.info("executeQuery on {0}, query: {1}, options: {2}", objectClass, query, options);
        if (objectClass.is(USER_OBJECT_CLASS)) {
        	try {
				processAccount(query, handler, options);
			} catch (UnsupportedOperationException | IOException | JAXBException e) {
				// TODO Auto-generated catch block
				throw new RuntimeException(e);
			}
        }
	}

	//TODO dodelat podle https://github.com/Evolveum/connector-drupal/blob/master/src/main/java/com/evolveum/polygon/connector/drupal/DrupalConnector.java
	private void processAccount(CoupaFilter query, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, IOException, JAXBException {
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
                int pageSize = getConfiguration().getDefaultPageSize();
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

	private String processPaging(int page, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}



	private boolean findPagedAccounts(String pageing, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, JAXBException, IOException {
		// TODO paging
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + USER_SEARCH);
		
		//TODO start of test hack
		uriBuilder.addParameter(LOGIN_PARAM, getConfiguration().getTestUser());
		//TODO end of test hack
		
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
		
		CoupaUserList userList = parseUserResponse(response);
		//TODO kontrolovat zda existuje
		for(CoupaUser coupaUser : userList.getUsers()){
			ConnectorObject connectorObject = convertUserToConnectorObject(coupaUser);
	        handler.handle(connectorObject);
		}
		return true;
	}



	private String processPageOptions(OperationOptions options) {
		// TODO Auto-generated method stub
		return null;
	}



	private void findAccountById(String byId, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		//HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USER + "/" + query.byUid);
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + USER_SEARCH);
		uriBuilder.addParameter(ID_PARAM, byId);
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
		
		CoupaUserList userList = parseUserResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertUserToConnectorObject(userList.getUsers().get(0));
        handler.handle(connectorObject);
	}
	
	//TODO refaktorovat duplicitni kod
	private void findAccountByLogin(String byLogin, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		//HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USER + "/" + query.byUid);
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + USER_SEARCH);
		uriBuilder.addParameter(LOGIN_PARAM, byLogin);
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
		
		CoupaUserList userList = parseUserResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertUserToConnectorObject(userList.getUsers().get(0));
        handler.handle(connectorObject);
	}
	
	CoupaUserList parseUserResponse(HttpResponse response) throws JAXBException, UnsupportedOperationException, IOException{
		CoupaUserList result = null;
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JAXBContext jaxbContext = JAXBContext.newInstance(CoupaUserList.class);  
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			InputSource source = new InputSource(new StringReader(responseContent));
			result = (CoupaUserList)jaxbUnmarshaller.unmarshal(source);
		}
		return result;
	}
	
	private ConnectorObject convertUserToConnectorObject(CoupaUser user) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(user.getId()));
        if (user.getLogin() != null) {
            builder.setName(user.getLogin());
        }
        if(user.getActive() != null){
        	addAttr(builder, USER_ATTR_ID, user.getId());
        }
//        if(user.getActive() != null){
//        	addAttr(builder, USER_ATTR_LOGIN, user.getLogin());
//        }
        if(user.getActive() != null){
        	addAttr(builder, USER_ATTR_ACTIVE, user.getActive());
        }
        if(user.getAuthenticationMethod() != null){
        	addAttr(builder, USER_ATTR_AUTHENTICATION_METHOD, user.getAuthenticationMethod());
        }
        if(user.getDefAddressLocationCode() != null){
        	addAttr(builder, USER_ATTR_DEFAULT_ADDRESS_LOCATION_CODE, user.getDefAddressLocationCode());
        }
        if(user.getDefLocale() != null){
        	addAttr(builder, USER_ATTR_DEFAULT_LOCALE, user.getDefLocale());
        }
        if(user.getEmail() != null){
        	addAttr(builder, USER_ATTR_EMAIL, user.getEmail());
        }
        if(user.getFirstname() != null){
        	addAttr(builder, USER_ATTR_FIRSTNAME, user.getFirstname());
        }
        if(user.getLastname() != null){
        	addAttr(builder, USER_ATTR_LASTNAME, user.getLastname());
        }
        if(user.getPurchasingUser() != null){
        		addAttr(builder, USER_ATTR_PURCHASING_USER, user.getPurchasingUser());
        }
        if(user.getSsoIdentifier() != null){
        		addAttr(builder, USER_ATTR_SSO_IDENTIFIER, user.getSsoIdentifier());
        }


        //TODO status
//        if (user.has(ATTR_STATUS)) {
//            boolean enable = STATUS_ENABLED.equals(user.getString(ATTR_STATUS)) ? true : false;
//            addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);
//        }

        //TODO role
//        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
//            builder.addAttribute(USER_ATTR_ROLES, user.getRoles());
//        }

        ConnectorObject connectorObject = builder.build();
        LOG.ok("convertUserToConnectorObject, user: {0}, \n\tconnectorObject: {1}",
                user.getId(), connectorObject);
        return connectorObject;
    }
	
//	private void getIfExists(CoupaUser object, String attrName, ConnectorObjectBuilder builder) {
//        if (object.has(attrName)) {
//            if (object.get(attrName) != null && !JSONObject.NULL.equals(object.get(attrName))) {
//                addAttr(builder, attrName, object.getString(attrName));
//            }
//        }
//    }

}

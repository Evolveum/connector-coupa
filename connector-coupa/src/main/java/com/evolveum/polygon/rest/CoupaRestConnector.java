package com.evolveum.polygon.rest;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.RetryableException;
import org.identityconnectors.framework.common.objects.Attribute;
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
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateOp;
import org.xml.sax.InputSource;

import com.evolveum.polygon.rest.model.CoupaDefaultAddress;
import com.evolveum.polygon.rest.model.CoupaRole;
import com.evolveum.polygon.rest.model.CoupaRoleList;
import com.evolveum.polygon.rest.model.CoupaUser;
import com.evolveum.polygon.rest.model.CoupaUserList;


@ConnectorClass(displayNameKey = "connector.example.rest.display", configurationClass = CoupaRestConfiguration.class)
public class CoupaRestConnector extends AbstractRestConnector<CoupaRestConfiguration> implements TestOp, SchemaOp, SearchOp<CoupaFilter>, CreateOp, UpdateOp {

	private static final Log LOG = Log.getLog(CoupaRestConnector.class);
	
	public static final String APPLICATION_XML = "application/xml";
	public static final String ACCEPT = "ACCEPT";
	public static final String USER_SEARCH = "/users";
	public static final String ROLE_SEARCH = "/roles";
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
	
	public static final String ROLE_ATTR_NAME = "name";
	public static final String ROLE_ATTR_ID = "id";
	public static final String ROLE_ATTR_DESCRIPTION = "description";
	public static final String ROLE_ATTR_OMNIPOTENT = "omnipotent";
	public static final String ROLE_ATTR_SYSTEM_ROLE = "system-role";
	
	public static final int PAGE_SIZE_LIMIT = 50;
	
	public static final String LOGIN_PARAM = "login";
	public static final String NAME_PARAM = "name";
	public static final String ID_PARAM = "id";
	public static final String LIMIT_PARAM = "limit";
	public static final String OFFSET_PARAM = "offset";
	
	public static final String ERROR_PART_DUPLICATE_LOGIN = "Login has already been taken";
	
	private URIBuilder prepareUserUriBuilder(){
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + USER_SEARCH);
		return uriBuilder;
	}
	
	private URIBuilder prepareRoleUriBuilder(){
		URIBuilder uriBuilder = getURIBuilder();
		String basePath = uriBuilder.getPath();
		uriBuilder.setPath(basePath + ROLE_SEARCH);
		return uriBuilder;
	}
	
	private HttpUriRequest prepareRequest(URIBuilder uriBuilder, Class<? extends HttpUriRequest> requestClass){
		URI uri;
		try {
			uri = uriBuilder.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
		HttpUriRequest request = null;
		try {
			request = requestClass.getConstructor(URI.class).newInstance(uri);
			request.addHeader(ACCEPT, APPLICATION_XML);
			request.addHeader(getConfiguration().getTokenName(), getConfiguration().getTokenValue());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return request;
	}
	
	@Override
	public void test() {
		URIBuilder uriBuilder = prepareUserUriBuilder();
		uriBuilder.addParameter(LOGIN_PARAM, getConfiguration().getTestUser());
		HttpUriRequest request;
		request = prepareRequest(uriBuilder, HttpGet.class);
		
		HttpResponse response = execute(request);
		
		//TODO zkontrolovat ze je v odpovedi a je 200 OK. jinak vyhodit vyjimku nebo neco aby byl vysledek testu fail
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
		//accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", String.class).build());
		//accountClassBuilder.addAttributeInfo(prepareNameAttributeBuilder("login", String.class).build());
		
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
		//accountClassBuilder.addAttributeInfo(prepareUidAttributeBuilder("id", String.class).build());
		//accountClassBuilder.addAttributeInfo(prepareNameAttributeBuilder("name", String.class).build());
		
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
				RetryableException retrEx = RetryableException.wrap("Unknown exception", e);
				throw retrEx;
			}
        }
        if (objectClass.is(ROLE_OBJECT_CLASS)) {
        	try {
				processRole(query, handler, options);
			} catch (UnsupportedOperationException | IOException | JAXBException e) {
				RetryableException retrEx = RetryableException.wrap("Unknown exception", e);
				throw retrEx;
			}
        }
	}

	private void processAccount(CoupaFilter query, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, IOException, JAXBException {
		if (query != null && query.getById() != null) {
            findAccountById(query.getById(), handler, options);
        }
        else if (query != null && query.getByName() != null) {
        	findAccountByLogin(query.getByName(), handler, options);
        } else {
            // find required page
            String offset = fetchOffset(options);
            String limit = fetchLimit(options);
            if (!StringUtil.isEmpty(offset) || !StringUtil.isEmpty(limit)) {
            	findPagedAccounts(offset, limit, handler, options);
            }
            // find all
            else {
                int pageSize = getConfiguration().getDefaultPageSize();
                if(pageSize > PAGE_SIZE_LIMIT){//hard interface page size limit
                	pageSize = PAGE_SIZE_LIMIT;
                }
                int page = 0;
                while (true) {
                	offset = page*pageSize + "";
                    limit = pageSize + "";
                    boolean finish = findPagedAccounts(offset, limit, handler, options);
                    if (finish) {
                        break;
                    }
                    page++;
                }
            }
        }
	}
	
	private void processRole(CoupaFilter query, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, IOException, JAXBException {
		if (query != null && query.getById() != null) {
            findRoleById(query.getById(), handler, options);
        }
        else if (query != null && query.getByName() != null) {
        	findRoleByName(query.getByName(), handler, options);
        } else {
            // find required page
            String offset = fetchOffset(options);
            String limit = fetchLimit(options);
            if (!StringUtil.isEmpty(offset) || !StringUtil.isEmpty(limit)) {
            	findPagedRoles(offset, limit, handler, options);
            }
            // find all
            else {
                int pageSize = getConfiguration().getDefaultPageSize();
                if(pageSize > PAGE_SIZE_LIMIT){//hard interface page size limit
                	pageSize = PAGE_SIZE_LIMIT;
                }
                int page = 0;
                while (true) {
                	offset = page*pageSize + "";
                    limit = pageSize + "";
                    boolean finish = findPagedRoles(offset, limit, handler, options);
                    if (finish) {
                        break;
                    }
                    page++;
                }
            }
        }
	}

	private String fetchLimit(OperationOptions options) {
		if(options == null || options.getPageSize() == null){
			return null;
		}
		int pageSize = options.getPageSize();
		if(pageSize > PAGE_SIZE_LIMIT){//hard interface page size limit
        	pageSize = PAGE_SIZE_LIMIT;
        }
		return pageSize + "";
	}



	private String fetchOffset(OperationOptions options) {
		if(options == null || options.getPagedResultsOffset() == null){
			return null;
		}
		return options.getPagedResultsOffset() + "";
	}


	private boolean findPagedRoles(String offset, String limit, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, JAXBException, IOException {
		// TODO paging
		URIBuilder uriBuilder = prepareRoleUriBuilder();
		uriBuilder.addParameter(LIMIT_PARAM, limit);
		uriBuilder.addParameter(OFFSET_PARAM, offset);
		HttpUriRequest request = prepareRequest(uriBuilder, HttpGet.class);
		
		LOG.info("find paged roles offset {0} limit {1} request {2}", offset, limit, request.getURI());
		HttpResponse response = execute(request);
		
		CoupaRoleList roleList = parseRolesResponse(response);
		if(roleList == null || roleList.getRoles() == null || roleList.getRoles().isEmpty()){
			return true;
		}
		//TODO kontrolovat zda existuje
		for(CoupaRole coupaRole : roleList.getRoles()){
			ConnectorObject connectorObject = convertRoleToConnectorObject(coupaRole);
	        handler.handle(connectorObject);
		}
		dispose();
		init(getConfiguration());
		return false;
	}



	private void findRoleById(String byId, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		//HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USER + "/" + query.byUid);
		URIBuilder uriBuilder = prepareRoleUriBuilder();
		uriBuilder.addParameter(ID_PARAM, byId);
		HttpUriRequest request = prepareRequest(uriBuilder, HttpGet.class);
		
		HttpResponse response = execute(request);
		
		CoupaRoleList roleList = parseRolesResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertRoleToConnectorObject(roleList.getRoles().get(0));
        handler.handle(connectorObject);
	}
	
	private void findRoleByName(String byName, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		URIBuilder uriBuilder = prepareRoleUriBuilder();
		uriBuilder.addParameter(NAME_PARAM, byName);
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
		
		CoupaRoleList roleList = parseRolesResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertRoleToConnectorObject(roleList.getRoles().get(0));
        handler.handle(connectorObject);
	}
	
	

	private boolean findPagedAccounts(String offset, String limit, ResultsHandler handler, OperationOptions options) throws UnsupportedOperationException, JAXBException, IOException {
		// TODO paging
		URIBuilder uriBuilder = prepareUserUriBuilder();
		uriBuilder.addParameter(LIMIT_PARAM, limit);
		uriBuilder.addParameter(OFFSET_PARAM, offset);
		HttpUriRequest request = prepareRequest(uriBuilder, HttpGet.class);
		
		LOG.info("find paged users offset {0} limit {1} request {2}", offset, limit, request.getURI());
		HttpResponse response = execute(request);
		
		CoupaUserList userList = parseUsersResponse(response);
		if(userList == null || userList.getUsers() == null || userList.getUsers().isEmpty()){
			return true;
		}
		//TODO kontrolovat zda existuje
		for(CoupaUser coupaUser : userList.getUsers()){
			ConnectorObject connectorObject = convertUserToConnectorObject(coupaUser);
	        handler.handle(connectorObject);
		}
		dispose();
		init(getConfiguration());
		return false;
	}



	private void findAccountById(String byId, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		//HttpGet request = new HttpGet(getConfiguration().getServiceAddress() + USER + "/" + query.byUid);
		URIBuilder uriBuilder = prepareUserUriBuilder();
		uriBuilder.addParameter(ID_PARAM, byId);
		HttpUriRequest request = prepareRequest(uriBuilder, HttpGet.class);
		
		HttpResponse response = execute(request);
		
		CoupaUserList userList = parseUsersResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertUserToConnectorObject(userList.getUsers().get(0));
        handler.handle(connectorObject);
	}
	
	private void findAccountByLogin(String byLogin, ResultsHandler handler, OperationOptions options) throws IOException, UnsupportedOperationException, JAXBException {
		URIBuilder uriBuilder = prepareUserUriBuilder();
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
		
		CoupaUserList userList = parseUsersResponse(response);
		//TODO kontrolovat zda existuje
        ConnectorObject connectorObject = convertUserToConnectorObject(userList.getUsers().get(0));
        handler.handle(connectorObject);
	}
	

	CoupaUser parseUserResponse(HttpResponse response) throws UnsupportedOperationException, IOException, JAXBException{
		CoupaUser result = null;
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JAXBContext jaxbContext = JAXBContext.newInstance(CoupaUser.class);  
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			InputSource source = new InputSource(new StringReader(responseContent));
			try {
				result = (CoupaUser)jaxbUnmarshaller.unmarshal(source);
			} catch (JAXBException e) {
				handleCoupaError(responseContent, response);
			}
		}
		return result;
	}
	CoupaUserList parseUsersResponse(HttpResponse response) throws JAXBException, UnsupportedOperationException, IOException{
		CoupaUserList result = null;
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JAXBContext jaxbContext = JAXBContext.newInstance(CoupaUserList.class);  
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			InputSource source = new InputSource(new StringReader(responseContent));
			try {
				result = (CoupaUserList)jaxbUnmarshaller.unmarshal(source);
			} catch (JAXBException e) {
				handleCoupaError(responseContent, response);
			}
		}
		return result;
	}
	
	
	CoupaRole parseRoleResponse(HttpResponse response) throws UnsupportedOperationException, IOException, JAXBException{
		CoupaRole result = null;
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JAXBContext jaxbContext = JAXBContext.newInstance(CoupaRole.class);  
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			InputSource source = new InputSource(new StringReader(responseContent));
			try {
				result = (CoupaRole)jaxbUnmarshaller.unmarshal(source);
			} catch (JAXBException e) {
				handleCoupaError(responseContent, response);
			}
		}
		return result;
	}
	CoupaRoleList parseRolesResponse(HttpResponse response) throws JAXBException, UnsupportedOperationException, IOException{
		CoupaRoleList result = null;
		if(response != null && response.getEntity() != null && response.getEntity().getContent() != null){
			String responseContent = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			JAXBContext jaxbContext = JAXBContext.newInstance(CoupaRoleList.class);  
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
			InputSource source = new InputSource(new StringReader(responseContent));
			try {
				result = (CoupaRoleList)jaxbUnmarshaller.unmarshal(source);
			} catch (JAXBException e) {
				handleCoupaError(responseContent, response);
			}
		}
		return result;
	}
	
	
	private void handleCoupaError(String responseContent, HttpResponse response) {
		//TODO not found (zatim neni potreba v coupe asi ani nejde mazat uzivatel)
		String resultMessage = response.getStatusLine().toString() + "\n" + responseContent;
		if(responseContent != null && responseContent.contains(ERROR_PART_DUPLICATE_LOGIN)){
			throw new AlreadyExistsException(resultMessage);
		}
		throw new InvalidAttributeValueException(resultMessage);
	}
	
	private ConnectorObject convertUserToConnectorObject(CoupaUser user) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(user.getId()));
        if (user.getLogin() != null) {
            builder.setName(user.getLogin());
        }
//        if(user.getActive() != null){
//        	addAttr(builder, USER_ATTR_ID, user.getId());
//        }
//        if(user.getActive() != null){
//        	addAttr(builder, USER_ATTR_LOGIN, user.getLogin());
//        }
        if(user.getActive() != null){
        	addAttr(builder, USER_ATTR_ACTIVE, user.getActive());
        }
        if(user.getAuthenticationMethod() != null){
        	addAttr(builder, USER_ATTR_AUTHENTICATION_METHOD, user.getAuthenticationMethod());
        }
        if(user.getDefAddress() != null && user.getDefAddress().getLocationCode() != null){
        	addAttr(builder, USER_ATTR_DEFAULT_ADDRESS_LOCATION_CODE, user.getDefAddress().getLocationCode());
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
	
	private ConnectorObject convertRoleToConnectorObject(CoupaRole role) throws IOException {
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(new Uid(role.getId()));
        if (role.getName() != null) {
            builder.setName(role.getName());
        }
        
        if(role.getDescription() != null){
        	addAttr(builder, ROLE_ATTR_DESCRIPTION, role.getDescription());
        }
        if(role.getOmnipotent() != null){
        	addAttr(builder, ROLE_ATTR_OMNIPOTENT, role.getOmnipotent());
        }
        if(role.getSystemRole() != null){
        	addAttr(builder, ROLE_ATTR_SYSTEM_ROLE, role.getSystemRole());
        }

        ConnectorObject connectorObject = builder.build();
        LOG.ok("convertRoleToConnectorObject, role: {0}, \n\tconnectorObject: {1}",
                role.getId(), connectorObject);
        return connectorObject;
    }

	@Override
	public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions arg2) {
		if (objectClass.is(USER_OBJECT_CLASS)) {    // __ACCOUNT__
			return createUser(null, attributes);
        }else{
        	return null;
        }
	}



	private Uid createUser(Uid uid, Set<Attribute> attributes) {
		LOG.ok("createUser, attributes: {1}", attributes);
        if (attributes == null || attributes.isEmpty()) {
            LOG.ok("request ignored, empty attributes");
            return null;
        }
        CoupaUser newUser = prepareUserFromAttributes(attributes);
        String userXml;
		try {
			userXml = convertUserToXml(newUser);
		} catch (JAXBException e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		}
        URIBuilder uriBuilder = prepareUserUriBuilder();
        HttpPost request = (HttpPost)prepareRequest(uriBuilder, HttpPost.class);
        HttpEntity entity;
		try {
			entity = new ByteArrayEntity(userXml.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
        request.setEntity(entity);
        
        HttpResponse response = execute(request);
		
		CoupaUser user;
		try {
			user = parseUserResponse(response);
		} catch (UnsupportedOperationException | IOException | JAXBException e) {
			RetryableException retrEx = RetryableException.wrap("Unknown exception", e);
			throw retrEx;
		}
		//TODO kontrolovat zda existuje
        String id = user.getId();
        Uid resultUid = new Uid(id);
        return resultUid;
	}

	//TODO vyrefaktorovat duplicitni kod
	@Override
	public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
		LOG.ok("updateUser, attributes: {1}", attributes);
        if (attributes == null || attributes.isEmpty()) {
            LOG.ok("request ignored, empty attributes");
            return null;
        }
        CoupaUser newUser = prepareUserFromAttributes(attributes, uid);
        String userXml;
		try {
			userXml = convertUserToXml(newUser);
		} catch (JAXBException e2) {
			e2.printStackTrace();
			throw new RuntimeException(e2);
		}
        URIBuilder uriBuilder = prepareUserUriBuilder();
        uriBuilder.setPath(uriBuilder.getPath() + "/" + newUser.getId());
        HttpPut request = (HttpPut)prepareRequest(uriBuilder, HttpPut.class);
        HttpEntity entity;
		try {
			entity = new ByteArrayEntity(userXml.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			throw new RuntimeException(e1);
		}
        request.setEntity(entity);
        
        HttpResponse response = execute(request);
		
		CoupaUser user;
		try {
			user = parseUserResponse(response);
		} catch (UnsupportedOperationException | IOException | JAXBException e) {
			RetryableException retrEx = RetryableException.wrap("Unknown exception", e);
			throw retrEx;
		}
		//TODO kontrolovat zda existuje
        String id = user.getId();
        Uid resultUid = new Uid(id);
        return resultUid;
	}

	private CoupaUser prepareUserFromAttributes(Set<Attribute> attributes) {
		CoupaUser prepredUser = new CoupaUser();
		String uidAttr = getStringAttr(attributes, Uid.NAME);
		prepredUser.setId(uidAttr);
		String nameAttr = getStringAttr(attributes, Name.NAME);
		prepredUser.setLogin(nameAttr);
		
		String activeAttr = getStringAttr(attributes, USER_ATTR_ACTIVE);
		prepredUser.setActive(activeAttr);
		String purchUserAttr = getStringAttr(attributes, USER_ATTR_PURCHASING_USER);
		prepredUser.setPurchasingUser(purchUserAttr);
		String authMethodAttr = getStringAttr(attributes, USER_ATTR_AUTHENTICATION_METHOD);
		prepredUser.setAuthenticationMethod(authMethodAttr);
		String ssoIdentAttr = getStringAttr(attributes, USER_ATTR_SSO_IDENTIFIER);
		prepredUser.setSsoIdentifier(ssoIdentAttr);
		String emailAttr = getStringAttr(attributes, USER_ATTR_EMAIL);
		prepredUser.setEmail(emailAttr);
		String firstnameAttr = getStringAttr(attributes, USER_ATTR_FIRSTNAME);
		prepredUser.setFirstname(firstnameAttr);
		String lastnameAttr = getStringAttr(attributes, USER_ATTR_LASTNAME);
		prepredUser.setLastname(lastnameAttr);
		String defLocaleAttr = getStringAttr(attributes, USER_ATTR_DEFAULT_LOCALE);
		prepredUser.setDefLocale(defLocaleAttr);
		String defAddrCodeAttr = getStringAttr(attributes, USER_ATTR_DEFAULT_ADDRESS_LOCATION_CODE);
		if(prepredUser.getDefAddress() == null && defAddrCodeAttr != null && !defAddrCodeAttr.isEmpty()){
			prepredUser.setDefAddress(new CoupaDefaultAddress());
			prepredUser.getDefAddress().setLocationCode(defAddrCodeAttr);
		}
		return prepredUser;
	}
	
	private CoupaUser prepareUserFromAttributes(Set<Attribute> attributes, Uid uid) {
		CoupaUser prepredUser = prepareUserFromAttributes(attributes);
		if(prepredUser.getId() == null && uid != null && uid.getUidValue() != null){
			prepredUser.setId(uid.getUidValue());
		}		
		return prepredUser;
	}
	
	private String convertUserToXml(CoupaUser user) throws JAXBException{
		JAXBContext jaxbContext = JAXBContext.newInstance(CoupaUser.class);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter sw = new StringWriter();
		jaxbMarshaller.marshal(user, sw);
		String result = sw.toString();
		return result;
	}

}

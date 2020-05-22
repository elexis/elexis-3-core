package ch.elexis.core.ui.eenv.login;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.scribejava.apis.KeycloakApi;
import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;

import ch.elexis.core.eenv.KeycloakUser;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;

public class ElexisEnvironmentLoginDialog extends Dialog {
	
	/**
	 * url not real served - the request to this url will be catched by the browser to intercept the
	 * code
	 */
	private final String CALLBACK_URL = "http://localhost:11223/elexis-rcp-callback";
	
	private Logger logger;
	
	private final OAuth20Service oauthService;
	private JwtParser jwtParser;
	
	private IUser user;
	private Browser browser;
	
	// TODO handle invalid claim
	// TODO store jwt?
	// https://github.com/jwtk/jjwt#jws-read-key-resolver
	
	public ElexisEnvironmentLoginDialog(Shell shell, String openidClientSecret, String keycloakUrl,
		String realmPublicKey){
		super(shell);
		
		logger = LoggerFactory.getLogger(getClass());
		
		oauthService = new ServiceBuilder(ElexisEnvironmentLoginContributor.OAUTH2_CLIENT_ID)
			.apiSecret(openidClientSecret).defaultScope("openid").callback(CALLBACK_URL)
			.build(KeycloakApi.instance(keycloakUrl, ElexisEnvironmentLoginContributor.REALM_ID));
		
		KeyFactory kf;
		try {
			kf = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpecX509 =
				new X509EncodedKeySpec(Base64.getDecoder().decode(realmPublicKey));
			RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);
			
			jwtParser = Jwts.parserBuilder().setSigningKey(publicKey).build();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Initialization error", e);
		}
		
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		browser.setUrl(oauthService.getAuthorizationUrl());
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changing(LocationEvent event){
				if (event.location.contains("localhost:11223/elexis-rcp-callback")) {
					// catch the callback, we're not using a local http server for this
					browser.setText("<HTML>Logging in ...</HTML>");
					parseCallback(event.location);
					event.doit = false;
				}
				super.changing(event);
			}
			
		});
		
		return parent;
	}
	
	private void parseCallback(String callbackUrl){
		// e.g. https://localhost:11223/elexis-rcp-callback?session_state=67b0202a-60aa-4dee-895b-0d5e6bf759d2
		// &code=ff725f7a-d852-4c35-b37a-892c9628fecf.67b0202a-60aa-4dee-895b-0d5e6bf759d2.55d54f98-c266-47e9-87fb-ef1a4381295c
		
		callbackUrl = callbackUrl.replace("http://", "");
		callbackUrl = callbackUrl.replace("https://", "");
		callbackUrl = callbackUrl.replace("localhost:11223/elexis-rcp-callback?", "");
		String[] parameters = callbackUrl.split("&");
		for (String parameter : parameters) {
			String[] keyValue = parameter.split("=");
			if (keyValue.length == 2) {
				if ("code".equals(keyValue[0])) {
					String code = keyValue[1];
					handleExchange(code);
					return;
				}
			}
		}
		browser.setText("<HTML><B>Invalid callback url</B> " + callbackUrl + "</HTML>");
		logger.warn("Invalid callback url [{}]", callbackUrl);
	}
	
	private void handleExchange(String code){
		try {
			OpenIdOAuth2AccessToken accessToken =
				(OpenIdOAuth2AccessToken) oauthService.getAccessToken(code);
			Jws<Claims> jwsClaim = jwtParser.parseClaimsJws(accessToken.getAccessToken());
			
			String preferredUsername = jwsClaim.getBody().get("preferred_username", String.class);
			String name = jwsClaim.getBody().get("given_name", String.class);
			String familyName = jwsClaim.getBody().get("family_name", String.class);
			long issueTime = jwsClaim.getBody().getIssuedAt().getTime();
			long expirationTime = jwsClaim.getBody().getExpiration().getTime();
			
			String assignedContactId = jwsClaim.getBody().get("ecid", String.class);
			validateAssignedContactId(assignedContactId);
			
			Set<String> roles = parseRoles(jwsClaim);
			if (!roles.contains("user")) {
				throw new IllegalStateException("(Required) User role not assigned");
			}
			
			user = new KeycloakUser(CoreModelServiceHolder.get(), preferredUsername, name,
				familyName, issueTime, expirationTime, assignedContactId, roles);
			
			okPressed();
			return;
			
		} catch (IOException | InterruptedException | ExecutionException
				| IllegalStateException e) {
			logger.error("Error in handling exchange", e);
		}
		cancelPressed();
	}
	
	@SuppressWarnings("unchecked")
	private Set<String> parseRoles(Jws<Claims> jwsClaim){
		Set<String> roles = new HashSet<String>();
		Map<String, Object> realmAccess =
			(Map<String, Object>) jwsClaim.getBody().get("realm_access");
		if (realmAccess != null) {
			List<String> realmAccessRoles = (List<String>) realmAccess.get("roles");
			if (realmAccessRoles != null) {
				roles.addAll(realmAccessRoles);
			}
		}
		Map<String, Object> resourceAccess =
			(Map<String, Object>) jwsClaim.getBody().get("resource_access");
		if (resourceAccess != null) {
			Map<String, Object> elexisRcpOpenidAccess =
				(Map<String, Object>) resourceAccess.get("elexis-rcp-openid");
			if (elexisRcpOpenidAccess != null) {
				List<String> elexisRcpOpenidAccessRoles =
					(List<String>) elexisRcpOpenidAccess.get("roles");
				if (elexisRcpOpenidAccessRoles != null) {
					roles.addAll(elexisRcpOpenidAccessRoles);
				}
			}
		}
		return roles;
	}
	
	private void validateAssignedContactId(String assignedContactId){
		Optional<IContact> assignedContact =
			CoreModelServiceHolder.get().load(assignedContactId, IContact.class);
		if (!assignedContact.isPresent()) {
			throw new IllegalStateException(
				"Invalid assignedContactId [" + assignedContactId + "]");
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		GridLayout layout = (GridLayout) parent.getLayout();
		layout.marginHeight = 0;
	}
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize(){
		return new Point(640, 680);
	}
	
	public IUser getUser(){
		return user;
	}
}

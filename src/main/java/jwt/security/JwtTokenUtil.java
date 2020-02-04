package jwt.security;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mobile.device.Device;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jwt.security.dto.JwtUser;

@Component
public class JwtTokenUtil implements Serializable {

	private static final long serialVersionUID = -3301605591108950415L;

	static final String CLAIM_KEY_USERNAME = "sub";
	static final String CLAIM_KEY_AUDIENCE = "audience";
	static final String CLAIM_KEY_CREATED = "iat";
	static final String CLAIM_KEY_AUTHORITIES = "roles";
	static final String CLAIM_KEY_IS_ENABLED = "isEnabled";

	private static final String AUDIENCE_UNKNOWN = "unknown";
	private static final String AUDIENCE_WEB = "web";
	private static final String AUDIENCE_MOBILE = "mobile";
	private static final String AUDIENCE_TABLET = "tablet";

	@Value("${jwt.secret}")
	private String secret;

	@Autowired
	ObjectMapper objectMapper;

	@Value("${jwt.expiration}")
	private Long expiration;

	/**
	 * Metodo che estrae lo username da un token jwt
	 * 
	 * @param token jwt
	 * @return username
	 */
	public String getUsernameFromToken(String token) {
		String username;
		try {
			final Claims claims = getClaimsFromToken(token);
			username = claims.getSubject();
		} catch (Exception e) {
			username = null;
		}
		return username;
	}

	/**
	 * Metodo che crea un JwtUser a partire da un token jwt
	 * 
	 * @param token jwt
	 * @return oggetto JwtUser
	 */
	public JwtUser getUserDetails(String token) {

		if (token == null) {
			return null;
		}
		try {
			final Claims claims = getClaimsFromToken(token);
			List<SimpleGrantedAuthority> authorities = null;
			if (claims.get(CLAIM_KEY_AUTHORITIES) != null) {
				authorities = ((List<String>) claims.get(CLAIM_KEY_AUTHORITIES)).stream()
						.map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());
			}

			return new JwtUser(claims.getSubject(), "", authorities, (boolean) claims.get(CLAIM_KEY_IS_ENABLED));
		} catch (Exception e) {
			return null;
		}

	}

	/**
	 * Metodo che estrae la data di creazione da un token jwt
	 * @param token jwt
	 * @return data di creazione
	 */
	public Date getCreatedDateFromToken(String token) {
		Date created;
		try {
			final Claims claims = getClaimsFromToken(token);
			created = new Date((Long) claims.get(CLAIM_KEY_CREATED));
		} catch (Exception e) {
			created = null;
		}
		return created;
	}
	
	/**
	 * Metodo che estra la data di scadenza da un token jwt
	 * @param token jwt
	 * @return data di scadenza
	 */
	public Date getExpirationDateFromToken(String token) {
		Date expiration;
		try {
			final Claims claims = getClaimsFromToken(token);
			expiration = claims.getExpiration();
		} catch (Exception e) {
			expiration = null;
		}
		return expiration;
	}
	
	/**
	 * Metodo che estrae audience da token jwt
	 * @param token jwt
	 * @return stringa che rappresenta l'audience
	 */
	public String getAudienceFromToken(String token) {
		String audience;
		try {
			final Claims claims = getClaimsFromToken(token);
			audience = (String) claims.get(CLAIM_KEY_AUDIENCE);
		} catch (Exception e) {
			audience = null;
		}
		return audience;
	}
	
	/**
	 * Metodo che estrae le claims da un token jwt
	 * @param token jwt
	 * @return claims
	 */
	private Claims getClaimsFromToken(String token) {
		Claims claims;
		try {
			claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
		} catch (Exception e) {
			claims = null;
		}
		return claims;
	}
	
	/**
	 * Metodo che restituisce una data di scadenza tenendo conto del parametro dell'applicazione
	 * @return data di scadenza
	 */
	private Date generateExpirationDate() {
		return new Date(System.currentTimeMillis() + expiration * 1000);
	}
	
	/**
	 * Metodo che controlla se un token jwt è scaduto
	 * @param token jwt
	 * @return true se è scaduto
	 */
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}
	
	/**
	 * Metodo che crea un audience a partire dal dispositivo che ha fatto la richiesta
	 * @param device 
	 * @return audience corrispondente
	 */
	private String generateAudience(Device device) {
		String audience = AUDIENCE_UNKNOWN;
		if (device.isNormal()) {
			audience = AUDIENCE_WEB;
		} else if (device.isTablet()) {
			audience = AUDIENCE_TABLET;
		} else if (device.isMobile()) {
			audience = AUDIENCE_MOBILE;
		}
		return audience;
	}
	

	private Boolean ignoreTokenExpiration(String token) {
		String audience = getAudienceFromToken(token);
		return (AUDIENCE_TABLET.equals(audience) || AUDIENCE_MOBILE.equals(audience));
	}
	
	/**
	 * Metodo per lanciare la creazione di un token jwt
	 * @param userDetails contiene i dati dell'utente
	 * @param device dispositivo che ha effettuato la richiesta
	 * @return token jwt creato
	 * @throws JsonProcessingException
	 */
	public String generateToken(UserDetails userDetails, Device device) throws JsonProcessingException {
		Map<String, Object> claims = new HashMap<>();
		claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
		claims.put(CLAIM_KEY_AUDIENCE, generateAudience(device));
		claims.put(CLAIM_KEY_CREATED, new Date());
		List<String> auth = userDetails.getAuthorities().stream().map(role -> role.getAuthority())
				.collect(Collectors.toList());
		claims.put(CLAIM_KEY_AUTHORITIES, auth);
		claims.put(CLAIM_KEY_IS_ENABLED, userDetails.isEnabled());

		return generateToken(claims);
	}
	
	/**
	 * Metodo che propriamente genera il token jwt
	 * @param claims del token da creare
	 * @return token jwt creato
	 */
	String generateToken(Map<String, Object> claims) {
		ObjectMapper mapper = new ObjectMapper();

		return Jwts.builder().setClaims(claims).setExpiration(generateExpirationDate())
				.signWith(SignatureAlgorithm.HS256, secret).compact();
	}
	
	/**
	 * Metodo che controlla se un token può essere refreshato
	 * @param token jwt
	 * @return true se può essere refreshato
	 */
	public Boolean canTokenBeRefreshed(String token) {
		final Date created = getCreatedDateFromToken(token);
		return (!isTokenExpired(token) || ignoreTokenExpiration(token));
	}
	
	/**
	 * Metodo per refreshare un token in scadenza
	 * @param token jwt vecchio
	 * @return nuovo token jwt
	 */
	public String refreshToken(String token) {
		String refreshedToken;
		try {
			final Claims claims = getClaimsFromToken(token);
			claims.put(CLAIM_KEY_CREATED, new Date());
			refreshedToken = generateToken(claims);
		} catch (Exception e) {
			refreshedToken = null;
		}
		return refreshedToken;
	}
	
	/**
	 * Metodo che controlla la validità di un token
	 * @param token jwt
	 * @param userDetails racchiude i dati dell'utente intestatario del token
	 * @return true se il token è valido
	 */
	public Boolean validateToken(String token, UserDetails userDetails) {
		JwtUser user = (JwtUser) userDetails;
		final String username = getUsernameFromToken(token);
		return (username.equals(user.getUsername()) && !isTokenExpired(token));
	}
}
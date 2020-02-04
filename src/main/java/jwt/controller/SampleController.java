package jwt.controller;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jwt.dao.UserRepository;
import jwt.model.Authority;
import jwt.model.AuthorityName;
import jwt.model.User;
import jwt.service.AuthorityService;
import jwt.service.UserService;

@RestController
public class SampleController {
	
	@Autowired
	UserService userService;
	
	@Autowired
	AuthorityService authService;

    /**
     * Esempio di endpoint pubblico senza restrizioni di accesso
     * @return
     */
    @RequestMapping(value = "public/hello", method = RequestMethod.GET)
    public @ResponseBody String publicHelloWorld(){
        return "Ciao Mondo-Pubblico";
    }
    
    /**
     * Esempio di endpoint a cui si può accedere solo tramite token valido
     * @return
     */
	@RequestMapping(value = "protected/hello", method = RequestMethod.GET)
	public @ResponseBody String protectedHelloWorld(){
	    return "Ciao Mondo-Protetto";
	}
    
	/**
	 * Endpoint per registrare un nuovo utente
	 * @param reqString oggetto JSON con parametri username, password e lista di authorities
	 * @return true se l'operazione è andata a buon fine
	 */
    @RequestMapping(value = "public/register", method = RequestMethod.POST)
    public @ResponseBody boolean addUser(@RequestBody String reqString){
    	
        try {
        	JSONObject req = new JSONObject(reqString);
			String username = req.getString("username");
			String password = req.getString("password");
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			password = passwordEncoder.encode(password);
			JSONArray authoritiesJson = req.getJSONArray("authorities");
			List<Authority> authorities = new ArrayList<>();
			
			for(int i = 0; i<authoritiesJson.length(); i++) {
				String authorityString = authoritiesJson.getString(i);
				
				try {
					Authority auth = this.authService.getFromName(authorityString);						
					authorities.add(auth);
				} catch (NullPointerException e) {
					continue;
				}
			}
			
			boolean result = this.userService.register(new User(username,password,true,authorities));
			
			return result;
			
		} catch (JSONException e) {
			return false;
		}

    }

}

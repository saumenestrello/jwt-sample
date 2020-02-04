package jwt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jwt.dao.AuthorityRepository;
import jwt.model.Authority;
import jwt.model.AuthorityName;

@Service
public class AuthorityService {
	
	@Autowired
	AuthorityRepository repo;
	
	public Authority getFromName(String name) {
		
		AuthorityName authName = parseStringName(name);
		if(authName == null) throw new NullPointerException();
		Authority result = this.repo.findByName(authName);
		
		return result;		
	}
	
	public AuthorityName parseStringName(String name) {
		switch(name) {
		case "admin":
			return AuthorityName.ROLE_ADMIN;
		case "user":
			return AuthorityName.ROLE_USER;
		default:
			return null;
		}
	}

}

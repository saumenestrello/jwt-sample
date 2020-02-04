package jwt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jwt.dao.UserRepository;
import jwt.model.User;

@Service
public class UserService {
	
	@Autowired
    private UserRepository userRepository;
	
	
	public boolean register(User user) {
		
		User saved = this.userRepository.save(user);
		
		if(saved != null) {
			return true;
		} else {
			return false;
		}
	}
	
}

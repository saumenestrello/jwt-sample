package jwt.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import jwt.model.User;


public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

}
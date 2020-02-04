package jwt.dao;


import org.springframework.data.jpa.repository.JpaRepository;

import jwt.model.Authority;
import jwt.model.AuthorityName;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    Authority findByName(AuthorityName name);

}
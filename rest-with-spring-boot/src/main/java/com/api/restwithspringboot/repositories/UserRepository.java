package com.api.restwithspringboot.repositories;

import com.api.restwithspringboot.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * No @QUery não passamos uma query padrão SQL, mas sim uma
     * JpaQuery, um formato que o JPA vai entender
     */
    @Query("SELECT u from User u where u.userName =:userName")
    User findByUsername(@Param("userName") String userName);
}

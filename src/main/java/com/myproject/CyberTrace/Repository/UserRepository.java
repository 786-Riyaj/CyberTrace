package com.myproject.CyberTrace.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.myproject.CyberTrace.Model.Users;
import com.myproject.CyberTrace.Model.Users.UserRole;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

	boolean existsByEmail(String email);

	Users findByEmail(String email);

	List<Users> findAllByRole(UserRole investigator);

}

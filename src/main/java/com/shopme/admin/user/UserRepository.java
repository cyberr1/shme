package com.shopme.admin.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.shopme.common.entity.User;

@Repository
public interface UserRepository extends PagingAndSortingRepository<User, Integer> {
	
	@Query("SELECT u FROM User u WHERE u.email=:email")
	public User getUserByEmail(@Param("email") String email);
	
	public Long countById(Integer id);
	
	@Query("Update User u set u.enabled =?2 where u.id=?1")
	@Modifying
	public void updateEnabledStatus(Integer id, boolean enabled);
	
	
	@Query("SELECT u from User u where CONCAT(u.id, u.email, u.firstName,' ', u.lastName) LIKE %?1% or "+
			"CONCAT(u.id, u.email, u.lastName, ' ',u.firstName) LIKE %?1%")
	public Page<User> findAll(String keyword, Pageable pageable);

	@Query("UPDATE User u SET u.failedAttempt = ?1 WHERE u.email = ?2")
    @Modifying
    public void updateFailedAttempts(int failAttempts, String email);

	//@Query("SELECT u FROM User u WHERE u.resetPasswordToken=:token")
	public User findByResetPasswordToken( String token );
}

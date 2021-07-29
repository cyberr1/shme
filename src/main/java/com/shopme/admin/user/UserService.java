package com.shopme.admin.user;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopme.common.entity.Role;
import com.shopme.common.entity.User;

@Service
@Transactional // for update query
public class UserService {

	public static final int MAX_FAILED_ATTEMPTS = 3;
	private static final long LOCK_TIME_DURATION = 24 * 60 * 60 * 1000; // 24 hours
	
	public static final int USERS_PER_PAGE = 4;
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	
	public User getByEmail(String email) {
		return userRepo.getUserByEmail(email);
	}
	
	
	public List<User> listAll() {
		return (List<User>) userRepo.findAll(Sort.by("id").ascending());
	}

	public Page<User> listByPage(int pageNum, String sortField, String sortDir, String keyword) {
		Sort sort = Sort.by(sortField);
		sort = sortDir.equals("asc") ? sort.ascending() : sort.descending();
		
		Pageable pageable= PageRequest.of(pageNum-1, USERS_PER_PAGE, sort);
		
		if(keyword != null) {
			return userRepo.findAll(keyword, pageable);
		}
		
		return userRepo.findAll(pageable);
		
	}

	public List<Role> listRoles() {
		return (List<Role>) roleRepo.findAll();
	}

	public User save(User user) {
		boolean isUpdatingUser = (user.getId() != null);
		if (isUpdatingUser) {
			User existingUser = userRepo.findById(user.getId()).get();
			if (user.getPassword().isEmpty()) {
				user.setPassword(existingUser.getPassword());
			} else {
				encodePassword(user);
			}
		} else {
			encodePassword(user);
		}
		return userRepo.save(user);
	}
	
	public User UpdatePassword(User userInForm) {
		User userInDB = userRepo.findById(userInForm.getId()).get();
		userInDB.setPassword(passwordEncoder.encode(userInDB.getPassword()));
		userInDB.setAccountNonLocked(true);
		userInDB.setFailedAttempt(0);
		userInDB.setResetPasswordToken(null);
		userInDB.setLockTime(null);
		System.out.println(userInDB);
		return userRepo.save(userInDB);

	}
	public User updateAccount(User userInForm) {
		User userInDB = userRepo.findById(userInForm.getId()).get();
		if(!userInForm.getPassword().isEmpty()) {
			userInDB.setPassword(userInForm.getPassword());
			encodePassword(userInDB);
		}
		if(userInForm.getPhotos() != null ) {
			userInDB.setPhotos(userInForm.getPhotos());
		}
		userInDB.setFirstName(userInForm.getFirstName());
		userInDB.setLastName(userInForm.getLastName());
		userInDB.setResetPasswordToken(userInForm.getResetPasswordToken());
		
		return userRepo.save(userInDB);
	}

	private void encodePassword(User user) {
		String encoded = passwordEncoder.encode(user.getPassword());
		user.setPassword(encoded);
	}

	public boolean isEmailUnique(Integer id, String email) {
		email = email.trim();
		User userByEmail = userRepo.getUserByEmail(email);

		if (userByEmail == null)
			return true;

		boolean isCreatingNew = (id == null);
		
		if (isCreatingNew) {
			if (userByEmail != null) return false;
		} else {
			if (userByEmail.getId() != id) return false;
		}
		return true;
	}

	public User get(Integer id) throws UserNotFoundException {
		try {
			return userRepo.findById(id).get();
		} catch (NoSuchElementException ex) {
			throw new UserNotFoundException("Could not find user with the id: " + id);
		}
	}

	public void delete(Integer id) throws UserNotFoundException {
		Long count = userRepo.countById(id);
		if (count == null || count == 0) {
			throw new UserNotFoundException("Could not find user with the id: " + id);
		}
		userRepo.deleteById(id);
	}

	public void updateEnableStatus(Integer id, boolean status) {
		userRepo.updateEnabledStatus(id, status);
	}
	
	//////////////
	public void increaseFailedAttempts(User user) {
        int newFailAttempts = user.getFailedAttempt() + 1;
        userRepo.updateFailedAttempts(newFailAttempts, user.getEmail());
    }
     
    public void resetFailedAttempts(String email) {
    	userRepo.updateFailedAttempts(0, email);
    }
     
    public void lock(User user) {
        user.setAccountNonLocked(false);
        user.setLockTime(new Date());
         
        userRepo.save(user);
    }
     
    public boolean unlockWhenTimeExpired(User user) {
        long lockTimeInMillis = user.getLockTime().getTime();
        long currentTimeInMillis = System.currentTimeMillis();
         
        if (lockTimeInMillis + LOCK_TIME_DURATION < currentTimeInMillis) {
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            user.setFailedAttempt(0);
             
            userRepo.save(user);
             
            return true;
        }
         
        return false;
    }


	public User findByToken(String token) {
		// TODO Auto-generated method stub
		return userRepo.findByResetPasswordToken(token);
	}
}

package com.np.hrms.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.np.hrms.dto.UserDTO;
import com.np.hrms.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

	Optional<User> findById(String id);

	User findByUserId(String user_id);

	Optional<User> findByEmailId(String email_id);

	Optional<User> findByContactNo(Long contact_no);

	@Query("SELECT new com.np.hrms.dto.UserDTO(u.userId, u.name) FROM User u")
	List<UserDTO> findAllIdAndNameForRManager();

	@Query(value = "SELECT CAST(AES_DECRYPT(u.password, 'bitnami@123') AS CHAR) AS decrypted_password "
			+ "FROM user u WHERE u.user_id = :userId", nativeQuery = true)
	String findDecryptedPasswordById(@Param("userId") String userId);

	@Query("SELECT u FROM User u WHERE u.userId = :userId")
	User findUserById(@Param("userId") String userId);

	@Query("SELECT u.name FROM User u WHERE u.userId = :approverId")
	String getApproverNameById(String approverId);
}

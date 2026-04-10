package com.hospital.handover.repository;

import com.hospital.handover.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByHisStaffId(Long hisStaffId);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.usercode) = LOWER(:usercode)")
    User findByUsercodeIgnoreCase(String usercode);
    
    @Query("SELECT u FROM User u WHERE u.hisStaffId IS NOT NULL")
    List<User> findAllWithHisStaffId();
}
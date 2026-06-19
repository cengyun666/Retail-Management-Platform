package com.example.userauth.repository;

import com.example.userauth.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    // 根据用户ID获取所有未删除的地址
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDeleted = 0")
    List<Address> findByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    // 根据用户ID获取未删除的默认地址
    @Query("SELECT a FROM Address a WHERE a.userId = :userId AND a.isDefault = 1 AND a.isDeleted = 0")
    Optional<Address> findByUserIdAndIsDefaultTrueAndIsDeletedFalse(@Param("userId") Long userId);

    // 根据用户ID删除地址
    void deleteByIdAndUserId(Long id, Long userId);

    // 统计用户的未删除地址数量
    @Query("SELECT COUNT(a) FROM Address a WHERE a.userId = :userId AND a.isDeleted = 0")
    long countByUserIdAndIsDeletedFalse(@Param("userId") Long userId);

    // 逻辑删除地址
    @Modifying
    @Query("UPDATE Address a SET a.isDeleted = 1 WHERE a.id = :id AND a.userId = :userId")
    void logicalDeleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    // 清除用户的默认地址
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = 0 WHERE a.userId = :userId AND a.isDefault = 1 AND a.id != :excludeId")
    void clearDefaultAddress(@Param("userId") Long userId, @Param("excludeId") Long excludeId);
}
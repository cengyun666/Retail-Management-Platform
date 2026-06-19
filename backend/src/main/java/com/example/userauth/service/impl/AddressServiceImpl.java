package com.example.userauth.service.impl;

import com.example.userauth.entity.Address;
import com.example.userauth.repository.AddressRepository;
import com.example.userauth.service.AddressService;
import com.example.userauth.dto.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    private static final int MAX_ADDRESS_COUNT = 10; // 用户最多只能添加10个地址

    @Override
    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public Address getDefaultAddress(Long userId) {
        Optional<Address> optionalAddress = addressRepository.findByUserIdAndIsDefaultTrueAndIsDeletedFalse(userId);
        return optionalAddress.orElse(null);
    }

    @Override
    @Transactional
    public Address addAddress(Address address, UserInfo currentUser) {
        // 检查用户地址数量是否超过限制
        long count = addressRepository.countByUserIdAndIsDeletedFalse(currentUser.getId());
        if (count >= MAX_ADDRESS_COUNT) {
            throw new RuntimeException("地址数量已达上限，最多只能添加" + MAX_ADDRESS_COUNT + "个地址");
        }

        // 设置地址所属用户
        address.setUserId(currentUser.getId());

        // 如果是默认地址，清除其他默认地址
        if (address.getIsDefault()) {
            addressRepository.clearDefaultAddress(currentUser.getId(), -1L);
        } else if (count == 0) {
            // 如果是第一个地址，默认设置为默认地址
            address.setIsDefault(true);
        }

        return addressRepository.save(address);
    }

    @Override
    @Transactional
    public Address updateAddress(Long id, Address address, UserInfo currentUser) {
        // 查找地址并验证权限
        Address existingAddress = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));

        if (!existingAddress.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("没有权限修改该地址");
        }

        // 更新地址信息
        existingAddress.setName(address.getName());
        existingAddress.setPhone(address.getPhone());
        existingAddress.setProvince(address.getProvince());
        existingAddress.setCity(address.getCity());
        existingAddress.setDistrict(address.getDistrict());
        existingAddress.setDetail(address.getDetail());

        // 如果设置为默认地址，清除其他默认地址
        if (address.getIsDefault()) {
            addressRepository.clearDefaultAddress(currentUser.getId(), id);
            existingAddress.setIsDefault(true);
        }

        return addressRepository.save(existingAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long id, UserInfo currentUser) {
        // 查找地址并验证权限
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));

        if (!address.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("没有权限删除该地址");
        }

        if (address.getIsDeleted() == 1) {
            throw new RuntimeException("地址已删除");
        }

        // 逻辑删除地址
        addressRepository.logicalDeleteByIdAndUserId(id, currentUser.getId());

        // 如果删除的是默认地址，并且还有其他未删除地址，设置第一个未删除地址为默认地址
        List<Address> remainingAddresses = addressRepository.findByUserIdAndIsDeletedFalse(currentUser.getId());
        if (address.getIsDefault() && !remainingAddresses.isEmpty()) {
            Address firstAddress = remainingAddresses.get(0);
            firstAddress.setIsDefault(true);
            addressRepository.save(firstAddress);
        }
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, UserInfo currentUser) {
        // 查找地址并验证权限
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));

        if (!address.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("没有权限设置该地址");
        }

        // 清除其他默认地址
        addressRepository.clearDefaultAddress(currentUser.getId(), id);

        // 设置当前地址为默认地址
        address.setIsDefault(true);
        addressRepository.save(address);
    }

    @Override
    public Address getAddressById(Long id, UserInfo currentUser) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("地址不存在"));

        if (!address.getUserId().equals(currentUser.getId())) {
            throw new RuntimeException("没有权限查看该地址");
        }

        if (address.getIsDeleted() == 1) {
            throw new RuntimeException("地址已删除");
        }

        return address;
    }
}
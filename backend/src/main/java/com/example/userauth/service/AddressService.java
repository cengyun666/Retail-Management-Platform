package com.example.userauth.service;

import com.example.userauth.entity.Address;
import com.example.userauth.dto.UserInfo;

import java.util.List;

public interface AddressService {
    // 获取用户的所有地址
    List<Address> getUserAddresses(Long userId);

    // 获取用户的默认地址
    Address getDefaultAddress(Long userId);

    // 添加新地址
    Address addAddress(Address address, UserInfo currentUser);

    // 更新地址
    Address updateAddress(Long id, Address address, UserInfo currentUser);

    // 删除地址
    void deleteAddress(Long id, UserInfo currentUser);

    // 设置默认地址
    void setDefaultAddress(Long id, UserInfo currentUser);

    // 获取地址详情
    Address getAddressById(Long id, UserInfo currentUser);
}
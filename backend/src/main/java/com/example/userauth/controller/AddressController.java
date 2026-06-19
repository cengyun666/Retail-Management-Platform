package com.example.userauth.controller;

import com.example.userauth.entity.Address;
import com.example.userauth.service.AddressService;
import com.example.userauth.dto.ApiResponse;
import com.example.userauth.dto.UserInfo;
import com.example.userauth.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@Api(tags = "地址管理")
public class AddressController {

    @Autowired
    private AddressService addressService;
    
    @Autowired
    private UserService userService;

    @GetMapping
    @ApiOperation("获取用户的所有地址")
    public ResponseEntity<ApiResponse<List<Address>>> getUserAddresses(@RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        List<Address> addresses = addressService.getUserAddresses(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("获取地址列表成功", addresses));
    }

    @GetMapping("/default")
    @ApiOperation("获取用户的默认地址")
    public ResponseEntity<ApiResponse<Address>> getDefaultAddress(@RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Address defaultAddress = addressService.getDefaultAddress(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("获取默认地址成功", defaultAddress));
    }

    @PostMapping
    @ApiOperation("添加新地址")
    public ResponseEntity<ApiResponse<Address>> addAddress(@RequestBody Address address, @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Address newAddress = addressService.addAddress(address, currentUser);
        return ResponseEntity.ok(ApiResponse.success("添加地址成功", newAddress));
    }

    @PutMapping("/{id}")
    @ApiOperation("更新地址")
    public ResponseEntity<ApiResponse<Address>> updateAddress(@PathVariable Long id, @RequestBody Address address, @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Address updatedAddress = addressService.updateAddress(id, address, currentUser);
        return ResponseEntity.ok(ApiResponse.success("更新地址成功", updatedAddress));
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除地址")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        addressService.deleteAddress(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("删除地址成功"));
    }

    @PutMapping("/{id}/default")
    @ApiOperation("设置默认地址")
    public ResponseEntity<ApiResponse<Void>> setDefaultAddress(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        addressService.setDefaultAddress(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("设置默认地址成功"));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取地址详情")
    public ResponseEntity<ApiResponse<Address>> getAddressById(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String token) {
        // 移除Bearer前缀
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        UserInfo currentUser = userService.getUserInfoByToken(token);
        Address address = addressService.getAddressById(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("获取地址详情成功", address));
    }
}
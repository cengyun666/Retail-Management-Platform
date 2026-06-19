package com.example.userauth;

import com.example.userauth.entity.Address;
import com.example.userauth.entity.User;
import com.example.userauth.repository.AddressRepository;
import com.example.userauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AddressDebugger {

    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void debugAddresses() {
        // 为user001添加测试地址
        String username = "user001";
        User user = userRepository.findByUsernameAndIsDeletedFalse(username);
        
        System.out.println("=== Address Debugger ===");
        
        if (user != null) {
            System.out.println("找到用户: " + username + " (ID: " + user.getId() + ")");
            long userId = user.getId();
            
            // 查看现有地址
            var existingAddresses = addressRepository.findByUserIdAndIsDeletedFalse(userId);
            System.out.println("现有地址数量: " + existingAddresses.size());
            
            if (existingAddresses.isEmpty()) {
                // 添加测试地址1
                Address address1 = new Address();
                address1.setUserId(userId);
                address1.setName("张三");
                address1.setPhone("13800138000");
                address1.setProvince("广东省");
                address1.setCity("深圳市");
                address1.setDistrict("南山区");
                address1.setDetail("科技园路1号");
                address1.setIsDefault(true);
                address1.setIsDeleted(0);
                
                // 添加测试地址2
                Address address2 = new Address();
                address2.setUserId(userId);
                address2.setName("李四");
                address2.setPhone("13900139000");
                address2.setProvince("北京市");
                address2.setCity("北京市");
                address2.setDistrict("海淀区");
                address2.setDetail("中关村大街1号");
                address2.setIsDefault(false);
                address2.setIsDeleted(0);
                
                addressRepository.save(address1);
                addressRepository.save(address2);
                
                System.out.println("已添加2条测试地址数据");
                
                // 再次查询确认
                var newAddresses = addressRepository.findByUserIdAndIsDeletedFalse(userId);
                System.out.println("添加后地址数量: " + newAddresses.size());
            } else {
                System.out.println("测试地址数据已存在，跳过添加");
                for (var address : existingAddresses) {
                    System.out.println("地址: " + address.getName() + " - " + address.getDetail() + " (ID: " + address.getId() + ")");
                }
            }
        } else {
            System.out.println("用户" + username + "不存在");
            // 查看所有用户
            var allUsers = userRepository.findByIsDeletedFalse();
            System.out.println("所有用户数量: " + allUsers.size());
            for (var u : allUsers) {
                System.out.println("用户: " + u.getUsername() + " (ID: " + u.getId() + ")");
            }
        }
        
        System.out.println("=== Address Debugger End ===");
    }
}
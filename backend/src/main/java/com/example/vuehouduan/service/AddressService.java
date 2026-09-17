package com.example.vuehouduan.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.vuehouduan.entity.Address;
import java.util.List;

public interface AddressService extends IService<Address> {
    
    List<Address> getAddressList(Long userId);
    
    Address addAddress(Long userId, Address address);
    
    Address updateAddress(Long addressId, Address address, Long userId);
    
    void deleteAddress(Long addressId, Long userId);
    
    void setDefaultAddress(Long addressId, Long userId);
}

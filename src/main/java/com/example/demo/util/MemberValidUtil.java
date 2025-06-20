package com.example.demo.util;

import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.customer.Customer;
import com.example.demo.entity.entityInterface.AppUser;
import com.example.demo.entity.store.Store;
import com.example.demo.exception.UnauthorizedException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberValidUtil {

    public boolean isCustomer(Customer customer) {
        return customer != null;
    }

    public void validateIsStore(Store store) {
        if (store == null) {
            throw new UnauthorizedException("가게만 사용할 수 있는 기능입니다.");
        }
    }

}

package com.example.demo.setting.util;

import com.example.demo.customer.entity.Customer;
import com.example.demo.store.entity.Store;
import com.example.demo.setting.exception.UnauthorizedException;
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

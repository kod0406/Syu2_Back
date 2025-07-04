package com.example.demo;

import com.example.demo.geolocation.*;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.entity.StoreMenu;
import com.example.demo.store.repository.MenuRepository;
import com.example.demo.store.repository.StoreLocationRepository;
import com.example.demo.store.repository.StoreRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class DemoApplicationTests {
    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private GeoService geoService;

    @Test
    @Rollback(true)  // 테스트 후 롤백하도록 변경
    @Transactional
    @DisplayName("storeId로 메뉴 리스트 조회")
    void testFindAllByStoreId() {
        // given
        Store store = Store.builder()
                .storeName("재열이네 분식")
                .ownerEmail("owner@example.com")
                .password("pw123")
                .provider("local")
                .build();
        storeRepository.save(store);

        StoreMenu menu1 = StoreMenu.builder()
                .menuName("김밥")
                .price(3000)
                .rating(4.5)
                .description("맛있는 김밥")
                .imageUrl("https://example.com/img/kimbap.jpg")
                .store(store)
                .build();

        StoreMenu menu2 = StoreMenu.builder()
                .menuName("떡볶이")
                .price(4000)
                .rating(4.8)
                .description("매콤한 떡볶이")
                .imageUrl("https://example.com/img/tteokbokki.jpg")
                .store(store)
                .build();

        menuRepository.save(menu1);
        menuRepository.save(menu2);
        List<StoreMenu> result = menuRepository.findAllByStore_StoreId(store.getStoreId());

    }

    @Test

    void check(){

            SimpleAddressDto simpleAddressDto = new SimpleAddressDto("서울특별시","남양주", "공릉동");
            System.out.println("getCity + " + simpleAddressDto.getCity());
            System.out.println("getDistrict + " + simpleAddressDto.getDistrict());
            System.out.println("getTown + "  + simpleAddressDto.getTown());
            List<StoreLocation> locations = storeLocationRepository.findByCityAndDistrict(
                    simpleAddressDto.getTown()
            );
            for (StoreLocation storeLocation : locations) {
                System.out.println(storeLocation.getStore());
            }
            System.out.println( locations.stream()
                    .map(StoreLocation::getStore)              // StoreLocation → Store
                    .map(GeoResponseStoreDto::new)             // Store → DTO
                    .toList());

    }


}

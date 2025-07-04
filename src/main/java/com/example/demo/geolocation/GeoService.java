package com.example.demo.geolocation;

import com.example.demo.benefit.dto.CouponDto;
import com.example.demo.benefit.entity.Coupon;
import com.example.demo.benefit.repository.CouponRepository;
import com.example.demo.store.entity.Store;
import com.example.demo.store.entity.StoreLocation;
import com.example.demo.store.repository.StoreLocationRepository;
import com.example.demo.store.repository.StoreRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class GeoService {
    private final StoreRepository storeRepository;
    private final CouponRepository couponRepository;
    private final StoreLocationRepository storeLocationRepository;

    @Value("${naver.cloud.AccessKey}")
    private String geoAccessKey;

    @Value("${naver.cloud.SecretKey}")
    private String geoSecret;


    public Map<String, String> makeHeaders() {
        log.info("geoAccessKey: " + geoAccessKey);
        log.info("geoSecret: " + geoSecret);
        Map<String, String> headers = new HashMap<>();
        headers.put("x-ncp-apigw-api-key-id", geoAccessKey);
        headers.put("x-ncp-apigw-api-key", geoSecret);

        return headers;
    }


    public SimpleAddressDto requestGeolocation(GeoRequestDto geoRequestDto) {
        try {
            String lat = geoRequestDto.getLatitude();
            String lng = geoRequestDto.getLongitude();
//            String url = "https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc?coords="
//                    + lng + "," + lat + "&output=json&orders=legalcode%2Cadmcode%2Caddr%2Croadaddr";
            String url = UriComponentsBuilder
                    .fromHttpUrl("https://maps.apigw.ntruss.com/map-reversegeocode/v2/gc")
                    .queryParam("coords", lng + "," + lat)
                    .queryParam("output", "json")
                    .queryParam("orders", "legalcode,admcode,addr,roadaddr") // 절대 인코딩된 상태로 넣지 말 것
                    .build()
                    .toUriString();


            Map<String, String> headersMap = makeHeaders();

            HttpHeaders headers = new HttpHeaders();
            headers.setAll(headersMap);
            headers.set("User-Agent", "Mozilla/5.0");
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));
            log.info(headers.toString());
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());

            JsonNode area1Node = root.path("results").get(0).path("region").path("area1").path("name");
            JsonNode area2Node = root.path("results").get(0).path("region").path("area2").path("name");
            JsonNode area3Node = root.path("results").get(0).path("region").path("area3").path("name");

            return new SimpleAddressDto(
                    area1Node.asText(), area2Node.asText(), area3Node.asText()
            );
        } catch (HttpClientErrorException e) {
            log.error("❌ API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public List<GeoResponseStoreDto> findStore(SimpleAddressDto simpleAddressDto) {
        log.info("getCity" + simpleAddressDto.getCity());
        log.info("getDistrict" + simpleAddressDto.getDistrict());
        List<StoreLocation> locations = storeLocationRepository.findByDistrict(
                simpleAddressDto.getDistrict()
        );
        for (StoreLocation storeLocation : locations) {
            log.info(storeLocation.getCity());
        }
        return locations.stream()
                .map(StoreLocation::getStore)              // StoreLocation → Store
                .map(GeoResponseStoreDto::new)             // Store → DTO
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getAllAvailableCoupons(SimpleAddressDto simpleAddressDto) {
        List<Coupon> availableCoupons = couponRepository.findAllAroundStoreCoupons(LocalDateTime.now().plusHours(9), simpleAddressDto.getCity(), simpleAddressDto.getDistrict());
        return availableCoupons.stream()
                .map(CouponDto::fromEntity)
                .collect(Collectors.toList());
    }


}

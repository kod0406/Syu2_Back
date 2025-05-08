package com.example.demo.entity.store;

import com.example.demo.entity.customer.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class QR_Code {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long QRId;

    private String QR_Code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id") // 외래키 이름
    private Store store;
}

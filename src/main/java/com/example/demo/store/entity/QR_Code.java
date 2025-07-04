package com.example.demo.store.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference("store-qr")
    private Store store;

    public void updateUrl(String url){
        this.QR_Code = url;
    }
}

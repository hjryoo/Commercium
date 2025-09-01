package com.commercium.order.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Embeddable;

@Embeddable
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class ShippingAddress {

    private String recipientName;
    private String phone;
    private String zipCode;
    private String address1;
    private String address2;

    protected ShippingAddress() {} // JPA용

    public static ShippingAddress of(String recipientName, String phone,
                                     String zipCode, String address1, String address2) {
        validateRequired(recipientName, "수령인명");
        validateRequired(phone, "연락처");
        validateRequired(zipCode, "우편번호");
        validateRequired(address1, "주소");

        return new ShippingAddress(recipientName, phone, zipCode, address1, address2);
    }

    private static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은 필수입니다");
        }
    }

    public String getFullAddress() {
        return String.format("(%s) %s %s", zipCode, address1,
                address2 != null ? address2 : "").trim();
    }
}

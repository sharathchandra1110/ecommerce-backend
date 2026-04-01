package com.ecommerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderRequest {
    @NotBlank
    private String shippingAddress;

    private String paymentMethod;
}
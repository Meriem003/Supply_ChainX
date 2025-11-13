package com.supplychainx.livraison.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponseDTO {

    private Long idCustomer;
    private String name;
    private String address;
    private String city;
}

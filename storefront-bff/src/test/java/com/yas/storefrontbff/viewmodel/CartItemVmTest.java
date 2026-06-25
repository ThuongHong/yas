package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CartItemVmTest {

    @Test
    void fromCartDetailVm_shouldMapProductIdAndQuantity() {
        CartDetailVm detail = new CartDetailVm(10L, 99L, 3);

        CartItemVm item = CartItemVm.fromCartDetailVm(detail);

        assertThat(item.productId()).isEqualTo(99L);
        assertThat(item.quantity()).isEqualTo(3);
    }
}

package com.yas.sampledata.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    @Test
    void getMessage_shouldReturnMessageCodeWhenBundleHasNoEntryEvenWithArguments() {
        String message = MessagesUtils.getMessage("api.data_not_found", "Product");

        assertThat(message).isEqualTo("api.data_not_found");
    }

    @Test
    void getMessage_shouldReturnErrorCodeWhenMessageKeyMissing() {
        String message = MessagesUtils.getMessage("missing.message.key");

        assertThat(message).isEqualTo("missing.message.key");
    }
}

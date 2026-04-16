package com.yas.sampledata.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessagesUtilsTest {

    private ResourceBundle originalMessageBundle;

    @BeforeEach
    void setUp() {
        originalMessageBundle = MessagesUtils.messageBundle;
        MessagesUtils.messageBundle = new ListResourceBundle() {
            @Override
            protected Object[][] getContents() {
                return new Object[][] {
                    { "api.data_not_found", "{} not found" }
                };
            }
        };
    }

    @AfterEach
    void tearDown() {
        MessagesUtils.messageBundle = originalMessageBundle;
    }

    @Test
    void getMessage_shouldFormatMessageWhenBundleHasEntry() {
        String message = MessagesUtils.getMessage("api.data_not_found", "Product");

        assertThat(message).isEqualTo("Product not found");
    }

    @Test
    void getMessage_shouldReturnErrorCodeWhenMessageKeyMissing() {
        String message = MessagesUtils.getMessage("missing.message.key");

        assertThat(message).isEqualTo("missing.message.key");
    }
}

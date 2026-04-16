package com.yas.sampledata.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void compactConstructor_shouldCreateEmptyFieldErrorsList() {
        ErrorVm errorVm = new ErrorVm("400", "Bad request", "Invalid body");

        assertThat(errorVm.statusCode()).isEqualTo("400");
        assertThat(errorVm.title()).isEqualTo("Bad request");
        assertThat(errorVm.detail()).isEqualTo("Invalid body");
        assertThat(errorVm.fieldErrors()).isEmpty();
    }

    @Test
    void canonicalConstructor_shouldKeepProvidedFieldErrors() {
        ErrorVm errorVm = new ErrorVm("400", "Bad request", "Invalid body", List.of("field a"));

        assertThat(errorVm.fieldErrors()).containsExactly("field a");
    }
}

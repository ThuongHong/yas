package com.yas.sampledata.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SampleDataVmTest {

    @Test
    void record_shouldExposeMessageValue() {
        SampleDataVm vm = new SampleDataVm("ok");

        assertThat(vm.message()).isEqualTo("ok");
    }
}

package com.yas.sampledata.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;

class SampleDataControllerTest {

    @Test
    void createSampleData_shouldReturnServiceResponse() {
        SampleDataService sampleDataService = mock(SampleDataService.class);
        SampleDataController controller = new SampleDataController(sampleDataService);
        SampleDataVm expected = new SampleDataVm("Insert Sample Data successfully!");
        when(sampleDataService.createSampleData()).thenReturn(expected);

        SampleDataVm response = controller.createSampleData(new SampleDataVm("ignored"));

        assertThat(response).isEqualTo(expected);
        verify(sampleDataService).createSampleData();
    }
}

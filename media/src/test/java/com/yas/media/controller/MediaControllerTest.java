package com.yas.media.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.media.model.Media;
import com.yas.media.model.dto.MediaDto;
import com.yas.media.service.MediaService;
import com.yas.media.viewmodel.MediaPostVm;
import com.yas.media.viewmodel.MediaVm;
import java.io.ByteArrayInputStream;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.yas.commonlibrary.exception.ApiExceptionHandler;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@ContextConfiguration(classes = {
    MediaController.class,
    ApiExceptionHandler.class
})
@AutoConfigureMockMvc(addFilters = false)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    private Media sampleMedia;
    private MediaVm sampleMediaVm;

    @BeforeEach
    void setUp() {
        sampleMedia = new Media();
        sampleMedia.setId(1L);
        sampleMedia.setCaption("Sample Caption");
        sampleMedia.setFileName("sample.jpg");
        sampleMedia.setMediaType(MediaType.IMAGE_JPEG_VALUE);

        sampleMediaVm = new MediaVm(1L, "Sample Caption", "sample.jpg", MediaType.IMAGE_JPEG_VALUE, "/medias/1/file/sample.jpg");
    }

    @Test
    void create_whenValidRequest_thenReturnsOk() throws Exception {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB), "png", baos);
        MockMultipartFile file = new MockMultipartFile("multipartFile", "sample.png", MediaType.IMAGE_PNG_VALUE, baos.toByteArray());

        when(mediaService.saveMedia(any(MediaPostVm.class))).thenReturn(sampleMedia);

        mockMvc.perform(multipart("/medias")
                .file(file)
                .param("caption", "Sample Caption")
                .param("fileNameOverride", "sample.png"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caption").value("Sample Caption"))
                .andExpect(jsonPath("$.fileName").value("sample.jpg"))
                .andExpect(jsonPath("$.mediaType").value(MediaType.IMAGE_JPEG_VALUE));
    }

    @Test
    void delete_whenValidId_thenReturnsNoContent() throws Exception {
        doNothing().when(mediaService).removeMedia(1L);

        mockMvc.perform(delete("/medias/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void get_whenMediaExists_thenReturnsMediaVm() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(sampleMediaVm);

        mockMvc.perform(get("/medias/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.caption").value("Sample Caption"));
    }

    @Test
    void get_whenMediaDoesNotExist_thenReturnsNotFound() throws Exception {
        when(mediaService.getMediaById(1L)).thenReturn(null);

        mockMvc.perform(get("/medias/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIds_whenMediasExist_thenReturnsList() throws Exception {
        when(mediaService.getMediaByIds(List.of(1L, 2L))).thenReturn(List.of(sampleMediaVm));

        mockMvc.perform(get("/medias")
                .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void getByIds_whenNoMediasExist_thenReturnsNotFound() throws Exception {
        when(mediaService.getMediaByIds(List.of(1L, 2L))).thenReturn(List.of());

        mockMvc.perform(get("/medias")
                .param("ids", "1,2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFile_whenValidRequest_thenReturnsInputStreamResource() throws Exception {
        MediaDto mediaDto = MediaDto.builder()
                .content(new ByteArrayInputStream("test file content".getBytes()))
                .mediaType(MediaType.IMAGE_JPEG)
                .build();

        when(mediaService.getFile(1L, "sample.jpg")).thenReturn(mediaDto);

        mockMvc.perform(get("/medias/{id}/file/{fileName}", 1L, "sample.jpg"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample.jpg\""))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG))
                .andExpect(content().bytes("test file content".getBytes()));
    }
}

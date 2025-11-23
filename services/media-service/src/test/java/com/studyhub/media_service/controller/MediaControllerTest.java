package com.studyhub.media_service.controller;

import com.studyhub.media_service.dto.FileStatusResponse;
import com.studyhub.media_service.dto.UploadResponse;
import com.studyhub.media_service.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MediaController.class)
@WithMockUser
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MediaService mediaService;

    @Test
    void upload_returnsCreatedAndFileId() throws Exception {
        Mockito.when(mediaService.storeFile(eq("test.txt"), any())).thenReturn("file-123");
        Mockito.when(mediaService.getFileUrl(eq("file-123"))).thenReturn("http://localhost:9000/media/file-123");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE, "hello".getBytes());

        mockMvc.perform(multipart("/api/media/upload").file(file).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileId", is("file-123")))
                .andExpect(jsonPath("$.message", is("File uploaded")))
                .andExpect(jsonPath("$.url", is("http://localhost:9000/media/file-123")));
    }

    @Test
    void getFileStatus_returnsNotFound_whenMissing() throws Exception {
        Mockito.when(mediaService.getFileStatus("missing")).thenReturn("NOT_FOUND");

        mockMvc.perform(get("/api/media/files/missing"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getFileStatus_returnsStatus_whenPresent() throws Exception {
        Mockito.when(mediaService.getFileStatus("file-123")).thenReturn("READY");

        mockMvc.perform(get("/api/media/files/file-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileId", is("file-123")))
                .andExpect(jsonPath("$.status", is("READY")));
    }
}

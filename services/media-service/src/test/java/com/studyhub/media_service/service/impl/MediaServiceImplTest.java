package com.studyhub.media_service.service.impl;

import com.studyhub.media_service.storage.StorageService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class MediaServiceImplTest {

    @Test
    void storeFile_callsStorage_and_updatesStatus() throws Exception {
        StorageService storage = Mockito.mock(StorageService.class);
        Mockito.when(storage.store(Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.anyLong(), Mockito.anyString()))
                .thenAnswer(i -> i.getArgument(1));

        MediaServiceImpl svc = new MediaServiceImpl(storage);

        String id = svc.storeFile("file.txt", new ByteArrayInputStream("hello".getBytes()));

        assertThat(id).isNotNull();
        String status = svc.getFileStatus(id);
        assertThat(status).isEqualTo("READY");
    }
}

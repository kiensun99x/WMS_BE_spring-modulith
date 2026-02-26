package com.rk.WMS.batch.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rk.WMS.batch.dto.ManualDispatchRequest;
import com.rk.WMS.batch.service.DispatchService;
import com.rk.WMS.common.exception.ErrorCode;
import com.rk.WMS.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Mục đích: Kiểm tra API endpoint dispatch thủ công
 */
@ExtendWith(MockitoExtension.class)
class ManualDispatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DispatchService dispatchService;

    @InjectMocks
    private ManualDispatchController manualDispatchController;

    private ObjectMapper objectMapper;

    /**
     * Setup trước mỗi test case
     * Khởi tạo MockMvc với controller và global exception handler
     */
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(manualDispatchController)
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    /**
     * Test case: Dispatch thủ công thành công
     * Expected:
     * - HTTP Status 200 (OK)
     * - Code = SUCCESS
     * - Message = "Dispatch thành công"
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - Thành công")
    void manualDispatch_Success() throws Exception {
        // Given - Chuẩn bị request
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList(1L, 2L, 3L));
        request.setWarehouseId(100L);

        // Mock service - không throw exception
        doNothing().when(dispatchService).manualDispatch(anyList(), anyLong());

        // When & Then - Thực hiện request và kiểm tra
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(ErrorCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.message").value("Dispatch thành công"));

        // Verify service được gọi đúng 1 lần với đúng tham số
        verify(dispatchService, times(1)).manualDispatch(
                argThat(list -> list.containsAll(Arrays.asList(1L, 2L, 3L))),
                eq(100L)
        );
    }

    /**
     * Test case: Dispatch thủ công - Request body null
     * Expected: Bad Request (400)
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - Request body null")
    void manualDispatch_NullRequestBody() throws Exception {
        // When & Then
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());

        verify(dispatchService, never()).manualDispatch(anyList(), anyLong());
    }

    /**
     * Test case: Dispatch thủ công - Thiếu trường bắt buộc
     * Expected: Bad Request (400)
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - Thiếu trường bắt buộc")
    void manualDispatch_MissingRequiredFields() throws Exception {
        // Given - Request thiếu warehouseId
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList(1L, 2L, 3L));
        // Không set warehouseId

        // When & Then
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dispatchService, never()).manualDispatch(anyList(), anyLong());
    }

    /**
     * Test case: Dispatch thủ công - orderIds rỗng
     * Expected: Bad Request (400) do @NotEmpty validation
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - orderIds rỗng")
    void manualDispatch_EmptyOrderIds() throws Exception {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList()); // List rỗng
        request.setWarehouseId(100L);

        // When & Then
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dispatchService, never()).manualDispatch(anyList(), anyLong());
    }

    /**
     * Test case: Dispatch thủ công - orderIds null
     * Expected: Bad Request (400) do @NotEmpty validation
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - orderIds null")
    void manualDispatch_NullOrderIds() throws Exception {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(null);
        request.setWarehouseId(100L);

        // When & Then
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dispatchService, never()).manualDispatch(anyList(), anyLong());
    }

    /**
     * Test case: Dispatch thủ công - warehouseId null
     * Expected: Bad Request (400) do @NotNull validation
     */
    @Test
    @DisplayName("POST /batch/dispatch/manual - warehouseId null")
    void manualDispatch_NullWarehouseId() throws Exception {
        // Given
        ManualDispatchRequest request = new ManualDispatchRequest();
        request.setOrderIds(Arrays.asList(1L, 2L, 3L));
        request.setWarehouseId(null);

        // When & Then
        mockMvc.perform(post("/batch/dispatch/manual")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dispatchService, never()).manualDispatch(anyList(), anyLong());
    }
}

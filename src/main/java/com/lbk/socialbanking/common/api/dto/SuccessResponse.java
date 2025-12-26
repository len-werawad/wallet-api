package com.lbk.socialbanking.common.api.dto;

public record SuccessResponse<T>(T data) {
    public static <T> SuccessResponse<T> of(T data) {
        return new SuccessResponse<>(data);
    }
}

package com.lbk.socialbanking.common.config;

import com.lbk.socialbanking.common.api.dto.PaginatedResponse;
import com.lbk.socialbanking.common.api.dto.SuccessResponse;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Customizes Swagger/OpenAPI documentation to reflect the actual response structure
 * that gets wrapped by SuccessResponseAdvice at runtime.
 */
@Component
public class SwaggerResponseWrapperCustomizer implements OperationCustomizer {

    private static final List<String> SKIP_PATHS = List.of("springdoc", "swagger", "actuator");

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        if (operation.getResponses() == null || shouldSkipWrapping(handlerMethod)) {
            return operation;
        }

        Type returnType = handlerMethod.getMethod().getGenericReturnType();

        if (isAlreadyWrapped(returnType) || isVoidReturn(returnType)) {
            return operation;
        }

        addDefaultErrorResponses(operation);
        wrapExistingResponses(operation, returnType);

        return operation;
    }

    private void wrapExistingResponses(Operation operation, Type returnType) {
        operation.getResponses().forEach((code, response) -> {
            if (response.getContent() == null) return;

            if (isSuccessCode(code)) {
                wrapSuccessResponse(response, returnType);
            } else if (isErrorCode(code)) {
                wrapErrorResponse(response);
            }
        });
    }

    private boolean shouldSkipWrapping(HandlerMethod handlerMethod) {
        String className = handlerMethod.getBeanType().getName();
        return SKIP_PATHS.stream().anyMatch(className::contains);
    }

    private boolean isAlreadyWrapped(Type type) {
        if (type instanceof ParameterizedType paramType) {
            Type rawType = paramType.getRawType();
            return rawType.equals(SuccessResponse.class) ||
                   rawType.equals(PaginatedResponse.class);
        }
        return false;
    }

    private boolean isVoidReturn(Type type) {
        return type.getTypeName().equals("void");
    }

    private boolean isSuccessCode(String code) {
        return code.startsWith("2");
    }

    private boolean isErrorCode(String code) {
        return code.startsWith("4") || code.startsWith("5");
    }

    private void wrapSuccessResponse(ApiResponse response, Type originalType) {
        if (isPaginatedResponse(originalType)) return;

        response.getContent().forEach((mediaTypeKey, mediaType) -> {
            Schema<?> originalSchema = mediaType.getSchema();
            if (originalSchema != null) {
                mediaType.setSchema(createSuccessWrapper(originalSchema));
            }
        });
    }

    private void wrapErrorResponse(ApiResponse response) {
        Schema<?> errorSchema = createErrorEnvelope();
        response.getContent().forEach((mediaTypeKey, mediaType) ->
            mediaType.setSchema(errorSchema)
        );
    }

    private boolean isPaginatedResponse(Type type) {
        return type instanceof ParameterizedType paramType &&
               paramType.getRawType().equals(PaginatedResponse.class);
    }

    private void addDefaultErrorResponses(Operation operation) {
        ApiResponses responses = operation.getResponses();

        if (!responses.containsKey("400")) {
            responses.addApiResponse("400", createErrorApiResponse(
                    "Bad Request - Invalid input or validation error",
                    400,
                    "VALIDATION_ERROR",
                    "Invalid input"
            ));
        }

        if (!responses.containsKey("401")) {
            responses.addApiResponse("401", createErrorApiResponse(
                    "Unauthorized - Missing or invalid authentication token",
                    401,
                    "UNAUTHORIZED",
                    "Missing or invalid authentication token"
            ));
        }

        if (!responses.containsKey("500")) {
            responses.addApiResponse("500", createErrorApiResponse(
                    "Internal Server Error - Unexpected error occurred",
                    500,
                    "INTERNAL_SERVER_ERROR",
                    "Unexpected error occurred"
            ));
        }
    }

    private ApiResponse createErrorApiResponse(String description, int status, String code, String message) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(createErrorEnvelope());
        mediaType.setExample(createErrorExample(status, code, message));

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }

    private Map<String, Object> createErrorExample(int status, String code, String message) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("status", status);
        error.put("code", code);
        error.put("message", message);
        error.put("traceId", "abcd1234efgh5678");

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        return response;
    }

    // Schema Builders

    private Schema<?> createSuccessWrapper(Schema<?> dataSchema) {
        Schema<?> wrapper = new Schema<>();
        wrapper.setType("object");
        wrapper.addProperty("data", dataSchema);
        wrapper.setRequired(List.of("data"));
        return wrapper;
    }

    private Schema<?> createErrorEnvelope() {
        Schema<?> envelope = new Schema<>();
        envelope.setType("object");
        envelope.addProperty("error", createErrorResponseSchema());
        envelope.setRequired(List.of("error"));
        return envelope;
    }

    private Schema<?> createErrorResponseSchema() {
        Schema<?> error = new Schema<>();
        error.setType("object");
        error.addProperty("status", createIntegerProperty("HTTP status code", 400));
        error.addProperty("code", createStringProperty("Error code", "VALIDATION_ERROR"));
        error.addProperty("message", createStringProperty("Error message", "Invalid input"));
        error.addProperty("traceId", createStringProperty("Request trace ID", "abcd1234efgh5678"));
        error.setRequired(List.of("status", "code", "message", "traceId"));
        return error;
    }

    private Schema<?> createIntegerProperty(String description, Object example) {
        return new Schema<>().type("integer").description(description).example(example);
    }

    private Schema<?> createStringProperty(String description, Object example) {
        return new Schema<>().type("string").description(description).example(example);
    }
}
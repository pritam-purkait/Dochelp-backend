package com.pritam.dochelp.controller;

import com.pritam.dochelp.dto.*;
import com.pritam.dochelp.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/chat")
@Tag(
        name="Chat Management",
        description="All chat related apis"
)
@RequiredArgsConstructor
public class ChatController {

    private final RagService ragService;

    @PostMapping("/query")
    @Operation(
            summary = "Ask a question",
            description = "Ask a question to the chatbot"
    )
    public ResponseEntity<ApiResponse<ChatResponseDto>> askQuestion(
           @Valid @RequestBody ChatRequestDto chatRequestDto
    ){

        ChatResponseDto chatResponseDto = ragService.askQuestion(chatRequestDto);

        return ResponseEntity.ok(
                ApiResponse.<ChatResponseDto>builder()
                        .success(true)
                        .message(null)
                        .data(chatResponseDto)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }

    @PostMapping("/stream")
    @Operation(
            summary = "Stream a question",
            description = "Stream a question to the chatbot"
    )
    public Flux<String> streamQuestion(
            @Valid @RequestBody ChatRequestDto chatRequestDto
    ){
        return ragService.streamQuestionAnswer(chatRequestDto);
    }

    @PostMapping("/search/similarity")
    @Operation(
            summary = "Search for similar chunks",
            description = "Search for similar chunks in the knowledge base"
    )
    public ResponseEntity<ApiResponse<SearchResultDto>> searchSimilar(
            @Valid @RequestBody SearchRequestDto searchRequestDto
    ){
      ragService.searchSimilarChunks(searchRequestDto);

        return ResponseEntity.ok(
                ApiResponse.<SearchResultDto>builder()
                        .success(true)
                        .message(null)
                        .data(null)
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }


}

package com.pritam.dochelp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatRequestDto {

    @NotBlank(message = "Question cannot be blank")
    private String question;

    private UUID documentId;
    private Integer topK;
    private Double minSimilarity;
    private String conversationId;
}

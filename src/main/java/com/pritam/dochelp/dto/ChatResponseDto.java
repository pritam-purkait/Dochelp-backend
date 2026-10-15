package com.pritam.dochelp.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatResponseDto {
    private String answer;
    private String conversationId;
    private List<CitationDto> citiations;
    private Long responseTimeMs;

}

package com.pritam.dochelp.dto;

import com.pritam.dochelp.entity.DocumentStatus;
import lombok.*;

import java.util.UUID;



@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResponseDto {

    private UUID id;
    private String filename;
    private Long fileSize;
    private DocumentStatus status;
    private Integer chunksCreated;
    private String message;
}

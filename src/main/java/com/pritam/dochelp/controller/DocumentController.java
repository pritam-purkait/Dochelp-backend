package com.pritam.dochelp.controller;

import com.pritam.dochelp.dto.ApiResponse;
import com.pritam.dochelp.dto.DocumentResponseDto;
import com.pritam.dochelp.service.DocumentMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController()
@RequestMapping("/api/v1/document")
@Tag(
        name="Document Management",
        description = "Endpoints for uploading, listing and managing documents and their vector embeddings"

)
@RequiredArgsConstructor
public class DocumentController {


    private final DocumentMetadataService documentService;

    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload a new document and index  it // PDF DOCX TEXT MD CSV"
            ,description = "This api is used to upload and index document files"
    )
    public ResponseEntity<ApiResponse<DocumentResponseDto>> uploadDocument(
            @RequestParam(value = "file")MultipartFile file
    ) {

        DocumentResponseDto documentResponseDto= this.documentService.uploadAndProcess(file);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<DocumentResponseDto>builder()
                                .success(true)
                                .message("Document uploaded and indexed successfully. ")
                                .data(documentResponseDto)
                                .timestamp(LocalDateTime.now())
                                .build()
                );

    }
}

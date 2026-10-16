package com.pritam.dochelp.controller;

import com.pritam.dochelp.dto.ApiResponse;
import com.pritam.dochelp.dto.DocumentMetadataDto;
import com.pritam.dochelp.dto.DocumentResponseDto;
import com.pritam.dochelp.service.DocumentMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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


    @PostMapping(value="/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload multiple documents",
            description="This api used to upload multiple document"
    )
    public ResponseEntity<ApiResponse<List<DocumentResponseDto>>> uploadMultiple(
            @RequestParam(value = "files")List<MultipartFile> files
    ){
        List<DocumentResponseDto> responseDtos = documentService.uploadMultipleDocuments(files);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.<List<DocumentResponseDto>>builder()
                                .success(true)
                                .message("Documents uploaded and indexed successfully. ")
                                .data(responseDtos)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

//   List all uploaded documents
    @GetMapping
    @Operation(
            summary = "List all uploaded documents",
            description="This api used to list all uploaded documents"
    )
    public ResponseEntity<ApiResponse<List<DocumentMetadataDto>>> getAllDocuments(){
        List<DocumentMetadataDto> documents = documentService.getAllDocuments();

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<List<DocumentMetadataDto>>builder()
                                .success(true)
                                .message("Documents fetched successfully. ")
                                .data(documents)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get document by id",
            description="This api used to get document by id"
    )
    public ResponseEntity<ApiResponse<DocumentMetadataDto>> getDocumentById(@PathVariable UUID id){
        DocumentMetadataDto document = documentService.getDocumentById(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        ApiResponse.<DocumentMetadataDto>builder()
                                .success(true)
                                .message("Document fetched successfully. ")
                                .data(document)
                                .timestamp(LocalDateTime.now())
                                .build()
                );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete document by id",
            description="This api used to delete document by id"
    )
    public ResponseEntity<ApiResponse<DocumentResponseDto>> deleteDocumentById(@PathVariable UUID id){

        documentService.deleteDocumentById(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                ApiResponse.<DocumentResponseDto>builder()
                        .success(true)
                        .message("Document deleted successfully. ")
                        .timestamp(LocalDateTime.now())
                        .build()
        );
    }
}

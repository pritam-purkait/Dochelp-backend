package com.pritam.dochelp.service;

import com.pritam.dochelp.dto.DocumentMetadataDto;
import com.pritam.dochelp.dto.DocumentResponseDto;
import com.pritam.dochelp.entity.DocumentMetadata;
import com.pritam.dochelp.entity.DocumentStatus;
import com.pritam.dochelp.exception.DocumentProcessingException;
import com.pritam.dochelp.exception.ResourceNotFoundException;
import com.pritam.dochelp.repository.DocumentMetadataRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor //used for dependency injection (constructor injection)
public class DocumentMetadataService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetadataService.class);

    private final DocumentMetadataRepo documentMetadataRepo;
    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;
    private final ModelMapper modelMapper;
    private final JdbcTemplate jdbcTemplate;

    //method to upload and parse document
    public DocumentResponseDto uploadAndProcess(MultipartFile file) {

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octat-strem";

        //building metadata for document
        DocumentMetadata documentMetadata = DocumentMetadata
                .builder()
                .filename(filename)
                .contentType(contentType)
                .status(DocumentStatus.UPLOADING)
                .fileSize(file.getSize())
                .createdAt(LocalDateTime.now())
                .build();

        //save the document metadata
        documentMetadata = documentMetadataRepo.save(documentMetadata);

        List<Document> parsedDocs = null;
        int chunksCreated = 0;

        try {
            //parse the file
            parsedDocs = parserService.parse(file);

            //ingest service
             chunksCreated = ingestionService.ingest(documentMetadata, parsedDocs);
            // documentMetadata.setTotalChunks(chunksCreated);
        } catch (DocumentProcessingException e) {
            log.error("Error processing document: {}", e.getMessage());
            documentMetadataRepo.delete(documentMetadata);
            throw e;
        }


        return DocumentResponseDto.builder()
                .id(documentMetadata.getId())
                .chunksCreated(chunksCreated)
                .filename(documentMetadata.getFilename())
                .fileSize(documentMetadata.getFileSize())
                .status(documentMetadata.getStatus())
                .message("Document indexed and processed successfully")
                .build();
    }

    public List<DocumentResponseDto> uploadMultipleDocuments(List<MultipartFile> files) {

        List<DocumentResponseDto> responseDtos = new ArrayList<>();

        for (MultipartFile f : files) {
            DocumentResponseDto result = uploadAndProcess(f);
            responseDtos.add(result);
        }

        return responseDtos;
    }

    public List<DocumentMetadataDto> getAllDocuments() {

        List<DocumentMetadata> documentMetadataList = documentMetadataRepo.findAllByOrderByCreatedAtDesc();

        return documentMetadataList.stream()
                .map(documentMetadata -> modelMapper.map(documentMetadata, DocumentMetadataDto.class))
                .toList();
    }

    public DocumentMetadataDto getDocumentById(UUID id) {

        DocumentMetadata documentMetadata = documentMetadataRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Document not found with id: " + id)
        );

        return modelMapper.map(documentMetadata, DocumentMetadataDto.class);
    }

    public void deleteDocumentById(UUID id) {

        DocumentMetadata documentMetadata = documentMetadataRepo.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Document not found with id: " + id)
        );
        //delete the vector entries
        try {
            String deleteVectorsSQL = "DELETE FROM vector_store WHERE metadata->>'document_id' = ?";
            int deleteCount = jdbcTemplate.update(deleteVectorsSQL, id.toString());
            log.info("Deleted {} vectors for document with id: {}", deleteCount, id);
        } catch (Exception e) {
            log.warn("Error deleting vectors for document: {}", e.getMessage());
        }
    }
}

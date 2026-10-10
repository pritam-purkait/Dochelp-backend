package com.pritam.dochelp.service;

import com.pritam.dochelp.dto.DocumentResponseDto;
import com.pritam.dochelp.entity.DocumentMetadata;
import com.pritam.dochelp.entity.DocumentStatus;
import com.pritam.dochelp.repository.DocumentMetadataRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor //used for dependency injection (constructor injection)
public class DocumentMetadataService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMetadataService.class);

    private final DocumentMetadataRepo documentMetadataRepo;
    private final DocumentParserService parserService;
    private final DocumentIngestionService ingestionService;
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


        //parse the file
        List<Document> parsedDocs = parserService.parse(file);

        //ingest service
        int chunksCreated = ingestionService.ingest(documentMetadata, parsedDocs);
       // documentMetadata.setTotalChunks(chunksCreated);



        return DocumentResponseDto.builder()
                .id(documentMetadata.getId())
                .chunksCreated(chunksCreated)
                .filename(documentMetadata.getFilename())
                .fileSize(documentMetadata.getFileSize())
                .status(documentMetadata.getStatus())
                .message("Document indexed and processed successfully")
                .build();
    }
}

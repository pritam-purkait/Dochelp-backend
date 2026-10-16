package com.pritam.dochelp.repository;

import com.pritam.dochelp.entity.DocumentMetadata;
import com.pritam.dochelp.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentMetadataRepo extends JpaRepository<DocumentMetadata, UUID> {

    List<DocumentMetadata> findAllByOrderByCreatedAtDesc();
    List<DocumentMetadata> findByStatus(DocumentStatus status);
}

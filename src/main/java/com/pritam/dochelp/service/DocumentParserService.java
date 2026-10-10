package com.pritam.dochelp.service;

import com.pritam.dochelp.exception.DocumentProcessingException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;



@Service
public class DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserService.class);



    public List<Document> parse(MultipartFile file) {

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";

        log.info("Parsing document: {} , size: {} bytes, content-type: {}", fileName, file.getSize(), contentType);

        try{

            Resource resource = new ByteArrayResource(file.getBytes()){
                @Override
                public @Nullable String getFilename() {
                    return fileName;
                }
            };

            if(fileName.toLowerCase().endsWith(".pdf") || contentType.contains("pdf")){
                return parsePdf(resource);
            }
            else{
                return parseGenericFile(resource);
            }



        }catch(IOException e){
            log.error("Error parsing document: {}", fileName, e);
            throw new DocumentProcessingException("could not read the uploaded document : " + fileName, e);
        } catch (Exception e) {
            log.error("Failed to parse document content : {}", fileName, e);
            throw new DocumentProcessingException("Failed to parse document content : " + fileName, e);
        }

    }

    private List<Document> parseGenericFile(Resource resource) {
        TikaDocumentReader tikaDocumentReader= new TikaDocumentReader(resource);
        return tikaDocumentReader.read();
    }

    private List<Document> parsePdf(Resource resource) {

        PdfDocumentReaderConfig config = PdfDocumentReaderConfig.builder()
                .withPageBottomMargin(0)
                .withPageTopMargin(0)
                .build();

        PagePdfDocumentReader documentReader = new PagePdfDocumentReader(resource, config);
        return documentReader.read();
    }
}

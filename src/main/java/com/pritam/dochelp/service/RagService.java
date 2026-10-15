package com.pritam.dochelp.service;

import com.pritam.dochelp.config.AppProperties;
import com.pritam.dochelp.dto.*;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final AppProperties appProperties;
    private final ChatClient chatClient;

    //ask
    public ChatResponseDto askQuestion(ChatRequestDto request){
        long startTime = System.currentTimeMillis();
        log.info("Processing query: '{}' , scoped documentId: {} ", request.getQuestion(), request.getDocumentId());

       List<Document> similarDocuments =  this.retrieveRelevantDocuments(
                request.getQuestion(),
                request.getDocumentId(),
                request.getTopK(),
                request.getMinSimilarity()
       );

        List<CitationDto> citationDtos = similarDocuments.stream()
                .map(this::mapToCitation)
                .toList();

        String contextText = buildContextString(similarDocuments);

        String prompt = buildPrompt(request.getQuestion(), contextText);

        String answer = this.chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        long responseTime = System.currentTimeMillis() - startTime;
        log.info("Response time {} ms with {} citations", responseTime, citationDtos.size());

        return ChatResponseDto.builder()
                .answer(answer)
                .conversationId(request.getConversationId() != null ? request.getConversationId() : UUID.randomUUID().toString())
                .responseTimeMs(responseTime)
                .citiations(citationDtos)
                .build();



    }

    // this streams
    public Flux<String> streamQuestionAnswer(ChatRequestDto requestDto) {
        log.info("Streaming query: '{}'", requestDto.getQuestion());

        List<Document> relevantDocuments = retrieveRelevantDocuments(
                requestDto.getQuestion(),
                requestDto.getDocumentId(),
                requestDto.getTopK(),
                requestDto.getMinSimilarity()
        );
      String contextText = buildContextString(relevantDocuments);
      String userPrompt = buildPrompt(requestDto.getQuestion(), contextText);

      return chatClient.prompt()
              .user(userPrompt)
              .stream()
              .content();

    }

    private String buildPrompt(@NotBlank(message = "Question cannot be blank") String question, String contextText) {

        if(contextText != null && !contextText.isBlank()){

            return String.format("""
                            Document Context:
                            ---------------------
                            %s
                            ---------------------                                       \s
                            User Message / Question: %s             \s
                            Instructions:
                            - If the user's question relates to the document context above, prioritize answering using that context and reference key sections.
                            - If the user is asking a general question, greeting, or discussing topics beyond the document context, respond helpfully and conversationally using your general knowledge while weaving in relevant document context if applicable
                    
                    
                    """, contextText, question);
        }else {
            return String.format("""
                       User Message / Question:
                       %s                         \s
                       Instructions:
                          - Respond helpfully, accurately, and conversationally to the user's message using your broad knowledge base.
                    
                    """, question);
        }
    }

    private String buildContextString(List<Document> similarDocuments) {
        if(similarDocuments == null || similarDocuments.isEmpty()) {
            return "";
        }
        return similarDocuments.stream()
                .map(document -> {
                    String fileName = (String) document.getMetadata().getOrDefault("fileName", "Unknown File");
                    Object page = document.getMetadata().getOrDefault("pageNumber", "N/A");
                    return String.format("[Source: %s | Page: %s]\n%s", fileName,  page, document.getText());
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    public SearchResultDto searchSimilarChunks(SearchRequestDto searchRequestDto) {

        List<Document> matchedDocs = retrieveRelevantDocuments(
                searchRequestDto.getQuery(),
                searchRequestDto.getDocumentId(),
                searchRequestDto.getTopK(),
                searchRequestDto.getSimilaritySearch()
        );

        List<CitationDto> citations = matchedDocs.stream()
                .map(this::mapToCitation)
                .toList();

        return SearchResultDto.builder()
                .query(searchRequestDto.getQuery())
                .totalMatches(citations.size())
                .matches(citations)
                .build();
    }

    private CitationDto mapToCitation(Document document) {

        Map<String, Object> metadata = document.getMetadata();
        UUID docId = null;
        if(metadata.get("documentId") != null){
            try{
                docId = UUID.fromString(metadata.get("documentId").toString());
            }catch(Exception ignore){

            }
        }
        Integer chunkIndex = null;
        if(metadata.get("chunkIndex") instanceof Number n){
            chunkIndex = n.intValue();
        }

        Integer pageNumber = null;
        if(metadata.get("pageNumber") instanceof Number n){
            pageNumber = n.intValue();
        } else if (metadata.get("pageNumber") instanceof Number n) {
            pageNumber = n.intValue();
        }
        Double score = null;
        if(metadata.get("score") instanceof Number n){
            score = 1.0 -  n.doubleValue();
        }

        return CitationDto.builder()
                .documentId(docId)
                .filename((String) metadata.getOrDefault("filename", "Unknown"))
                .chunkIndex(chunkIndex)
                .pageNumber(pageNumber)
                .snippet(document.getText())
                .similarityScore(score)
                .metadata(metadata)
                .build();
    }

    private List<Document> retrieveRelevantDocuments(@NotBlank(message = "Query cannot be empty") String query, UUID documentId, Integer topK, Double similaritySearch) {

        int effectiveTopK = (topK != null && topK > 0) ? topK : appProperties.getRag().getTopK();

        double effectiveSimilarity = (similaritySearch != null) ? similaritySearch : appProperties.getRag().getSimilarityThreshold();

        SearchRequest.Builder searchRequestBuilder = SearchRequest
                .builder()
                .query(query)
                .topK(effectiveTopK);

        if(effectiveSimilarity > 0.0)
            searchRequestBuilder.similarityThreshold(effectiveSimilarity);

        if(documentId != null)
        {
            FilterExpressionBuilder b = new FilterExpressionBuilder();
            searchRequestBuilder.filterExpression(b.eq("documentId", documentId.toString()).build());
        }

        try {
            List<Document> documents = vectorStore.similaritySearch(searchRequestBuilder.build());
            log.info("Retrieved {} chunks for query: '{}' (scoped docId: {})", documents.size(), query, documentId);
            return documents;
        } catch (Exception e) {
            log.error("Similarity search failed for query: '{}'", query, e);
            return Collections.emptyList();
        }


    }

}


package com.ai_mcp_sandbox.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.AbstractEmbeddingModel;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class VectorStoreConfig {

	@Value("${spring.ai.vectorstore.qdrant.host:qdrant-service}")
	private String host;

	@Value("${spring.ai.vectorstore.qdrant.port:6334}")
	private int port;

	@Value("${spring.ai.vectorstore.qdrant.collection-name:hotel_embeddings}")
	private String collectionName;

	@Value("${GEMINI_API_KEY}")
	private String geminiApiKey;

	@Bean
	public QdrantClient qdrantClient() {
		return new QdrantClient(
				QdrantGrpcClient.newBuilder(host, port, false).build()
		);
	}

	@Bean
	@Primary
	public EmbeddingModel embeddingModel() {
		RestClient restClient = RestClient.builder().build();
		ObjectMapper objectMapper = new ObjectMapper();

		return new AbstractEmbeddingModel() {
			@Override
			public EmbeddingResponse call(EmbeddingRequest request) {
				List<Embedding> embeddings = new ArrayList<>();
				int index = 0;
				for (String text : request.getInstructions()) {
					List<Float> vector = getEmbeddingVector(text);
					embeddings.add(new Embedding(vector, index++));
				}
				return new EmbeddingResponse(embeddings);
			}

			@Override
			public List<Float> embed(Document document) {
				return getEmbeddingVector(document.getText());
			}

			@Override
			public int dimensions() {
				return 768;
			}

			private List<Float> getEmbeddingVector(String text) {
				try {
					String url = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent?key=" + geminiApiKey;
					Map<String, Object> body = Map.of(
							"content", Map.of("parts", List.of(Map.of("text", text)))
					);

					String response = restClient.post()
							.uri(url)
							.contentType(MediaType.APPLICATION_JSON)
							.body(body)
							.retrieve()
							.body(String.class);

					JsonNode jsonNode = objectMapper.readTree(response);
					JsonNode values = jsonNode.path("embedding").path("values");
					List<Float> vector = new ArrayList<>();
					if (values.isArray()) {
						for (JsonNode val : values) {
							vector.add((float) val.asDouble());
						}
					}
					return vector;
				} catch (Exception e) {
					throw new RuntimeException("Failed to generate Gemini embedding", e);
				}
			}
		};
	}

	@Bean
	@Primary
	public QdrantVectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
		return QdrantVectorStore.builder(qdrantClient, embeddingModel)
				.collectionName(collectionName)
				.initializeSchema(true)
				.build();
	}
}
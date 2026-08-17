package com.ai_mcp_sandbox.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;

@Configuration
public class VectorStoreConfig {

	@Bean
	public QdrantClient qdrantClient() {
		return new QdrantClient(
				QdrantGrpcClient.newBuilder("qdrant-service", 6334, false).build()
		);
	}

	@Bean
	public QdrantVectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
		return QdrantVectorStore.builder(qdrantClient, embeddingModel)
				.collectionName("hotel_embeddings")
				.initializeSchema(true)
				.build();
	}
}
package com.ai_mcp_sandbox.config;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VectorStoreConfig {

	@Value("${spring.ai.vectorstore.qdrant.host:qdrant-service}")
	private String host;

	@Value("${spring.ai.vectorstore.qdrant.port:6334}")
	private int port;

	@Value("${spring.ai.vectorstore.qdrant.collection-name:hotel_embeddings}")
	private String collectionName;

	@Bean
	public QdrantClient qdrantClient() {
		return new QdrantClient(
				QdrantGrpcClient.newBuilder(host, port, false).build()
		);
	}

	@Bean
	public QdrantVectorStore vectorStore(QdrantClient qdrantClient, EmbeddingModel embeddingModel) {
		return QdrantVectorStore.builder(qdrantClient, embeddingModel)
				.collectionName(collectionName)
				.initializeSchema(false)
				.build();
	}
}
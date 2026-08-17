terraform {
  required_version = ">= 1.0"
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }

  backend "gcs" {
    bucket = "hotelbooking-gcp-tfstate-aliaz"
    prefix = "terraform/state"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = "asia-south1"
}

variable "gcp_project_id" {
  type        = string
  description = "The ID of your Google Cloud Project"
}

# 1. Dedicated VPC for AI MCP Services
resource "google_compute_network" "ai_vpc" {
  name                    = "ai-mcp-network"
  auto_create_subnetworks = true
}

# 2. GKE Autopilot Cluster
resource "google_container_cluster" "ai_cluster" {
  name     = "ai-mcp-cluster"
  location = "asia-south1"
  network  = google_compute_network.ai_vpc.name

  enable_autopilot    = true
  deletion_protection = false # <-- Crucial for terraform destroy
}

output "kubernetes_cluster_name" {
  value = google_container_cluster.ai_cluster.name
}

output "kubernetes_cluster_location" {
  value = google_container_cluster.ai_cluster.location
}
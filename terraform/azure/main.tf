terraform {
  required_version = ">= 1.0"
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }

  backend "azurerm" {
    resource_group_name  = "hotelbooking-tfstate-rg"
    storage_account_name = "hotelazuretstate"
    container_name       = "tfstate"
    key                  = "devops-tools.terraform.tfstate"
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "devops_rg" {
  name     = "hotel-devops-rg"
  location = "Central India"
}

resource "azurerm_kubernetes_cluster" "devops_aks" {
  name                = "hotel-devops-aks"
  location            = azurerm_resource_group.devops_rg.location
  resource_group_name = azurerm_resource_group.devops_rg.name
  dns_prefix          = "hotel-devops-aks"
  oidc_issuer_enabled = true

  default_node_pool {
    name       = "devopspool"
    node_count = 2
    vm_size    = "Standard_D2s_v5" # <-- Reverted to your known-working VM size
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "DevOps"
    Project     = "HotelBooking"
  }
}

output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.devops_aks.name
  description = "The name of the Azure AKS Cluster"
}

output "resource_group_name" {
  value       = azurerm_resource_group.devops_rg.name
  description = "Azure Resource Group Name"
}
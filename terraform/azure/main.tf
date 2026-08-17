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

# 1. Resource Group
resource "azurerm_resource_group" "devops_rg" {
  name     = "hotel-devops-rg"
  location = "Central India"
}

# 2. Azure Kubernetes Service (AKS) for Hotel DevOps Tools
resource "azurerm_kubernetes_cluster" "devops_aks" {
  name                = "hotel-devops-aks"
  location            = azurerm_resource_group.devops_rg.location
  resource_group_name = azurerm_resource_group.devops_rg.name
  dns_prefix          = "hotel-devops-aks"

  default_node_pool {
    name       = "devopspool"
    node_count = 2
    vm_size    = "Standard_D2s_v3"
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "DevOps"
    Project     = "HotelBooking"
  }
}

# --- OUTPUTS ---
output "kubernetes_cluster_name" {
  value       = azurerm_kubernetes_cluster.devops_aks.name
  description = "The name of the Azure AKS Cluster for Hotel Booking"
}

output "resource_group_name" {
  value       = azurerm_resource_group.devops_rg.name
  description = "Azure Resource Group Name"
}
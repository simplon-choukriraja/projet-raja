  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "3.41"
    }
  }
}

provider "azurerm" {
  features {}
}

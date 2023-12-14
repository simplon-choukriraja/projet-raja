# creation resource groupe

resource "azurerm_resource_group" "projet" {
  name = var.resource_group_name
  location = var.location
}


## Create aks cluster

resource "azurerm_kubernetes_cluster" "aks_cluster" {
  name                = var.aks_cluster
  location            = var.location
  resource_group_name = azurerm_resource_group.projet.name
  dns_prefix          = var.aks_cluster
  
   
  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_DS2_v2"  
  }

  identity { 
    type = "SystemAssigned" 
  }
}

## Create a virtual network within the resource group

resource "azurerm_virtual_network" "vnet" {
  name                = var.vnet_j
  resource_group_name = azurerm_resource_group.projet.name
  location            = var.location
  address_space       = ["10.1.0.0/16"]
}

# Create subnet VM

resource "azurerm_subnet" "subnet_vm" {
  name                 = var.subnet_j
  resource_group_name  = azurerm_resource_group.projet.name
  virtual_network_name = azurerm_virtual_network.vnet.name
  address_prefixes     = ["10.1.1.0/24"]
}

## Create VM network interface

resource "azurerm_network_interface" "vm" {
  name                = var.vm-projet
  location            = var.location
  resource_group_name = azurerm_resource_group.projet.name

  ip_configuration {
    name                          = var.config_vm
    subnet_id                     = azurerm_subnet.subnet_vm.id
    private_ip_address_allocation = "Static"
    private_ip_address            = "10.1.1.10"
    public_ip_address_id          = azurerm_public_ip.ip-pub.id
  }
}

# Azure public ip

resource "azurerm_public_ip" "ip-pub" {
  name                    = "pubip-rj"
  location                = var.location
  resource_group_name     = azurerm_resource_group.projet.name
  allocation_method       = "Static"
  idle_timeout_in_minutes = 30
  
}

# Create VM

resource "azurerm_linux_virtual_machine" "vm" {
  name                = var.vm_jenkins
  resource_group_name = azurerm_resource_group.projet.name
  location            = var.location
  size                = "Standard_A1_v2"
  network_interface_ids = [azurerm_network_interface.vm.id]

  admin_ssh_key {
    username   = var.admin
    public_key = "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCzxOdvEDVyYLtK5k4+EssfzqqhvtdsNTd5rHZ5nLQI0RIakUBEJSEzLzQPzh0+93RgwqoTJBXZP10GsAIsf2isT63Uc+gmCNVbhvYh78PI8Xgk2vA6UtzlAJX+B4PQIPgHYcc69i4CGeXP+KIPK0BNPuLKo3GU7/I4NqftYiZaSmpVUSgCJR5GfBFprBmInm/9h/Zkx073A0D65ETnjhMXPecvVlXFru8DUaFY9278zxrv+2aXRg4tI4nrV2V+SX4ke+O2hadBXJbOMlYol/AoyviH20dib8Du8gkbeUHDM0cD+UbCk1jxc/8sLdJRqaIwklcIEwBE96WQ424EUoulT9MeFYTLuPV3BmZ35TFZL/7+CjvH62Rd60UgW1UCfdnIBRgLdzkQ6gwxWXjVJwDOJEBlub3V9ABSCTLwPMhwlTc+QBByGIlFLPc9LeLqdaztvOmDPRlZXyAoKLdLsv3YiVgkTv1lJf4HpDeEE7/BXBnCnH/YCgaNsvq4YusYzhM= raja@MacBook-Air-de-Raja-Choukri.local"
    }

  os_disk {
    name                 = var.OSdisk_name
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
  }

  source_image_reference {
    publisher = "Debian"
    offer     = "debian-11"
    sku       = "11"
    version   = "latest"
  }
  
  computer_name                   = var.rajavm
  disable_password_authentication = true
  admin_username                  = var.admin
}

## Create NSG

resource "azurerm_network_security_group" "vm" {
  name                = var.NSG
  location            = var.location
  resource_group_name = azurerm_resource_group.projet-raja.name

  security_rule {
    name                       = var.VM_rule
    priority                   = 100
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_ranges    = ["22"]
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }

  security_rule {
    name                       = var.VM_rule2
    priority                   = 101
    direction                  = "Inbound"
    access                     = "Allow"
    protocol                   = "Tcp"
    source_port_range          = "*"
    destination_port_ranges    = ["8080"]
    source_address_prefix      = "*"
    destination_address_prefix = "*"
  }



  tags = {
    environment = "Production"
  }
}

resource "azurerm_subnet_network_security_group_association" "vm" {
  subnet_id                 = azurerm_subnet.subnet_vm.id
  network_security_group_id = azurerm_network_security_group.vm.id
}

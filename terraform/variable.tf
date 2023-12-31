variable "resource_group_name" {
  default = "projet"
} 

variable "location" {
  default = "westeurope"
}

variable "vnet_j" {
  default = "vnet_j"
}

variable "subnet_j" {
  default = "subnet_j"
}


variable "aks_cluster" {
  default = "Akscluster-raja"
}

variable "ip_j" {
  default = "ip_j"
}

variable "vm" {
    default = "vm_jenkins"
}


variable "OSdisk_name" {
    default = "OSdisk"
}

variable "vm-projet" {
    default = "vm-projet"
}

variable "config_vm" {
    default = "config_vm"
}

variable "admin" {
    default = "raja"
}

variable "vm_jenkins" {
    default = "vm_jenkins"
}

variable "rajavm" {
    default = "rajavm"
}

variable "NSG" {
    default = "NSG"
}


variable "VM_rule" {
    default = "SSH"

}

variable "VM_rule2" {
    default = "HTTP"

}

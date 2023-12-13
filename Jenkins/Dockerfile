## Installation du jenkins
FROM jenkins/jenkins:lts
USER root 
ENV DEBIAN_FRONTEND=noninteractive
RUN apt update -y && \  
    apt install -y jq parallel docker git

## INSTALLATION AZURE CLI 

RUN curl -sL https://aka.ms/InstallAzureCLIDeb | bash

## INSTALLATION TERRAFORM

RUN apt-get update \
    && apt-get install -y gnupg software-properties-common wget

RUN wget -qO - https://apt.releases.hashicorp.com/gpg | gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg

RUN echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | tee /etc/apt/sources.list.d/hashicorp.list

RUN apt-get update -y \
    && apt-get install -y terraform

RUN apt-get update && apt-get install -y apt-utils















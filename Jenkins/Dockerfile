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

## INSTALLATION KUBERNETES


RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    curl -LO "https://dl.k8s.io/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256" && \
    echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

## INSTALLER HELM

RUN curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | tee /usr/share/keyrings/helm.gpg > /dev/null &&  \
    apt-get install apt-transport-https --yes &&  \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" |  tee /etc/apt/sources.list.d/helm-stable-debian.list &&  \
    apt-get update &&  \
    apt-get install helm 
















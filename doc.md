# ***PROJET***

## **TERRAFORM**

- Deployer l'infrastructure cloud aver terraform.
Pou faire ca il faut creer les fichier suivants: 

* *main.tf*
* *variable.tf*
* *providers.tf*

- Ajouter la partie de AKS sur terraform pour eviter de deployer 2 fois le groupe de ressource.
La partie que il faut ajouter sur terraform pour avoir AKS c'est la suivant: 

```consol

resource "azurerm_kubernetes_cluster" "aks_cluster" {
  name                = var.cluster_name
  location            = var.location
  resource_group_name = azurerm_resource_group.projet-raja.name
  dns_prefix          = var.dns_prefix
  
   
  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "Standard_DS2_v2"  
  }

  identity { 
    type = "SystemAssigned" 
  }
}
```

Pour lancer terraform -> **terraform init**
pour lire le plan du terraform -> **terraform plan**
pour lancer la creation dell'infrastrucrure -> **terraform apply**

La commande *plan* est utilisée avec Terraform, un outil d'infrastructure en tant que code (IaC) utilisé pour définir, créer et gérer l'infrastructure de manière déclarative. -> **terraform apply**

Le commande terraform *apply* dans Terraform est utilisée pour appliquer les modifications définies dans le code d'infrastructure aux fournisseurs de cloud ou aux systèmes sur site. Après avoir exécuté terraform plan et évalué les modifications proposées. -> **terraform apply**

La commande *destroy* est utilisée avec Terraform pour supprimer complètement les ressources gérées par l'infrastructure spécifiées dans votre code Terraform. Cette commande est particulièrement utile lorsque vous souhaitez mettre hors service ou supprimer une infrastructure existante. -> terraform destroy**

## **INSTALLER AZURE CLI SUR LA MV**

Après avoir créé l'infrastructure cloud avec *Terraform*, on se dirige vers *AZURE* et récupère les *RDP* pour pouvoir accéder au *MV*. 

RDP -> ssh rj@104.45.5.199

- Sur la MV vituel j'installe *GIT*: 

1. Obtenez les packages nécessaires pour le processus d’installation 

```consol
sudo apt-get update
sudo apt-get install ca-certificates curl apt-transport-https lsb-release gnupg
```
2. Téléchargez et installez la clé de signature Microsoft :

```consol
sudo mkdir -p /etc/apt/keyrings
curl -sLS https://packages.microsoft.com/keys/microsoft.asc |
    gpg --dearmor |
    sudo tee /etc/apt/keyrings/microsoft.gpg > /dev/null
sudo chmod go+r /etc/apt/keyrings/microsoft.gpg
```

3. Ajoutez le référentiel de logiciels Azure CLI :

```consol
AZ_REPO=$(lsb_release -cs)
echo "deb [arch=`dpkg --print-architecture` signed-by=/etc/apt/keyrings/microsoft.gpg] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" |
sudo tee /etc/apt/sources.list.d/azure-cli.list
```

4. Mettez à jour les informations concernant le référentiel, puis installez le package azure-cli:

```consol 
sudo apt-get update
sudo apt-get install azure-cli

```
## **INSTALLER GIT SUR LA MV**

```consol
sudo apt-add-repository ppa:git-core/ppa
sudo apt-get update
sudo apt-get install git
```

## **INSTALLER DOCKER SUR LA MV**

1. Installer les dépendances de Docker:

```consol
*sudo apt-get update*
```
2. exécutez la commande ci-dessous pour installer les paquets:

```cosol
*sudo apt-get install apt-transport-https ca-certificates curl gnupg2 software-properties-common*
```
3. Ajouter le dépôt officiel Docker

```consol 
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
```

4.  *Ajoute le dépôt Docker à la liste des sources de notre machine:*

```consol 
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable"
```

5. *Mettre à jour le cache des paquets pour prendre en compte les paquets de ce nouveau dépôt :*

```consol
sudo apt-get update
```

6. *Installation des paquets Docker:*
```consol
sudo apt install docker-ce
```

- Stop container -> *sudo docker stop <ID CONTAINER>*
- Start Docker-> *sudo systemctl start docker*
- Vous pouvez regarder le statut de Docker -> *sudo systemctl status docker*
- Lister le image container -> *sudo docker image ls -a*
- Lister le container Docker -> sudo *sudo docker container ls -s*

https://www.it-connect.fr/installation-pas-a-pas-de-docker-sur-debian-11/

## **INSTALLER DOCKER-COMPOSE SUR LA MV**

```consol
sudo curl -L "https://github.com/docker/compose/releases/download/1.26.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose --version
```
## **INSTALLER KUBERNETES SUR LA MV**

1. Mettre à jour le cache des paquets

```consol
sudo apt-get update 
```

2. Installer kubectl:

```consol
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
curl -LO "https://dl.k8s.io/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl.sha256"
echo "$(cat kubectl.sha256)  kubectl" | sha256sum --check
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
```

- Aprés avoir creer le cluster avec **Terraform** sur la mv j'ai installer jenkins et le deployment kubernetes pour faire on sorte que mes manifest soint pris en compte et deployer il faut faire la commande suivant: 

```consol
az aks get-credentials --name myakscluster --resource-group projet-raja
```

4. **Creation un database on Azure pour le server Mysql** -> az mysql server create --resource-group projet-raja --name mymsqlserver  --location westeurope --admin-user myadmin --sku-name GP_Gen5_2 --version 8.0  --ssl-enforcement disabled --tags AppProfile:WordPress

5. **Creation ACR-Azure Container Registry** -> az acr create --resource-group projet-raja --name projetacr --sku basic 

6. **Deployer image WordPress sur Docker** -> sudo docker pull wordpress:6.1.1

7. **Deployer container WordPress sur Docker** -> sudo docker create --name wordpress -p 80:80 wordpress:6.1.1

9. **Deployer image MYSQL sur Docker** -> sudo docker pull mysql:8

10. **Deployer container MYSQL sur Docker** sudo docker create --name mysql -p 5000:5000 mysql:8

```consol
sudo docker create --name mysql -p 3306:3306 mysql:8
```

![](https://hackmd.io/_uploads/SJX9l7iAh.png)

8. **Deployer le container WordPress sur Docker**

```consol
sudo docker create --name wordpress -p 80:80 wordpress:6.1.1
```

![](https://hackmd.io/_uploads/HJDNQQjRn.png)


### ***INSTALLER TERRAFORM **
## ***BUILD WORDPRESS AVEC KUBERNETES***

1. *INSTALLER HELM*

```consol
curl https://baltocdn.com/helm/signing.asc | gpg --dearmor | sudo tee /usr/share/keyrings/helm.gpg > /dev/null
sudo apt-get install apt-transport-https --yes
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/helm.gpg] https://baltocdn.com/helm/stable/debian/ all main" | sudo tee /etc/apt/sources.list.d/helm-stable-debian.list
sudo apt-get update
sudo apt-get install helm
```

2. *AJOUTER LE RÉFÉRENTIEL HELM DE TRAEFIK AUX REPOSITORIES*

```consol
helm repo add traefik https://helm.traefik.io/traefik
helm repo update
```

3. *DÉPLOYER TRAEFIK AVEC HELM*

```consol
helm install traefik traefik/traefik
```

- **Creer un namespace**
Pour lancer la creation du namespace: *kubectl create namespace*
- **Creer le persistentvolume**
Pour lancer la creation du persistentvolume.yml *kubectl create -f persistentvolume.yml -n wordpress*
- **Creer le volume-claim** 
Pour lancer la creation du volume-claim *kubectl create -f volume-claim.yml -n wordpress*
- **Creer un secret.**
Pour lancer la creation d'un secret *kubectl create -f secret.yml -n wordpress*
- **Creer un deployment wordpress**
Pour lancer le deployment du wordpress *kubectl create -f deployment-wp.yml -n wordpress*
- **Creer un deployment mysql**
Pour lancer le deployment du wordpress *kubectl create -f deployment-mysql.yml -n wordpress*
- **Creer le service mysql**
Pour lancer le deploiement *kubectl create -f service-mysql.yml -n wordpress*
- **Creer le service wordpress**
Pour lancer le deploiement *kubectl create -f service-wp.yml -n wordpress*

J'ai afficher le site wordpress sur mon *DNS* -> **wordpress.raja-ch.me**
<img width="489" alt="Capture d’écran 2023-11-18 à 17 49 43" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/7a1d13cd-f74b-41bc-89d6-0567d6e38140">

Pour rendre le logiciel WordPress visible sur votre DNS, vous devez disposer d'un DNS au cas où vous pourriez l'acquérir une fois acquis deux fois et configuré.

Pour configurer votre DNS avec Gandi :
<img width="314" alt="Capture d’écran 2023-11-19 à 13 14 02" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/b01341a3-317f-41e4-9d48-0a525fcfa8ca">

## **Mettre en place une authentification BasicAuth HTTP avec mot de passe local dans la config de Traefik**

Pour pouvoir faire cela il faut créer le fichier middleware.yml dans lequel est également incluse la partie basicauth dans laquelle je vais crypter le mot de passe.

- **basicauth.yml**

```consol 
apiVersion: traefik.io/v1alpha1
kind: Middleware
metadata:
  name: auth
spec:
  basicAuth:
    removeHeader: false
    secret: authsecret

---
apiVersion: v1
kind: Secret
metadata:
  name: authsecret
data:
  users:
    dXNlcjokYXByMSQwdERsbjBKZyR4LnlyUk8ubVltdm1mNmxUNG9rNWExCgo=

```
Pour chiffrer mdp, j'ai exécuté la commande suivante :

```consol
htpasswd -nb user password | openssl base64 
htpasswd -nb raja rajach8 | openssl base64
```
- Sur la mv c'est on utilise cet commande pour genere le MDP on peu avoir un msg d'erreur car il connait pas du coup il faut la telecharge avec la commande suivant: 

```consol 
sudo apt-get update
sudo apt-get install apache2-utils

```
Enfin il faut aussi ajouter cet partie sur le fichier ingress.yml

<img width="616" alt="Capture d’écran 2023-11-19 à 15 18 52" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/a7428e5c-2cf8-4f9a-bbab-863bf223875b">

Après avoir créé le middleware.yml et mis à jour l'entrée, nous obtenons que lorsque nous essayons d'accéder à WordPress, il nous sera demandé de saisir les informations d'identification pour y accéder.

<img width="855" alt="Capture d’écran 2023-11-20 à 11 55 13" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/c26bbb24-8698-4927-9e3e-0db9f662ac18">

<img width="1100" alt="Capture d’écran 2023-11-20 à 11 49 31" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/d3a65a83-7b01-4916-b964-1e1d732b420a">

*NOM D'UTILISATEUR*: user
*MOT DE PASSE*: rajach

**## *METTRE EN PLACE UN CERTIFICAT TLS SUR TRAEFIK LIÉ AU DOMAINE AVEC REDIRECTION EN HTTPS

Mettre en place un certificat TLS sur Traefik lié au domaine avec redirection en https*

1. *Installer le cert-manager*

- Installation de cert manager: kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.1/cert-manager.yaml

- Creer le fichier cert-manager.yml

```consol 
apiVersion: cert-manager.io/v1
kind: Issuer
metadata:
 name: cert-manager
spec:
 acme:
   server: https://acme-v02.api.letsencrypt.org/directory
   privateKeySecretRef:
     name: cert-manager-account-key
   solvers:
     - http01:
         ingress:
           class: traefik
```

- Faut ajouter la partie du cert manager sur ingress: 

Annotation: *cert-manager.io/issuer: cert-manager***

Il faut creer le fichier tls.yml 

```consol 

apiVersion: traefik.containo.us/v1alpha1
kind: Middleware
metadata:
  name: redirect
  namespace: wordpress
spec:
  redirectScheme:
    scheme: https
    permanent: true
```
Aprés il faut ajouter dans l'annotation sur l'ingress.

 <img width="960" alt="Capture d’écran 2023-11-24 à 11 28 59" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/ed18ee97-871c-4a3c-9b70-6beef4d93379">

- https://wordpress.raja-ch.me/wp-admin/install.php


## *DEPLOYER JENKINS*

- **Creer un Dockerfile**

```conosl 
FROM jenkins/jenkins:lts
USER root 
ENV DEBIAN_FRONTEND=noninteractive
RUN apt update -y && \  
    apt install -y jq parallel  && \
    curl -sSL https://get.docker.com/ | sh
```
- **docker-compose.yml**

```consol
version: '3.8'
services:
  jenkins:
    image : jenkins
    privileged: true
    user: root
    ports:
     - 8080:8080
     - 50000:50000
    container_name: jenkins-projet
    volumes:
      - ./jenkins_configuration:/var/jenkins_home
      - /usr/bin/kubectl:/usr/bin/kubectl
      - /var/run/docker.sock:/var/run/docker.sock
```
Lancert la commande suivantes pour faire marcher jenkins: 

- *sudo docker-compose up -d --build*
- *sudo docker-compose up -d*

- Pour acceder à Jenkins -> * http://104.45.5.199:8080*

- MDP ->> *sudo docker logs f35bde67441b*

*PROBLEM* 
Je deployer jenkins avec Dockerfile et docker-compose.yml mais j'ai rencontre un problem jenkins il marche pas, j'ai les information avec le logs que jenkins tourne par contre j'arrive pas a le faire afficher sur le host. 
J'ai esseyer de preciser l'adresse IP sur le manifest docker-compose.yml mais je continue toujours avoir le même problem.

- In questo caso bisogna sopranominare la parte 'none' che si genera con dockerfile in jenkins e prendere l'ultima che si é sviluppata e poi utilizzare la commande *--no-cache*. In caso il problema non si é risolto bisogna guardare su Azure e gardare nella parte subnet la voce *nsg* e cercare di guardare se le regole sono ben definiti in caso non fosse cosi aggiungere la regola per la porte 8080, per fare in modo che jenkins funzioni sulla porta. 

Successivamente dopo aver creato jenkins l'obiettivo seguente e quello di automatizzare i manifesti yml tramite kustomize et aggiungerli nella parte della pipiline


*SOLUTION*

Da aggiungere la parte per risolvere il problema

## ***INSTALLER JAVA***

```consol
sudo apt-get install openjdk-11-jdk
```

# *SUPERVISER DES CONTAINERS*

1. *Installation de Prometheus et Grafana via Helm*

- **Installation de Helm**

```consol 
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

- **Ajout du repository pour Prometheus et Grafana, et mise à jour**

```consol
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update
```
- **Installation d'un Helm Chart dans un namespace monitoring**

```consol 
helm install prometheus \
  prometheus-community/kube-prometheus-stack \
  --namespace projet-monitoring \
  --create-namespace
```

<img width="930" alt="Capture d’écran 2023-11-25 à 15 48 39" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/11cb683a-9ccd-4c44-96dc-ac56e4dc70ef">

- Pour verifié que le namespace est été bien creer il faut utiliser la commande suivant: 

```consol 
kubectl get all -n monitoring
```

<img width="930" alt="Capture d’écran 2023-11-25 à 15 40 00" src="https://github.com/simplon-choukriraja/projet-raja/assets/108053084/2adff6d6-6829-4a48-ba8d-30b8304d149c">











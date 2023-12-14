
pipeline {
    agent any 

    environment {
        AZURE_SUBSCRIPTION = 'a1f74e2d-ec58-4f9a-a112-088e3469febb'
        AZURE_TENANT_ID = 'a2e466aa-4f86-4545-b5b8-97da7c8febf3' 
        AZURE_CLIENT_ID = '7ac0b3c5-acf9-4398-afdf-8d77fe0aaaaa'
        AZURE_CLIENT_SECRET = 'JID8Q~JaaVpUZ9fcvovJAny263zoFtccGeva0aTw'
    }
    
    stages{
        stage('Azure Login') {
            steps {
                script {
                // Eseguire l'autenticazione ad Azure utilizzando le credenziali di servizio
                sh 'az login --service-principal -u 7ac0b3c5-acf9-4398-afdf-8d77fe0aaaaa -p JID8Q~JaaVpUZ9fcvovJAny263zoFtccGeva0aTw --tenant a2e466aa-4f86-4545-b5b8-97da7c8febf3'
                    
                }
            }
        }

        
        stage('Run Terraform Commands') {
            steps {
                script {
                    dir('terraform') {
                        sh 'terraform init'
                        sh 'terraform apply -auto-approve'
                    }
                 }
             }
        }

        stage('Add az get-credentials Kubernetes') {
            steps {
                script {
                    dir('kubernetes') {
                      sh 'az login'
                      sh 'az aks get-credentials --name myakscluster --resource-group projet-rj'
                    }
                 }
             }
        }
        
        stage('Traefik avec Helm') {
            steps {
                script {
                    dir('kubernetes') {
                      // AJOUTER LE RÉFÉRENTIEL HELM DE TRAEFIK AUX REPOSITORIES  
                      sh 'helm repo add traefik https://helm.traefik.io/traefik'
                      sh 'helm repo update'
                      // DÉPLOYER TRAEFIK AVEC HELM
                      sh 'helm install traefik traefik/traefik'
                    }
                }
            }
        }
        
        stage('Deploy App Wordpress end MariaDB with k8s') {
            steps {
                script {
                    dir('kubernetes') {
                      sh 'kubectl create -f namespace.yml'  
                      ssh 'kubectl apply -f deployment.wp.yml' 
                      ssh 'kubectl apply -f deployment.mysql.yml'
                      ssh 'kubectl apply -f ingress.yml'
                      ssh 'kubectl apply -f service-mysql.yml'
                      ssh 'kubectl apply -f middleware.yml'
                      ssh 'kubectl apply -f pv.yml'
                      ssh 'kubectl apply -f secret-wp.yml'
                      ssh 'kubectl apply -f secret-mysql.yml'
                      ssh 'kubectl apply -f service-wp.yml'
                      ssh 'kubectl apply -f storageclass.yml'  
                      ssh 'kubectl apply -f basicauth.yml'
                      ssh 'kubectl apply -f cert-manager.yml'
                          
                    }
                 }
            }
        }               
     }    
     post {
        always {
            // Nettoyage de l'espace de travail Jenkins
            step([$class: 'WsCleanup'])
        }
    }
}


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

        
        //stage('Run Terraform Commands') {
            //steps {
                //script {
                    //dir('terraform') {
                        //sh 'terraform init'
                        //sh 'terraform apply -auto-approve'
                        
                    //}
                 //}
             //}
        //}

        //stage('Add az get-credentials Kubernetes') {
            //steps {
                //script {
                    //dir('kubernetes') {
                      //sh 'az aks get-credentials --name myakscluster --resource-group projet'
                    //}
                 //}
             //}
        //}
        
        stage('Traefik avec Helm') {
            steps {
                script {
                    dir('kubernetes') {
                       //AJOUTER LE RÉFÉRENTIEL HELM DE TRAEFIK AUX REPOSITORIES  
                      sh 'helm repo add traefik https://helm.traefik.io/traefik'
                      sh 'helm repo update'
                       //DÉPLOYER TRAEFIK AVEC HELM
                      sh 'helm upgrade --install traefik traefik/traefik'
                    }
                }
            }
        }
        
        stage('Deploy App Wordpress end MariaDB with k8s') {
            steps {
                script {
                    dir('kubernetes') {
                      sh 'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.1/cert-manager.yaml'
                      sh 'kubectl create namespace wordpress'  
                      sh 'kubectl apply -f deployment-wp.yml' 
                      sh 'kubectl apply -f deployment-mysql.yml'
                      sh 'kubectl apply -f ingress.yml'
                      sh 'kubectl apply -f service-mysql.yml'
                      sh 'kubectl apply -f middleware.yml'
                      sh 'kubectl apply -f pvc.yml'
                      sh 'kubectl apply -f secret-mysql.yml'
                      sh 'kubectl apply -f service-wp.yml'
                      sh 'kubectl apply -f storageclass.yml'  
                      sh 'kubectl apply -f basicauth.yml'
                      sh 'kubectl apply -f cert-manager.yml'
                          
                    }
                 }
            }
        }
        
        stage ('Installation de Prometheus et Grafana via Helm') {
            steps {
                script {
                    //Installation de Prometheus et Grafana via Helm
                    sh ('''
                    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                    chmod 700 get_helm.sh
                    ./get_helm.sh
                    ''')
                        //Ajout du repository pour Prometheus et Grafana, et mise à jour
                        sh ('''
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
                        ''')
                            //Installation d'un Helm Chart dans un namespace monitoring
                            sh ('''
                            helm install prometheus \
                            prometheus-community/kube-prometheus-stack \
                            --namespace projet-monitoring \
                            --create-namespace  
                            ''')
                            }
                        }
                }
        
        stage ('Loki') {
            steps {
                script {
                    //Installation Loki
                    sh ('''
                    helm repo add grafana https://grafana.github.io/helm-charts
                    helm repo update
                    helm upgrade --install promtail --namespace projet-monitoring grafana/promtail
                    helm upgrade --install loki --namespace projet-monitoring grafana/loki-stack
                    ''')
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

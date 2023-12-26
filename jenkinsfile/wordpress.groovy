pipeline {
    agent any 
    
    environment {
        
        MYSQL_ROOT_PASSWORD = credentials('password')
        GANDI_API_KEY = credentials('API_KEY')
        TRAEFIK_IP = 'traefikIP'
        
    }
    
    stages{
        
        stage('Clean Workspace') {
            steps {
                //This step deletes the entire workspace
                deleteDir()
            }
        }
        
        stage('Azure Login') {
            steps {
                script {
                // Eseguire l'autenticazione ad Azure utilizzando le credenziali di servizio
                      withCredentials([azureServicePrincipal(credentialsId: 'AzureServicePrincipal')]) {
                        sh 'az login --service-principal -u $AZURE_CLIENT_ID -p $AZURE_CLIENT_SECRET -t $AZURE_TENANT_ID'
                      
                    }
                }
            }
        }

        stage('Clone Repository') {
            steps {
                script {
                    sh 'git clone https://github.com/simplon-choukriraja/projet-raja.git'
                }
            }
        }

        stage('Run Terraform Commands') {
            steps {
                script {
                    dir('projet-raja/terraform') {
                        sh 'terraform init'
                        sh 'terraform apply -auto-approve'
                     }
                 }
             }
        }

        stage('Configuration of Azure Credentials for Kubernetes Cluster Access') {
            steps {
                script {
                    dir('projet-raja/kubernetes') {
                    sh 'az aks get-credentials --name Akscluster-raja --resource-group projet --overwrite-existing'
                    }
                 }
             }
        }
        
        stage('Implementation of Traefik Using Helm') {
            steps {
                script {
                    dir('projet-raja/kubernetes') {
                       //AJOUTER LE RÉFÉRENTIEL HELM DE TRAEFIK AUX REPOSITORIES  
                      sh 'helm repo add traefik https://helm.traefik.io/traefik'
                      sh 'helm repo update'
                       //DÉPLOYER TRAEFIK AVEC HELM
                      sh 'helm upgrade --install traefik traefik/traefik'
                    }
                }
            }
        }
        
        stage('Deploy App Wordpress and MySQL with Kubernetes') {
            steps {
                script {
                   withCredentials([string(credentialsId: 'password', variable: 'MYSQL_ROOT_PASSWORD')]) { 
                     dir('projet-raja/kubernetes') { 
                            sh 'kubectl create namespace wordpress'  
                            sh 'kubectl apply -f deployment-wp.yml'
                            sh 'kubectl apply -f deployment-mysql.yml'
                            sh "sed -i 's/MYSQL_ROOT_PASSWORD: passwordmysql/MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}/' secret-mysql.yml"
                            sh 'kubectl apply -f secret-mysql.yml' 
                            sh 'kubectl apply -f ingress.yml'
                            sh 'kubectl apply -f service-mysql.yml'
                            sh 'kubectl apply -f pvc.yml'  
                            sh 'kubectl apply -f service-wp.yml'
                            sh 'kubectl apply -f storageclass.yml'
                            sh 'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.1/cert-manager.yaml'
                            sh 'kubectl apply -f middleware.yml'
                            sh 'sleep 120'
                            sh 'kubectl apply -f cert-manager.yml'
                          
                       } 
                    }
                }
            }
        }
 

        stage('Recover IP Traefik') {
            steps {
                    script {
                         def TRAEFIK_IP = sh(script: "kubectl get svc traefik -n default -o jsonpath='{.status.loadBalancer.ingress[0].ip}'", returnStdout: true).trim()
                         echo "L'indirizzo IP di Traefik è: ${TRAEFIK_IP}"
                        
                        // Salva l'IP in un file temporaneo per utilizzarlo nello stage successivo
                         writeFile file: 'traefik_ip.txt', text: TRAEFIK_IP
                        
                }
            }
        } 

        stage('Mettre à jour l enregistrement DNS sur Gandi') {
            steps {
                script {
                    def TRAEFIK_IP= readFile('traefik_ip.txt').trim()
                    withCredentials([string(credentialsId: 'API_KEY', variable: 'GANDI_API_KEY')]) {
                            def apiUrl = 'https://api.gandi.net/v5/livedns/domains/raja-ch.me/records/wordpress/A'
                            def response = sh(
                                script: """
                                    echo "URL: ${apiUrl}"
                                    curl -X PUT -H "Content-Type: application/json" \\
                                    -H "Authorization: Apikey ${GANDI_API_KEY}" \\
                                    -d '{"rrset_values": ["${TRAEFIK_IP}"]}' \\
                                    "${apiUrl}"
                                """,
                                returnStatus: true
                            )
                            if (response == 0) {
                                //echo "DNS record updated successfully"
                            } else {
                                //error "Failed to update DNS record on Gandi"
                            }
                    
                    }    
                }
            }
        }
        stage('Rinnova Certificato TLS') {
            steps {
                script {
                    dir('projet-raja/kubernetes') { 
                         Assicurati che il nome del file cert-manager.yml e il nome del certificato siano corretti
                         //Cancella il certificato esistente
                        sh 'kubectl delete -f cert-manager.yml -n wordpress'
                        // Aspetta un po' per dare tempo a cert-manager di rilevare la cancellazione
                        sleep(30)

                        // Ricrea la risorsa Certificate
                        sh 'kubectl apply -f cert-manager.yml -n wordpress'

                        // Aspetta per il rinnovo/ricreazione del certificato
                        sleep(120)

                        // Verifica lo stato del nuovo certificato
                        sh 'kubectl get certificates -n wordpress'
                        sh 'kubectl describe certificate tls-cert-ingress-http -n wordpress'

                        
                    }
                }
            }
        }

        
        stage ('Installation of Prometheus and Grafana via Helm') {
            steps {
                script {
                  dir('raja-projet/monitoring') { 
                    //Installation of Helm
                    sh ('''
                    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                    chmod 700 get_helm.sh
                    ./get_helm.sh
                    ''')
                        // Adding the repository for Prometheus and Grafana, and updating
                        sh ('''
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
                        ''')
                            // Installation of a Helm Chart in a namespace projet-monitoring
                            sh ('''
                            helm install prometheus \
                            prometheus-community/kube-prometheus-stack \
                            --namespace projet-monitoring \
                            --create-namespace  
                            ''')
                      
                   }
                }
            } 
        }    
     

        stage ('Installation of Prometheus and Grafana with kubernetes') {
            steps {
                script {
                  dir('raja-projet/monitoring') { 
                      sh 'kubectl apply -f grafana.yml -n projet-monitoring'
                      sh 'kubectl apply -f prometheus.yml -n projet-monitoring'
                      sh 'kubectl apply -f service-grafana.yml -n projet-monitoring'
                  }
                }
            }
        }
    }                   
}


pipeline {
    agent any 
    
    environment {
        NAMESPACE = 'wordpress'
        SERVICE_NAME = 'wordpress-service'
        DNS_ZONE = 'raja-ch.me'
        DNS_RECORD = 'www'
        
    }
    
    stages{
        stage('Clean Workspace') {
            steps {
                // This step deletes the entire workspace
                deleteDir()
            }
        }
        stage('Azure Login') {
            steps {
                script {
                // Eseguire l'autenticazione ad Azure utilizzando le credenziali di servizio
                    sh 'az login --service-principal -u c6608247-3dee-4555-b0ae-f275f6ce38f0 -p fns8Q~PpgPKKTxEeR3FUqfPbu3h-0OsZjXiROc_K --tenant a2e466aa-4f86-4545-b5b8-97da7c8febf3' 
                    
                }
            }
        }

        //stage('Clone Repository') {
            //steps {
                //script {
                    //sh 'git clone https://github.com/simplon-choukriraja/projet-raja.git'
                //}
            //}
        //}

        //stage('Run Terraform Commands') {
            //steps {
                //script {
                    //dir('projet-raja/terraform') {
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
        
        //stage('Traefik avec Helm') {
            //steps {
                //script {
                    //dir('projet-raja/kubernetes') {
                       //AJOUTER LE RÉFÉRENTIEL HELM DE TRAEFIK AUX REPOSITORIES  
                      //sh 'helm repo add traefik https://helm.traefik.io/traefik'
                      //sh 'helm repo update'
                       //DÉPLOYER TRAEFIK AVEC HELM
                      //sh 'helm upgrade --install traefik traefik/traefik'
                    //}
                //}
            //}
        //}
        
        stage('Deploy App Wordpress end MariaDB with k8s') {
            steps {
                script {
                    dir('kubernetes') {
                      sh 'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.1/cert-manager.yaml'
                      sh 'kubectl create namespace wordpress'  
                      sh 'ls'
                      sh 'kubectl apply -f deployment-mysql.yml'
                      sh 'kubectl apply -f ingress.yml'
                      sh 'kubectl apply -f service-mysql.yml'
                      sh 'kubectl apply -f middleware.yml'
                      sh 'kubectl apply -f pvc.yml'
                      sh 'kubectl apply -f basicauth.yml'
                      sh 'kubectl apply -f secret-mysql.yml'
                      sh 'kubectl apply -f service-wp.yml'
                      sh 'kubectl apply -f storageclass.yml'  
                      sh 'kubectl apply -f cert-manager.yml'
                          
                    }
                 }
            }
        }

    
        stage('Recover IP Traefik') {
            steps {
                script {
                    // Esegue il comando kubectl per ottenere l'indirizzo IP del LoadBalancer
                    def traffikIP = sh(script: "kubectl get svc ${SERVICE_NAME} -n ${NAMESPACE} -o jsonpath='{.status.loadBalancer.ingress[0].ip}'", returnStdout: true).trim()
                    echo "L'indirizzo IP di Traefik è: ${traffikIP}"
                }
            }
        } 

        stage('Mettre à jour l enregistrement DNS sur Gandi') {
            steps {
                script {
                    // Utilizza l'API di Gandi per aggiornare il record DNS
                    sh ('''
                    curl -X PUT -H 'Content-Type: application/json' -H 'Authorization: Apikey ${GANDI_API_KEY}' \\
                    -d '{\"rrset_ttl\": 10800, \"rrset_values\": [\"${env.TRAFFIK_IP}\"]}' \\
                    https://api.gandi.net/v5/livedns/domains/${DNS_ZONE}/records/${DNS_RECORD}/A
                    ''')
                }
            }
        }

        stage('Auth-Secret') {
            steps {
                script {
                    // Accedi al cluster Kubernetes (assicurati che Jenkins abbia le credenziali appropriate)
                    // e utilizza kubectl per ottenere il valore del secret
                    def username = sh(script: "kubectl get secret ${SECRET_NAME} -n ${NAMESPACE} -o=jsonpath='{.data.username}' | base64 --decode", returnStdout: true).trim()
                    def password = sh(script: "kubectl get secret ${SECRET_NAME} -n ${NAMESPACE} -o=jsonpath='{.data.password}' | base64 --decode", returnStdout: true).trim()
                    sh 'curl -k ${username}:${password} https://wordpress.raja-ch.me'
                }
            }
        }
        stage ('Installation de Prometheus et Grafana via Helm') {
            steps {
                script {
                    // Installation de Prometheus et Grafana via Helm
                    sh ('''
                    curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
                    chmod 700 get_helm.sh
                    ./get_helm.sh
                    ''')
                        // Ajout du repository pour Prometheus et Grafana, et mise à jour
                        sh ('''
                        helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                        helm repo update
                        ''')
                            // Installation d'un Helm Chart dans un namespace monitoring
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
                    // Installation Loki
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


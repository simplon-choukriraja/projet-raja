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
                // Perform Azure authentication using service credentials
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

        //stage('Configuration of Azure Credentials for Kubernetes Cluster Access') {
            //steps {
                //script {
                    //dir('projet-raja/kubernetes') {
                    //sh 'az aks get-credentials --name Akscluster-raja --resource-group projet --overwrite-existing'
                    //}
                 //}
             //}
        //}
        
        //stage('Implementation of Traefik Using Helm') {
            //steps {
                //script {
                    //dir('projet-raja/kubernetes') {
                       //Add the Traefik Helm repository to the repositories 
                      //sh 'helm repo add traefik https://helm.traefik.io/traefik'
                      //sh 'helm repo update'
                       //Deploy Traefik with Helm
                      //sh 'helm upgrade --install traefik traefik/traefik'
                    //}
                //}
            //}
        //}
        
        //stage('Deploy App Wordpress and MySQL with Kubernetes') {
            //steps {
                //script {
                   //withCredentials([string(credentialsId: 'password', variable: 'MYSQL_ROOT_PASSWORD')]) { 
                     //dir('projet-raja/kubernetes') { 
                            //sh 'kubectl create namespace wordpress'  
                            //sh 'kubectl apply -f deployment-wp.yml'
                            //sh 'kubectl apply -f deployment-mysql.yml'
                            //sh "sed -i 's/MYSQL_ROOT_PASSWORD: passwordmysql/MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}/' secret-mysql.yml"
                            //sh 'kubectl apply -f secret-mysql.yml' 
                            //sh 'kubectl apply -f ingress.yml'
                            //sh 'kubectl apply -f service-mysql.yml'
                            //sh 'kubectl apply -f pvc.yml'  
                            //sh 'kubectl apply -f service-wp.yml'
                            //sh 'kubectl apply -f storageclass.yml'
                            //sh 'kubectl apply -f https://github.com/cert-manager/cert-manager/releases/download/v1.10.1/cert-manager.yaml'
                            //sh 'kubectl apply -f middleware.yml'
                            //sh 'sleep 120'
                            //sh 'kubectl apply -f cert-manager.yml'
                       //} 
                    //}
                //}
            //}
        //}
 

        //stage('Recover IP Traefik') {
            //steps {
                //script {
                    //def TRAEFIK_IP = sh(script: "kubectl get svc traefik -n default -o jsonpath='{.status.loadBalancer.ingress[0].ip}'", returnStdout: true).trim()
                    //echo "L'indirizzo IP di Traefik è: ${TRAEFIK_IP}"
                        
                      //Save the IP address to a temporary file for use in the next stage
                    //writeFile file: 'traefik_ip.txt', text: TRAEFIK_IP
                        
                //}
            //}
        //} 

        //stage('Mettre à jour l enregistrement DNS sur Gandi') {
            //steps {
                //script {
                    //def TRAEFIK_IP= readFile('traefik_ip.txt').trim()
                    //withCredentials([string(credentialsId: 'API_KEY', variable: 'GANDI_API_KEY')]) {
                            //def apiUrl = 'https://api.gandi.net/v5/livedns/domains/raja-ch.me/records/wordpress/A'
                            //def response = sh(
                                //script: """
                                    //echo "URL: ${apiUrl}"
                                    //curl -X PUT -H "Content-Type: application/json" \\
                                    //-H "Authorization: Apikey ${GANDI_API_KEY}" \\
                                    //-d '{"rrset_values": ["${TRAEFIK_IP}"]}' \\
                                    //"${apiUrl}"
                                //""",
                                //returnStatus: true
                            //)
                            //if (response == 0) {
                                //echo "DNS record updated successfully"
                            //} else {
                                //error "Failed to update DNS record on Gandi"
                            //}
                    
                    //}    
                //}
            //}
        //}
        //stage('Renewal Process for TLS Certificate using Cert-Manager in Kubernetes') {
            //steps {
                //script {
                    //dir('projet-raja/kubernetes') { 
                         //Ensure that the cert-manager.yml file name and the certificate name are correct
                         //Delete the existing certificate
                        //sh 'kubectl delete -f cert-manager.yml -n wordpress'
                         //Wait a while to give cert-manager time to detect the deletion
                        //sleep(30)

                         //Recreate the Certificate resource 
                        //sh 'kubectl apply -f cert-manager.yml -n wordpress'

                        //Wait for the renewal/recreation of the certificate
                        //sleep(60)

                        //Check the status of the new certificate
                        //sh 'kubectl get certificates -n wordpress'
                        //sh 'kubectl describe certificate tls-cert-ingress-http -n wordpress'

                        
                    //}
                //}
            //}
        //}

        
        stage('Installation of Prometheus and Grafana via K8s') {
            steps {
                script {
                    dir('projet-raja/monitoring') { 
                        sh 'kubectl create -f grafana.yml'
                        sh 'kubectl create -f prometheus.yml'
                        sh 'kubectl create -f service-grafana.yml'
                        sh 'kubectl create -f service-prometheus.yml'
                        sh 'kubectl apply -f deployment-box.yml'
                                             
                    }
                }
            }
        } 
    } 
}


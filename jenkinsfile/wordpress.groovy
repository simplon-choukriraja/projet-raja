pipeline {
    agent any 

    environment {
        SHEBANG = '#!/usr/bin/env -S bash -Eeuxo pipefail'
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

        stage('Prepara Chiave SSH') {
            steps {
                script {
            // Copia il file chiave SSH nella directory corretta
                sh 'cp /Users/raja/.ssh/id_rsa.pub .' 
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
    }

    post {
        always {
            // Nettoyage de l'espace de travail Jenkins
            step([$class: 'WsCleanup'])
        }
    }
}

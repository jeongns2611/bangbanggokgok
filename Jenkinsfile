pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        APP_HOST = 'ubuntu@j14a104.p.ssafy.io'
        APP_DEPLOY_DIR = '/home/ubuntu/banggok'
        DATA_HOST = 'ubuntu@j14a104a.p.ssafy.io'
        DATA_DEPLOY_DIR = '/home/ubuntu/backend-job'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh '''
                    echo "BRANCH_NAME=$BRANCH_NAME"
                    pwd
                    ls -al
                '''
            }
        }

        stage('Backend CI') {
            agent {
                docker {
                    image 'gradle:8.7-jdk17'
                    reuseNode true
                }
            }
            steps {
                dir('backend') {
                    sh '''
                        chmod +x ./gradlew
                        SPRING_PROFILES_ACTIVE=test ./gradlew clean test bootJar --no-daemon
                    '''
                }
            }
        }

        stage('Frontend CI') {
            agent {
                docker {
                    image 'node:20-alpine'
                    reuseNode true
                }
            }
            steps {
                dir('frontend') {
                    sh '''
                        npm ci || npm install
                        npm run build
                    '''
                }
            }
        }

        stage('Deploy EC2-1') {
            when {
                branch 'master'
            }
            steps {
                sshagent(credentials: ['banggok-ssh-key']) {
                    sh '''
                        mkdir -p /root/.ssh
                        ssh-keyscan -H j14a104.p.ssafy.io >> /root/.ssh/known_hosts
                        chmod 600 /root/.ssh/known_hosts

                        rsync -az --delete \
                          --exclude=.git \
                          --exclude=.env \
                          --exclude='.env*' \
                          --exclude='nginx/certs/' \
                          --exclude='nginx/conf.d/default.conf' \
                          ./ ${APP_HOST}:${APP_DEPLOY_DIR}/

                        ssh ${APP_HOST} "cd ${APP_DEPLOY_DIR} && sudo docker compose up -d --build app frontend && sudo docker compose restart nginx"
                    '''
                }
            }
        }
//
//         stage('Deploy Spark Code To EC2-2') {
//             when {
//                 branch 'master'
//             }
//             steps {
//                 sshagent(credentials: ['banggok-ssh-key']) {
//                     sh '''
//                         mkdir -p ~/.ssh
//                         ssh-keyscan -H j14a104a.p.ssafy.io >> ~/.ssh/known_hosts
//                         chmod 600 ~/.ssh/known_hosts
//
//                         rsync -az --delete \
//                           --exclude=.git \
//                           ./backend/ ${DATA_HOST}:${DATA_DEPLOY_DIR}/
//                     '''
//                 }
//             }
//         }
//
//         stage('Run Spark Job On EC2-2') {
//             when {
//                 branch 'master'
//             }
//             steps {
//                 sshagent(credentials: ['banggok-ssh-key']) {
//                     sh '''
//                         mkdir -p ~/.ssh
//                         ssh-keyscan -H j14a104a.p.ssafy.io >> ~/.ssh/known_hosts
//                         chmod 600 ~/.ssh/known_hosts
//
//                         ssh ${DATA_HOST} '
//                             cd ${DATA_DEPLOY_DIR}/scripts &&
//                             chmod +x run_spark_job.sh &&
//                             ./run_spark_job.sh
//                         '
//                     '''
//                 }
//             }
//         }
    }

    post {
        success {
            echo 'Pipeline succeeded'
        }
        failure {
            echo 'Pipeline failed'
        }
    }
}

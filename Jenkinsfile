pipeline {
    agent any

    tools {
        maven 'maven-3.9'
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
    }

    stages {
        stage('Initialize & Clean') {
            steps {
                cleanWs()
                checkout scm
                echo 'Workspace cleaned and initialized.'
            }
        }

        stage('Global Dependencies Setup') {
            steps {
                sh 'mvn clean install -DskipTests -pl common-library -am'
            }
        }

        stage('Microservices Pipelines') {
            parallel {
                stage('Backoffice-BFF') {
                    when { 
                        expression { 
                           return hasChanges('common-library/**') || hasChanges('backoffice-bff/**') 
                        }
                    }
                    steps { buildService('backoffice-bff') }
                }
                stage('Cart') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('cart/**')
                        }
                    }
                    steps { buildService('cart') }
                }
                stage('Customer') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('customer/**')
                        }
                    }
                    steps { buildService('customer') }
                }
                stage('Inventory') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('inventory/**')
                        }
                    }
                    steps { buildService('inventory') }
                }
                stage('Location') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('location/**')
                        }
                    }
                    steps { buildService('location') }
                }
                stage('Media') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('media/**')
                        }
                    }
                    steps { buildService('media') }
                }
                stage('Order') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('order/**')
                        }
                    }
                    steps { buildService('order') }
                }
                stage('Payment') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('payment/**')
                        }
                    }
                    steps { buildService('payment') }
                }
                stage('Payment-Paypal') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('payment-paypal/**')
                        }
                    }
                    steps { buildService('payment-paypal') }
                }
                stage('Product') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('product/**')
                        }
                    }
                    steps { buildService('product') }
                }
                stage('Promotion') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('promotion/**')
                        }
                    }
                    steps { buildService('promotion') }
                }
                stage('Rating') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('rating/**')
                        }
                    }
                    steps { buildService('rating') }
                }
                stage('Recommendation') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('recommendation/**')
                        }
                    }
                    steps { buildService('recommendation') }
                }
                stage('Sampledata') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('sampledata/**')
                        }
                    }
                    steps { buildService('sampledata') }
                }
                stage('Search') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('search/**')
                        }
                    }
                    steps { buildService('search') }
                }
                stage('Storefront-BFF') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('storefront-bff/**')
                        }
                    }
                    steps { buildService('storefront-bff') }
                }
                stage('Tax') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('tax/**')
                        }
                    }
                    steps { buildService('tax') }
                }
                stage('Webhook') {
                    when {
                        expression {
                            return hasChanges('common-library/**') || hasChanges('webhook/**')
                        }
                    }
                    steps { buildService('webhook') }
                }
            }
        }
    }

    post {
        success {
            echo 'Gom tất cả báo cáo Coverage và kiểm tra ngưỡng 70%...'
            recordCoverage(
                tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                sourceDirectories: getSourcePaths(),
                qualityGates: [
                    [threshold: 70.0, metric: 'LINE', baseline: 'PROJECT', criticality: 'FAILURE']
                ]
            )
        }
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}

def hasChanges(String pattern) {
    def branchName = env.BRANCH_NAME
    
    if (branchName == 'main' || branchName == 'develop') {
        return anyOf { changeset pattern }
    }

    try {
        def changedFiles = sh(
            script: "git diff --name-only origin/main...HEAD", 
            returnStdout: true
        ).trim()
        
        return changedFiles.split('\n').any { it.matches("${pattern.replace('/**', '.*')}") }
    } catch (Exception e) {
        return true
    }
}

def buildService(String serviceName) {
    echo "--- Processing Service: ${serviceName} ---"
    
    sh "mvn clean install -DskipTests -pl ${serviceName} -am"
    sh "mvn test jacoco:report -pl ${serviceName} -am"

    sh "/opt/snyk/snyk-linux test --file=${serviceName}/pom.xml || true"

    withSonarQubeEnv('yas') {
        sh """
            mvn sonar:sonar \
            -pl ${serviceName} \
            -am \
            -Dsonar.projectKey=thuonghong_yas-${serviceName} \
            -Dsonar.projectName=yas-${serviceName} \
            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
        """
    }
}

def getSourcePaths() {
    def services = [
        'search', 'media', 'cart', 'customer', 'inventory', 'location', 
        'order', 'payment', 'payment-paypal', 'product', 'promotion', 
        'rating', 'recommendation', 'sampledata', 'storefront-bff', 
        'tax', 'webhook', 'backoffice-bff', 'common-library'
    ]
    return services.collect { [path: "${it}/src/main/java"] }
}
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
                        anyOf {
                            changeset 'common-library/**'
                            changeset 'backoffice-bff/**'
                            expression { env.BUILD_NUMBER == '1' } 
                        } 
                    }
                    steps { buildService('backoffice-bff') }
                }
                stage('Cart') {
                    when { anyOf { changeset 'common-library/**'; changeset 'cart/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('cart') }
                }
                stage('Customer') {
                    when { anyOf { changeset 'common-library/**'; changeset 'customer/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('customer') }
                }
                stage('Inventory') {
                    when { anyOf { changeset 'common-library/**'; changeset 'inventory/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('inventory') }
                }
                stage('Location') {
                    when { anyOf { changeset 'common-library/**'; changeset 'location/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('location') }
                }
                stage('Media') {
                    when { anyOf { changeset 'common-library/**'; changeset 'media/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('media') }
                }
                stage('Order') {
                    when { anyOf { changeset 'common-library/**'; changeset 'order/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('order') }
                }
                stage('Payment') {
                    when { anyOf { changeset 'common-library/**'; changeset 'payment/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('payment') }
                }
                stage('Payment-Paypal') {
                    when { anyOf { changeset 'common-library/**'; changeset 'payment-paypal/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('payment-paypal') }
                }
                stage('Product') {
                    when { anyOf { changeset 'common-library/**'; changeset 'product/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('product') }
                }
                stage('Promotion') {
                    when { anyOf { changeset 'common-library/**'; changeset 'promotion/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('promotion') }
                }
                stage('Rating') {
                    when { anyOf { changeset 'common-library/**'; changeset 'rating/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('rating') }
                }
                stage('Recommendation') {
                    when { anyOf { changeset 'common-library/**'; changeset 'recommendation/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('recommendation') }
                }
                stage('Sampledata') {
                    when { anyOf { changeset 'common-library/**'; changeset 'sampledata/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('sampledata') }
                }
                stage('Search') {
                    when { anyOf { changeset 'common-library/**'; changeset 'search/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('search') }
                }
                stage('Storefront-BFF') {
                    when { anyOf { changeset 'common-library/**'; changeset 'storefront-bff/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('storefront-bff') }
                }
                stage('Tax') {
                    when { anyOf { changeset 'common-library/**'; changeset 'tax/**'; expression { env.BUILD_NUMBER == '1' } } }
                    steps { buildService('tax') }
                }
                stage('Webhook') {
                    when { anyOf { changeset 'common-library/**'; changeset 'webhook/**'; expression { env.BUILD_NUMBER == '1' } } }
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

// def buildService(String serviceName) {
//     echo "--- Processing Service: ${serviceName} ---"
//     sh "mvn install -DskipTests -pl ${serviceName} -am"
//     sh "mvn test jacoco:report -pl ${serviceName} -am"
//     // sh "mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl ${serviceName} -am"
//     sh """
//         mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
//         -pl ${serviceName} \
//         -am \
//         -Dsonar.projectKey=thuonghong_yas-${serviceName} \
//         -Dsonar.projectName=yas-${serviceName} \
//         -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
//     """
// }
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
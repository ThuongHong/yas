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
                // sh "find . -name mvnw -exec chmod +x {} \\;"
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
                    when { anyOf { changeset 'common-library/**'; changeset 'backoffice-bff/**' } }
                    steps { buildService('backoffice-bff') }
                }
                stage('Cart') {
                    when { anyOf { changeset 'common-library/**'; changeset 'cart/**' } }
                    steps { buildService('cart') }
                }
                stage('Customer') {
                    when { anyOf { changeset 'common-library/**'; changeset 'customer/**' } }
                    steps { buildService('customer') }
                }
                stage('Inventory') {
                    when { anyOf { changeset 'common-library/**'; changeset 'inventory/**' } }
                    steps { buildService('inventory') }
                }
                stage('Location') {
                    when { anyOf { changeset 'common-library/**'; changeset 'location/**' } }
                    steps { buildService('location') }
                }
                stage('Media') {
                    when { anyOf { changeset 'common-library/**'; changeset 'media/**' } }
                    steps { buildService('media') }
                }
                stage('Order') {
                    when { anyOf { changeset 'common-library/**'; changeset 'order/**' } }
                    steps { buildService('order') }
                }
                stage('Payment') {
                    when { anyOf { changeset 'common-library/**'; changeset 'payment/**' } }
                    steps { buildService('payment') }
                }
                stage('Payment-Paypal') {
                    when { anyOf { changeset 'common-library/**'; changeset 'payment-paypal/**' } }
                    steps { buildService('payment-paypal') }
                }
                stage('Product') {
                    when { anyOf { changeset 'common-library/**'; changeset 'product/**' } }
                    steps { buildService('product') }
                }
                stage('Promotion') {
                    when { anyOf { changeset 'common-library/**'; changeset 'promotion/**' } }
                    steps { buildService('promotion') }
                }
                stage('Rating') {
                    when { anyOf { changeset 'common-library/**'; changeset 'rating/**' } }
                    steps { buildService('rating') }
                }
                stage('Recommendation') {
                    when { anyOf { changeset 'common-library/**'; changeset 'recommendation/**' } }
                    steps { buildService('recommendation') }
                }
                stage('Sampledata') {
                    when { anyOf { changeset 'common-library/**'; changeset 'sampledata/**' } }
                    steps { buildService('sampledata') }
                }
                stage('Search') {
                    when { anyOf { changeset 'common-library/**'; changeset 'search/**' } }
                    steps { buildService('search') }
                }
                stage('Storefront-BFF') {
                    when { anyOf { changeset 'common-library/**'; changeset 'storefront-bff/**' } }
                    steps { buildService('storefront-bff') }
                }
                stage('Tax') {
                    when { anyOf { changeset 'common-library/**'; changeset 'tax/**' } }
                    steps { buildService('tax') }
                }
                stage('Webhook') {
                    when { anyOf { changeset 'common-library/**'; changeset 'webhook/**' } }
                    steps { buildService('webhook') }
                }
            }
        }

        // stage('Secret Scan (Gitleaks)') {
        //     steps {
        //         echo 'Running Gitleaks to detect secrets and credentials in source code...'
        //         sh '''
        //             gitleaks detect \
        //             --source . \
        //             --report-format json \
        //             --report-path gitleaks-report.json \
        //             --exit-code 0
        //         '''
        //         archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
        //     }
        // }

        stage('Security Scan (Snyk - Root)') {
            steps {
                echo 'Running Snyk scan for entire monorepo...'

                snykSecurity(
                    snykInstallation: 'snyk-tool',
                    snykTokenId: 'snyk-token',
                    failOnIssues: false,
                    additionalArguments: '--all-projects --maven-aggregate-project'
                )
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

def buildService(String serviceName) {
    echo "--- Processing Service: ${serviceName} ---"
    
    sh "mvn clean install -DskipTests -pl ${serviceName} -am"
    sh "mvn test jacoco:report -pl ${serviceName} -am"

    // snykSecurity(
    //     snykInstallation: 'snyk-tool',
    //     snykTokenId: 'snyk-token',
    //     targetFile: "${serviceName}/pom.xml",
    //     failOnIssues: false,
    //     additionalArguments: '--debug --org=thuonghong -Drevision=1.0-SNAPSHOT'
    // )

    withSonarQubeEnv('yas') {
        sh """
            mvn sonar:sonar \
            -pl ${serviceName} \
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
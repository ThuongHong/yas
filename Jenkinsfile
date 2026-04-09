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
                script {
                    def baseRef = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT
                    if (!baseRef) {
                        baseRef = sh(
                            script: """
                                git merge-base HEAD origin/main 2>/dev/null \
                                || git merge-base HEAD origin/master 2>/dev/null \
                                || git rev-parse HEAD^1 2>/dev/null \
                                || echo ''
                            """,
                            returnStdout: true
                        ).trim()
                    }

                    def changedFiles = ''
                    if (baseRef) {
                        changedFiles = sh(
                            script: "git diff --name-only ${baseRef} HEAD 2>/dev/null || echo ''",
                            returnStdout: true
                        ).trim()
                    }

                    echo "Base ref: ${baseRef ?: '(none — build all)'}"
                    echo "Changed files:\n${changedFiles ?: '(none — build all)'}"

                    def allServices = ['backoffice-bff', 'cart', 'customer', 'inventory', 'location',
                                       'media', 'order', 'payment', 'payment-paypal', 'product',
                                       'promotion', 'rating', 'recommendation', 'sampledata', 'search',
                                       'storefront-bff', 'tax', 'webhook']

                    def fileList = changedFiles ? changedFiles.split('\n') as List : []
                    def commonChanged = fileList.any { it.startsWith('common-library/') }

                    def servicesToBuild
                    if (!baseRef || commonChanged) {
                        servicesToBuild = allServices
                    } else {
                        servicesToBuild = allServices.findAll { svc ->
                            fileList.any { it.startsWith("${svc}/") }
                        }
                    }

                    echo "Services to build: ${servicesToBuild}"
                    env.SERVICES_TO_BUILD = servicesToBuild.join(',')
                }
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
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('backoffice-bff') } }
                    steps { buildService('backoffice-bff') }
                }
                stage('Cart') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('cart') } }
                    steps { buildService('cart') }
                }
                stage('Customer') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('customer') } }
                    steps { buildService('customer') }
                }
                stage('Inventory') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('inventory') } }
                    steps { buildService('inventory') }
                }
                stage('Location') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('location') } }
                    steps { buildService('location') }
                }
                stage('Media') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('media') } }
                    steps { buildService('media') }
                }
                stage('Order') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('order') } }
                    steps { buildService('order') }
                }
                stage('Payment') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('payment') } }
                    steps { buildService('payment') }
                }
                stage('Payment-Paypal') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('payment-paypal') } }
                    steps { buildService('payment-paypal') }
                }
                stage('Product') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('product') } }
                    steps { buildService('product') }
                }
                stage('Promotion') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('promotion') } }
                    steps { buildService('promotion') }
                }
                stage('Rating') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('rating') } }
                    steps { buildService('rating') }
                }
                stage('Recommendation') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('recommendation') } }
                    steps { buildService('recommendation') }
                }
                stage('Sampledata') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('sampledata') } }
                    steps { buildService('sampledata') }
                }
                stage('Search') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('search') } }
                    steps { buildService('search') }
                }
                stage('Storefront-BFF') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('storefront-bff') } }
                    steps { buildService('storefront-bff') }
                }
                stage('Tax') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('tax') } }
                    steps { buildService('tax') }
                }
                stage('Webhook') {
                    when { expression { env.SERVICES_TO_BUILD.tokenize(',').contains('webhook') } }
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
            when { changeRequest() }
            steps {
                echo 'Running Snyk scan for entire monorepo...'

                snykSecurity(
                    snykInstallation: 'snyk-tool',
                    snykTokenId: 'snyk-token',
                    failOnIssues: false,
                    targetFile: 'pom.xml',
                    additionalArguments: '--maven-aggregate-project'
                )
            }
        }
    }

    post {
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'

            recordIssues(
                tools: [junitParser(pattern: '**/target/surefire-reports/*.xml')],
                skipPublishingChecks: false
            )
        }   
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
    }
}

def buildService(String serviceName) {
    echo "--- Processing Service: ${serviceName} ---"

    sh "mvn clean install -DskipTests -pl ${serviceName} -am"
    sh "mvn test jacoco:report -pl ${serviceName} -am"

    // snykSecurity(
    //     snykInstallation: 'snyk-tool',
    //     snykTokenId: 'snyk-token',
    //     targetFile: "${serviceName}",
    //     failOnIssues: false,
    // )

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
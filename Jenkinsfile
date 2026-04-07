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

		stage('Pipeline Backoffice-BFF Service') {
			when {
				anyOf {
					changeset 'backoffice-bff/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Backoffice-BFF') {
					steps {
						echo 'Building Backoffice-BFF Service...'
						sh 'mvn clean install -DskipTests -pl backoffice-bff -am'
					}
				}
				stage('Test Backoffice-BFF') {
					steps {
						echo 'Running Backoffice-BFF Tests...'
						sh 'mvn test jacoco:report -pl backoffice-bff -am'
					}
				}
				stage('SonarQube Analysis Backoffice-BFF') {
					steps {
						echo 'Running SonarQube Analysis for Backoffice-BFF...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl backoffice-bff -am'
					}
				}
			}
		}

		stage('Pipeline Cart Service') {
			when {
				anyOf {
					changeset 'cart/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Cart') {
					steps {
						echo 'Building Cart Service...'
						sh 'mvn clean install -DskipTests -pl cart -am'
					}
				}
				stage('Test Cart') {
					steps {
						echo 'Running Cart Tests...'
						sh 'mvn test jacoco:report -pl cart -am'
					}
				}
				stage('SonarQube Analysis Cart') {
					steps {
						echo 'Running SonarQube Analysis for Cart...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl cart -am'
					}
				}
			}
		}

		stage('Pipeline Customer Service') {
			when {
				anyOf {
					changeset 'customer/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Customer') {
					steps {
						echo 'Building Customer Service...'
						sh 'mvn clean install -DskipTests -pl customer -am'
					}
				}
				stage('Test Customer') {
					steps {
						echo 'Running Customer Tests...'
						sh 'mvn test jacoco:report -pl customer -am'
					}
				}
				stage('SonarQube Analysis Customer') {
					steps {
						echo 'Running SonarQube Analysis for Customer...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl customer -am'
					}
				}
			}
		}

		stage('Pipeline Inventory Service') {
			when {
				anyOf {
					changeset 'inventory/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Inventory') {
					steps {
						echo 'Building Inventory Service...'
						sh 'mvn clean install -DskipTests -pl inventory -am'
					}
				}
				stage('Test Inventory') {
					steps {
						echo 'Running Inventory Tests...'
						sh 'mvn test jacoco:report -pl inventory -am'
					}
				}
				stage('SonarQube Analysis Inventory') {
					steps {
						echo 'Running SonarQube Analysis for Inventory...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl inventory -am'
					}
				}
			}
		}

		stage('Pipeline Location Service') {
			when {
				anyOf {
					changeset 'location/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Location') {
					steps {
						echo 'Building Location Service...'
						sh 'mvn clean install -DskipTests -pl location -am'
					}
				}
				stage('Test Location') {
					steps {
						echo 'Running Location Tests...'
						sh 'mvn test jacoco:report -pl location -am'
					}
				}
				stage('SonarQube Analysis Location') {
					steps {
						echo 'Running SonarQube Analysis for Location...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl location -am'
					}
				}
			}
		}

		stage('Pipeline Media Service') {
			when {
				anyOf {
					changeset 'media/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Media') {
					steps {
						echo 'Building Media Service...'
						sh 'mvn clean install -DskipTests -pl media -am'
					}
				}
				stage('Test Media') {
					steps {
						echo 'Running Media Tests...'
						sh 'mvn test jacoco:report -pl media -am'
					}
				}
				stage('SonarQube Analysis Media') {
					steps {
						echo 'Running SonarQube Analysis for Media...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl media -am'
					}
				}
			}
		}

		stage('Pipeline Order Service') {
			when {
				anyOf {
					changeset 'order/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Order') {
					steps {
						echo 'Building Order Service...'
						sh 'mvn clean install -DskipTests -pl order -am'
					}
				}
				stage('Test Order') {
					steps {
						echo 'Running Order Tests...'
						sh 'mvn test jacoco:report -pl order -am'
					}
				}
				stage('SonarQube Analysis Order') {
					steps {
						echo 'Running SonarQube Analysis for Order...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl order -am'
					}
				}
			}
		}

		stage('Pipeline Payment Service') {
			when {
				anyOf {
					changeset 'payment/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Payment') {
					steps {
						echo 'Building Payment Service...'
						sh 'mvn clean install -DskipTests -pl payment -am'
					}
				}
				stage('Test Payment') {
					steps {
						echo 'Running Payment Tests...'
						sh 'mvn test jacoco:report -pl payment -am'
					}
				}
				stage('SonarQube Analysis Payment') {
					steps {
						echo 'Running SonarQube Analysis for Payment...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl payment -am'
					}
				}
			}
		}

		stage('Pipeline Payment-Paypal Service') {
			when {
				anyOf {
					changeset 'payment-paypal/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Payment-Paypal') {
					steps {
						echo 'Building Payment-Paypal Service...'
						sh 'mvn clean install -DskipTests -pl payment-paypal -am'
					}
				}
				stage('Test Payment-Paypal') {
					steps {
						echo 'Running Payment-Paypal Tests...'
						sh 'mvn test jacoco:report -pl payment-paypal -am'
					}
				}
				stage('SonarQube Analysis Payment-Paypal') {
					steps {
						echo 'Running SonarQube Analysis for Payment-Paypal...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl payment-paypal -am'
					}
				}
			}
		}

		stage('Pipeline Product Service') {
			when {
				anyOf {
					changeset 'product/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Product') {
					steps {
						echo 'Building Product Service...'
						sh 'mvn clean install -DskipTests -pl product -am'
					}
				}
				stage('Test Product') {
					steps {
						echo 'Running Product Tests...'
						sh 'mvn test jacoco:report -pl product -am'
					}
				}
				stage('SonarQube Analysis Product') {
					steps {
						echo 'Running SonarQube Analysis for Product...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl product -am'
					}
				}
			}
		}

		stage('Pipeline Promotion Service') {
			when {
				anyOf {
					changeset 'promotion/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Promotion') {
					steps {
						echo 'Building Promotion Service...'
						sh 'mvn clean install -DskipTests -pl promotion -am'
					}
				}
				stage('Test Promotion') {
					steps {
						echo 'Running Promotion Tests...'
						sh 'mvn test jacoco:report -pl promotion -am'
					}
				}
				stage('SonarQube Analysis Promotion') {
					steps {
						echo 'Running SonarQube Analysis for Promotion...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl promotion -am'
					}
				}
			}
		}

		stage('Pipeline Rating Service') {
			when {
				anyOf {
					changeset 'rating/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Rating') {
					steps {
						echo 'Building Rating Service...'
						sh 'mvn clean install -DskipTests -pl rating -am'
					}
				}
				stage('Test Rating') {
					steps {
						echo 'Running Rating Tests...'
						sh 'mvn test jacoco:report -pl rating -am'
					}
				}
				stage('SonarQube Analysis Rating') {
					steps {
						echo 'Running SonarQube Analysis for Rating...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl rating -am'
					}
				}
			}
		}

		stage('Pipeline Recommendation Service') {
			when {
				anyOf {
					changeset 'recommendation/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Recommendation') {
					steps {
						echo 'Building Recommendation Service...'
						sh 'mvn clean install -DskipTests -pl recommendation -am'
					}
				}
				stage('Test Recommendation') {
					steps {
						echo 'Running Recommendation Tests...'
						sh 'mvn test jacoco:report -pl recommendation -am'
					}
				}
				stage('SonarQube Analysis Recommendation') {
					steps {
						echo 'Running SonarQube Analysis for Recommendation...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl recommendation -am'
					}
				}
			}
		}

		stage('Pipeline Sampledata Service') {
			when {
				anyOf {
					changeset 'sampledata/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Sampledata') {
					steps {
						echo 'Building Sampledata Service...'
						sh 'mvn clean install -DskipTests -pl sampledata -am'
					}
				}
				stage('Test Sampledata') {
					steps {
						echo 'Running Sampledata Tests...'
						sh 'mvn test jacoco:report -pl sampledata -am'
					}
				}
				stage('SonarQube Analysis Sampledata') {
					steps {
						echo 'Running SonarQube Analysis for Sampledata...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl sampledata -am'
					}
				}
			}
		}

		stage('Pipeline Search Service') {
			when {
				anyOf {
					changeset 'search/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Search') {
					steps {
						echo 'Building Search Service...'
						sh 'mvn clean install -DskipTests -pl search -am'
					}
				}
				stage('Test Search') {
					steps {
						echo 'Running Search Tests...'
						sh 'mvn test jacoco:report -pl search -am'
					}
				}
				stage('SonarQube Analysis Search') {
					steps {
						echo 'Running SonarQube Analysis for Search...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl search -am'
					}
				}
			}
		}

		stage('Pipeline Storefront-BFF Service') {
			when {
				anyOf {
					changeset 'storefront-bff/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Storefront-BFF') {
					steps {
						echo 'Building Storefront-BFF Service...'
						sh 'mvn clean install -DskipTests -pl storefront-bff -am'
					}
				}
				stage('Test Storefront-BFF') {
					steps {
						echo 'Running Storefront-BFF Tests...'
						sh 'mvn test jacoco:report -pl storefront-bff -am'
					}
				}
				stage('SonarQube Analysis Storefront-BFF') {
					steps {
						echo 'Running SonarQube Analysis for Storefront-BFF...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl storefront-bff -am'
					}
				}
			}
		}

		stage('Pipeline Tax Service') {
			when {
				anyOf {
					changeset 'tax/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Tax') {
					steps {
						echo 'Building Tax Service...'
						sh 'mvn clean install -DskipTests -pl tax -am'
					}
				}
				stage('Test Tax') {
					steps {
						echo 'Running Tax Tests...'
						sh 'mvn test jacoco:report -pl tax -am'
					}
				}
				stage('SonarQube Analysis Tax') {
					steps {
						echo 'Running SonarQube Analysis for Tax...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl tax -am'
					}
				}
			}
		}

		stage('Pipeline Webhook Service') {
			when {
				anyOf {
					changeset 'webhook/**'
					// changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Webhook') {
					steps {
						echo 'Building Webhook Service...'
						sh 'mvn clean install -DskipTests -pl webhook -am'
					}
				}
				stage('Test Webhook') {
					steps {
						echo 'Running Webhook Tests...'
						sh 'mvn test jacoco:report -pl webhook -am'
					}
				}
				stage('SonarQube Analysis Webhook') {
					steps {
						echo 'Running SonarQube Analysis for Webhook...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -pl webhook -am'
					}
				}
			}
		}
	}

    post {
        success {
            echo 'Gom tất cả báo cáo Coverage...'
            recordCoverage(
                tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                sourceDirectories: [
                    [path: 'search/src/main/java'],
                    [path: 'media/src/main/java'],
                    [path: 'cart/src/main/java'],
                    [path: 'customer/src/main/java'],
                    [path: 'inventory/src/main/java'],
                    [path: 'location/src/main/java'],
                    [path: 'order/src/main/java'],
                    [path: 'payment/src/main/java'],
                    [path: 'payment-paypal/src/main/java'],
                    [path: 'product/src/main/java'],
                    [path: 'promotion/src/main/java'],
                    [path: 'rating/src/main/java'],
                    [path: 'recommendation/src/main/java'],
                    [path: 'sampledata/src/main/java'],
                    [path: 'storefront-bff/src/main/java'],
                    [path: 'tax/src/main/java'],
                    [path: 'webhook/src/main/java'],
                    [path: 'backoffice-bff/src/main/java'],
                    [path: 'common-library/src/main/java']
                ]
            )
        }
        always {
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
        }
    }
}

pipeline {
	agent any 
		
    tools {
        maven 'maven-3.9'
    }

	environment {
		SONAR_TOKEN = credentials('sonar-token')
	}

	stages {
		stage('Pipeline Backoffice-BFF Service') {
			when {
				anyOf {
					changeset 'backoffice-bff/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Backoffice-BFF') {
					steps {
						echo 'Building Backoffice-BFF Service...'
						sh 'mvn clean install -DskipTests -f backoffice-bff'
					}
				}
				stage('Test Backoffice-BFF') {
					steps {
						echo 'Running Backoffice-BFF Tests...'
						sh 'mvn test -f backoffice-bff'
					}
				}
				stage('SonarQube Analysis Backoffice-BFF') {
					steps {
						echo 'Running SonarQube Analysis for Backoffice-BFF...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f backoffice-bff'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'backoffice-bff/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Cart Service') {
			when {
				anyOf {
					changeset 'cart/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Cart') {
					steps {
						echo 'Building Cart Service...'
						sh 'mvn clean install -DskipTests -f cart'
					}
				}
				stage('Test Cart') {
					steps {
						echo 'Running Cart Tests...'
						sh 'mvn test -f cart'
					}
				}
				stage('SonarQube Analysis Cart') {
					steps {
						echo 'Running SonarQube Analysis for Cart...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f cart'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'cart/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Customer Service') {
			when {
				anyOf {
					changeset 'customer/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Customer') {
					steps {
						echo 'Building Customer Service...'
						sh 'mvn clean install -DskipTests -f customer'
					}
				}
				stage('Test Customer') {
					steps {
						echo 'Running Customer Tests...'
						sh 'mvn test -f customer'
					}
				}
				stage('SonarQube Analysis Customer') {
					steps {
						echo 'Running SonarQube Analysis for Customer...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f customer'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'customer/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Inventory Service') {
			when {
				anyOf {
					changeset 'inventory/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Inventory') {
					steps {
						echo 'Building Inventory Service...'
						sh 'mvn clean install -DskipTests -f inventory'
					}
				}
				stage('Test Inventory') {
					steps {
						echo 'Running Inventory Tests...'
						sh 'mvn test -f inventory'
					}
				}
				stage('SonarQube Analysis Inventory') {
					steps {
						echo 'Running SonarQube Analysis for Inventory...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f inventory'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'inventory/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Location Service') {
			when {
				anyOf {
					changeset 'location/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Location') {
					steps {
						echo 'Building Location Service...'
						sh 'mvn clean install -DskipTests -f location'
					}
				}
				stage('Test Location') {
					steps {
						echo 'Running Location Tests...'
						sh 'mvn test -f location'
					}
				}
				stage('SonarQube Analysis Location') {
					steps {
						echo 'Running SonarQube Analysis for Location...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f location'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'location/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Media Service') {
			when {
				anyOf {
					changeset 'media/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Media') {
					steps {
						echo 'Building Media Service...'
						sh 'mvn clean install -DskipTests -f media'
					}
				}
				stage('Test Media') {
					steps {
						echo 'Running Media Tests...'
						sh 'mvn test -f media'
					}
				}
				stage('SonarQube Analysis Media') {
					steps {
						echo 'Running SonarQube Analysis for Media...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f media'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'media/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Order Service') {
			when {
				anyOf {
					changeset 'order/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Order') {
					steps {
						echo 'Building Order Service...'
						sh 'mvn clean install -DskipTests -f order'
					}
				}
				stage('Test Order') {
					steps {
						echo 'Running Order Tests...'
						sh 'mvn test -f order'
					}
				}
				stage('SonarQube Analysis Order') {
					steps {
						echo 'Running SonarQube Analysis for Order...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f order'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'order/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Payment Service') {
			when {
				anyOf {
					changeset 'payment/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Payment') {
					steps {
						echo 'Building Payment Service...'
						sh 'mvn clean install -DskipTests -f payment'
					}
				}
				stage('Test Payment') {
					steps {
						echo 'Running Payment Tests...'
						sh 'mvn test -f payment'
					}
				}
				stage('SonarQube Analysis Payment') {
					steps {
						echo 'Running SonarQube Analysis for Payment...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f payment'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'payment/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Payment-Paypal Service') {
			when {
				anyOf {
					changeset 'payment-paypal/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Payment-Paypal') {
					steps {
						echo 'Building Payment-Paypal Service...'
						sh 'mvn clean install -DskipTests -f payment-paypal'
					}
				}
				stage('Test Payment-Paypal') {
					steps {
						echo 'Running Payment-Paypal Tests...'
						sh 'mvn test -f payment-paypal'
					}
				}
				stage('SonarQube Analysis Payment-Paypal') {
					steps {
						echo 'Running SonarQube Analysis for Payment-Paypal...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f payment-paypal'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'payment-paypal/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Product Service') {
			when {
				anyOf {
					changeset 'product/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Product') {
					steps {
						echo 'Building Product Service...'
						sh 'mvn clean install -DskipTests -f product'
					}
				}
				stage('Test Product') {
					steps {
						echo 'Running Product Tests...'
						sh 'mvn test -f product'
					}
				}
				stage('SonarQube Analysis Product') {
					steps {
						echo 'Running SonarQube Analysis for Product...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f product'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'product/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Promotion Service') {
			when {
				anyOf {
					changeset 'promotion/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Promotion') {
					steps {
						echo 'Building Promotion Service...'
						sh 'mvn clean install -DskipTests -f promotion'
					}
				}
				stage('Test Promotion') {
					steps {
						echo 'Running Promotion Tests...'
						sh 'mvn test -f promotion'
					}
				}
				stage('SonarQube Analysis Promotion') {
					steps {
						echo 'Running SonarQube Analysis for Promotion...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f promotion'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'promotion/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Rating Service') {
			when {
				anyOf {
					changeset 'rating/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Rating') {
					steps {
						echo 'Building Rating Service...'
						sh 'mvn clean install -DskipTests -f rating'
					}
				}
				stage('Test Rating') {
					steps {
						echo 'Running Rating Tests...'
						sh 'mvn test -f rating'
					}
				}
				stage('SonarQube Analysis Rating') {
					steps {
						echo 'Running SonarQube Analysis for Rating...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f rating'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'rating/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Recommendation Service') {
			when {
				anyOf {
					changeset 'recommendation/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Recommendation') {
					steps {
						echo 'Building Recommendation Service...'
						sh 'mvn clean install -DskipTests -f recommendation'
					}
				}
				stage('Test Recommendation') {
					steps {
						echo 'Running Recommendation Tests...'
						sh 'mvn test -f recommendation'
					}
				}
				stage('SonarQube Analysis Recommendation') {
					steps {
						echo 'Running SonarQube Analysis for Recommendation...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f recommendation'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'recommendation/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Sampledata Service') {
			when {
				anyOf {
					changeset 'sampledata/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Sampledata') {
					steps {
						echo 'Building Sampledata Service...'
						sh 'mvn clean install -DskipTests -f sampledata'
					}
				}
				stage('Test Sampledata') {
					steps {
						echo 'Running Sampledata Tests...'
						sh 'mvn test -f sampledata'
					}
				}
				stage('SonarQube Analysis Sampledata') {
					steps {
						echo 'Running SonarQube Analysis for Sampledata...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f sampledata'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'sampledata/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Search Service') {
			when {
				anyOf {
					changeset 'search/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Search') {
					steps {
						echo 'Building Search Service...'
						sh 'mvn clean install -DskipTests -f search'
					}
				}
				stage('Test Search') {
					steps {
						echo 'Running Search Tests...'
						sh 'mvn test -f search'
					}
				}
				stage('SonarQube Analysis Search') {
					steps {
						echo 'Running SonarQube Analysis for Search...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f search'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'search/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Storefront-BFF Service') {
			when {
				anyOf {
					changeset 'storefront-bff/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Storefront-BFF') {
					steps {
						echo 'Building Storefront-BFF Service...'
						sh 'mvn clean install -DskipTests -f storefront-bff'
					}
				}
				stage('Test Storefront-BFF') {
					steps {
						echo 'Running Storefront-BFF Tests...'
						sh 'mvn test -f storefront-bff'
					}
				}
				stage('SonarQube Analysis Storefront-BFF') {
					steps {
						echo 'Running SonarQube Analysis for Storefront-BFF...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f storefront-bff'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'storefront-bff/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Tax Service') {
			when {
				anyOf {
					changeset 'tax/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Tax') {
					steps {
						echo 'Building Tax Service...'
						sh 'mvn clean install -DskipTests -f tax'
					}
				}
				stage('Test Tax') {
					steps {
						echo 'Running Tax Tests...'
						sh 'mvn test -f tax'
					}
				}
				stage('SonarQube Analysis Tax') {
					steps {
						echo 'Running SonarQube Analysis for Tax...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f tax'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'tax/**/surefire-reports/*.xml'
				}
			}
		}

		stage('Pipeline Webhook Service') {
			when {
				anyOf {
					changeset 'webhook/**'
					changeset 'pom.xml'
				}
			}
			stages {
				stage('Build Webhook') {
					steps {
						echo 'Building Webhook Service...'
						sh 'mvn clean install -DskipTests -f webhook'
					}
				}
				stage('Test Webhook') {
					steps {
						echo 'Running Webhook Tests...'
						sh 'mvn test -f webhook'
					}
				}
				stage('SonarQube Analysis Webhook') {
					steps {
						echo 'Running SonarQube Analysis for Webhook...'
						sh 'mvn org.sonarsource.scanner.maven:sonar-maven-plugin:sonar -f webhook'
					}
				}
			}
			post {
				always {
					junit allowEmptyResults: true, testResults: 'webhook/**/surefire-reports/*.xml'
				}
			}
		}
	}
}

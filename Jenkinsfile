pipeline {

    agent {
        node {
            label 'Ubuntu_agent_1'
        }
    }

    options {
        buildDiscarder logRotator(
                    daysToKeepStr: '16', 
                    numToKeepStr: '10'
            )
    }
	
	
    tools {
        maven 'Maven3'
    }
	
    environment {
        DOCKER_IMAGE = 'hello_world:jenk_proj'	
    }

    stages {
     
        stage('Cleanup Workspace') {
            steps {
                cleanWs()
                sh """
                echo "Cleaned Up Workspace For Project"
                """
            }
        }

        


	
        stage('Code Checkout') {
            steps {
                checkout([
                    $class: 'GitSCM',
                    branches: [[name: '*/main']],
                    userRemoteConfigs: [[url: 'https://github.com/Naghme98/Jenkins_Proj_4.git']]
                ])
            }
        }
	    
        stage ('Unit test'){
            steps {
                sh 'mvn clean test'
            }
	    }
	
        stage('Code Analysis') {
            steps {
                sh """
                echo "Running Code Analysis"
                """
            }
        }
		
        stage ('Package') {
            steps {
                sh 'mvn -B -DskipTests clean package'
            }
        }
		
        stage('Build and Deploy Code') {

			when {
			     branch 'develop'
		    }
		
            steps {
                sh """
                    echo "Building Artifact"
                """
	    
                script {
			        docker_image = docker.build("${env.DOCKER_IMAGE}",'-f ./Dockerfile .')
		        }
		
			    sh """
                    echo "Deploying Code"
                """

				sh "docker rm newApp"
			    sh "docker run -d -p 8888:8080 --name newApp ${DOCKER_IMAGE}"
                
            }
        }
	    

    }

    post {
            always {
                echo 'I have finished and deleting workspace'
            }
            success {
                echo 'Job succeeeded!'
            }
            unstable {
                echo 'I am unstable :/'
            }
            failure {
                echo 'I failed :('
            }
            changed {
                echo 'Things were different before...'
            }
        }

}

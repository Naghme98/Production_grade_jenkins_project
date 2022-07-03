# Jenkins multi-branch pipeline

##Introduction:

In the past, about 20 years ago, when IT companies wanted to develop, create or invent a new product, they used to have two main groups of people. The One who develops it and the other one who tests and deploys it in the real world. They would work in the water-flow style of product development. As it is understandable, this process was time-consuming, slow, and not scalable for the growing needs of the technology industry. 
As a matter of necessity, in 2007-2008 Devops was invented and companies started moving toward using this layout for their project management. DevOps is a set of practices that combines software development and IT operations. CI/CD is a DevOps technique that simplifies Agile development by using the right tools to speed up deployment.
In this paper, we are going to study one of the CI/CD’s popular tools which are “Jenkins” and try to implement a semi-real scenario with the use of pipelining. 

## Methods:

Setting up the “Jenkins”:
Jenkins supports multi-node operations. By the means of the multi-node, we can set up different numbers of agents for the Jenkins controller. Then the controller will assign jobs to them. As a result, the scalability and fault tolerance will increase. 

Following the instructions inside “jenkins.io”, I was able to install Jenkins on a Ubuntu server as my controller. After that, I created a new Linux machine using AWS services and assigned it as my agent.

Managing an agent:
I went through these steps:
Manage Jenkins -> Manage Nodes and Cloud -> New Node

![image](https://user-images.githubusercontent.com/45916098/177034118-96f0cbe0-d6d1-48a9-a817-1fc8f006f063.png)
        
<p align=center>
<i>Figure 1: Agent configuration</i>
</p>

Because the controller tried to connect to the agent through ssh, I needed to add the Agent’s private key in the Jenkins credential.

![image](https://user-images.githubusercontent.com/45916098/177034141-77ae6a95-50a4-4c6f-982d-9970d7f20df7.png)

<p align=center>
<i>Figure 2: Updating Credentials for connecting to agent</i>
</p>
 
Creating a Maven project:
For this project, I simply created a HelloWorld Maven project and used it to apply steps of the pipeline and check if everything is right. 

![image](https://user-images.githubusercontent.com/45916098/177034152-cd40ce02-c55e-41e6-a607-80a18d86615c.png)

<p align=center>
<i>Figure 3: Sample Maven project</i>
</p>

### 1. Setting up SCM:
    For this part, I used Github as my SCM. For making the proper connections from Jenkins to Github and vice versa, I had to manage “Github Webhook” and “Jenkins credential configuration”. Webhooks allow you to build or set up integrations and I needed for receiving the updates on the repository. 

I went through these steps for Webhook:
Going to the repository -> Settings -> Webhooks -> Add webhook 
The address in the Payload URL should be my Jenkins controller’s public IP address.

![image](https://user-images.githubusercontent.com/45916098/177034168-acb937a3-78f0-49fd-9c8e-0620bcefa34f.png)

<p align=center>
<i>Figure 4: Manage Webhooks</i>
</p>

For allowing Jenkins to get connected to the private repositories too, I also created a personal access token and used it inside the credentials of the Jenkins.

For creating a Personal access token we need to go in this way:
Settings -> Developer settings -> Personal access tokens 


![image](https://user-images.githubusercontent.com/45916098/177034184-20425974-2380-4cf7-8273-844d49178069.png)
<p align=center>
<i>Figure 5: Github Personal Access Token</i>
</p>

### 2. Creating the Multi-branch pipeline in Jenkins: 
    A DevOps pipeline is a set of automated processes and tools that allows both developers and operations professionals to work cohesively to build and deploy code to a production environment.

Jenkins has different options for doing the CI/CD jobs like Freestyle, Pipeline, Multibranch Pipeline, etc. So, I went through creating a Multibranch Pipeline for this project. 
I had to add my repository address for the “Repository Https URL” part in the configurations and also set the strategy to “Discover branch” to “Only branches that are also field as PR”. Because my repository was public, there was no need to specify any credentials, but for private ones, we can use the credentials we created before.


![image](https://user-images.githubusercontent.com/45916098/177034189-f6ddc390-fdf1-4539-99e9-eccb2a5a83ba.png)
<p align=center>
<i>Figure 6: Multibranch pipeline configurations</i>
</p>

### 3. Managing the plugins: 
    The power of Jenkins is that it supports lots of plugins. In this project I needed some plugins to be installed and configured:
Blue Ocean: A graphical interface for the pipeline jobs.
Maven (One of my main problems was after installing this plugin. You need to go through “Manage Jenkins -> Global Tool Configuration -> Maven -> click on Maven installation -> give a name to it and select to auto download it)
Github plugin
Github pull request 
Docker pipeline (Not docker plugin -- be careful)

### 4. Project Scenario: 
    
![image](https://user-images.githubusercontent.com/45916098/177034201-68715acd-b44c-47d5-92ec-cd519911f020.png)    

<p align=center>
<i>Figure 7: Pipeline scenario </i>
</p>

I had three branches:
Feature: Simply developers would commit their codes in this branch and whenever they want to merge it with the production, they would create a Pull Request (PR) to the branch Develop.
Develop: This branch would be handled by seniors e.g. When they can merge the PRs from Feature and also create PR for the Main branch.
Main: This is the actual branch that my pipeline will check for creating the new version of the product. 

After Github receives any PR, it will use the configured Webhook to let Jenkins know there is some change there and probably there is a need for action.
In the next step, Jenkins will create the SSH connection to the Agent and run the pipeline code. The second step would be performing the actions defined in the code on this agent machine.



### 5. Pipeline code and stages:

    My pipeline consists of 6 stages. The last step is just only for the PRs on the Main branch (from Develop to Main) and will not be activated from the Feature branch.
Cleanup Workspace
Code Checkout: Checking out to the current Github repository’s Main branch (It needs to read this directory)
Unit test: Running some defined Unit tests using Maven
Code Analysis: This step is just a simple echo (Wasn’t part of my project )
Packaging: It will use Maven to create a .jar file for the new code
Building and Deploying the code: In this step first, it will delete the previous running container, and then, using the new .jar file, it will create a Docker image and run this Container.




## Code explanation:

![image](https://user-images.githubusercontent.com/45916098/177034282-b066a659-3ba7-4d43-a10c-707d63b24889.png)
    
<p align=center>
<i>Figure 8: Definition part of the pipeline code</i>
</p>


I defined the agent node that I wanted to run my pipeline on it, and  Maven as a tool and set some options. 


![image](https://user-images.githubusercontent.com/45916098/177034285-cf17e6d2-2044-4339-a5c9-7ae4c669c5ef.png)

<p align=center>
<i>Figure 9: First stages of the pipeline</i>
</p>

The second step was to define my first and second stages. The first stage would be called “cleanWs()” and simply echo a message for us. The other stage would checkout to the Main branch of the current repository.


![image](https://user-images.githubusercontent.com/45916098/177034292-be49ec78-f139-46b8-af76-393f942b61c8.png)
<p align=center>
<i>Figure 10: Unit test, Package and Code Analysis stages</i>
</p>


In these stages (Figure 10), I used two commands from “mvn” to run the tests and also create the .jar file for me.


![image](https://user-images.githubusercontent.com/45916098/177034302-8721cb67-22f9-41ed-bf71-bf7e1e69c92c.png)

<p align=center>
<i>Figure 11: Last stage of pipeline code<i>
</p>

In the last stage, it checks if only the branch is “Develop” it will start it’s work. Otherwise, it would ignore this part.

![image](https://user-images.githubusercontent.com/45916098/177034304-07c3d4c7-37a0-4948-813e-fdf4d977a696.png)

<p align=center>
<i>Figure 12: Post part of the pipeline code</i>
<p>

In the end, it always prints a message and checks the status, and based on the status, will print another message.


## Results:

I tried to do two pull requests, one from branch Feature to Develop and the other one from Develop to Main.
 
These are the outputs:

Feature -> Develop:

![image](https://user-images.githubusercontent.com/45916098/177034313-259a51da-8873-4894-820a-0a653f7952c4.png)
<p align=center>
<i>Figure 13: Pipeline output Branch Feature-> Develop</i>
</p>

As you can see, the Build stage escaped. 
Also you can go to the Console output of this job and see the exact details.

![image](https://user-images.githubusercontent.com/45916098/177034316-c16c1550-cefe-44eb-94b7-5b758001819e.png)
<p align=center>
<i>Figure 14: Pipeline graph  output Branch Feature -> Develop </i>
</p>

 
![image](https://user-images.githubusercontent.com/45916098/177034326-0a4d5dc4-3d8d-4c93-8be1-995d17bdf6e7.png)
<p align=center>
<i> Figure 15: Pipeline stages output status  Branch Feature-> Develop </i>
</p>




Develop -> Main:
    
![image](https://user-images.githubusercontent.com/45916098/177034341-6544b5e1-6e3c-44d9-8cfe-c50b7ebcbab9.png)
<p align=center>
<i>Figure 16: Pipeline output Branch Develop-> Main </i>
</p>



![image](https://user-images.githubusercontent.com/45916098/177034345-b1d1058d-f56a-49fa-8e82-99fdb210af55.png)
<p align=center>
<i>Figure 17: Pipeline graphical output Branch Develop-> Main </i>
</p>

![image](https://user-images.githubusercontent.com/45916098/177034356-b9cadb5a-3195-413f-91a3-db4d51ba7290.png)

<p align=center>
<i>Figure 18: Pipeline stages output status output Branch Develop-> Main  </i>
</p>


------
## References:

    1. https://aws.amazon.com/devops/what-is-devops/
    2. https://www.cuelogic.com/blog/what-is-devops
    3. https://www.edureka.co/blog/what-is-jenkins/
    4. https://medium.com/openwhisk/how-to-make-jenkins-pipeline-jobs-triggered-by-pull-requests-for-apache-projects-2a526f0eb366
    5. https://acloudguru.com/blog/engineering/adding-a-jenkins-agent-node
    6. https://console.aws.amazon.com/ec2/v2/home?region=us-east-1#ConnectToInstance:instanceId=i-0e163b3c98d12841d
    7. https://support.cloudbees.com/hc/en-us/articles/115003929412-How-to-create-an-agent-in-Linux-from-console
    8. https://www.jenkins.io/doc/book/using/using-agents/
    9. https://peiruwang.medium.com/build-ci-cd-multibranch-pipeline-with-jenkins-and-kubernetes-637de560d55a
    10. https://cloudacademy.com/blog/what-is-static-analysis-within-ci-cd-pipelines/
    11. https://www.mathworks.com/products/polyspace/static-analysis-notes/continuous-integration-continuous-delivery.html
    12. https://hackernoon.com/how-to-make-docker-images-with-jenkins-pipelines-xsh3uza
    13. https://www.jenkins.io/doc/tutorials/build-a-java-app-with-maven/
    14. https://www.baeldung.com/ops/jenkins-pipelines
    15. https://www.fosstechnix.com/build-java-project-using-maven-in-jenkins-pipeline/
    16. http://www.mastertheboss.com/cool-stuff/jenkins/creating-your-first-jenkins-pipeline/
    17. https://www.jenkins.io/doc/pipeline/examples/
    18. https://morioh.com/p/3ead02be43c7
    19. https://support.cloudbees.com/hc/en-us/articles/115003929412-How-to-create-an-agent-in-Linux-from-console
    20. https://devopscube.com/jenkins-pipeline-as-code/
    21. https://devopscube.com/jenkins-shared-library-tutorial/
    22. https://devopscube.com/create-jenkins-shared-library/
    23. https://devopscube.com/jenkins-2-tutorials-getting-started-guide/
    24. http://ceur-ws.org/Vol-2218/paper31.pdf
    25. https://www.jenkins.io/doc/tutorials/build-a-java-app-with-maven/




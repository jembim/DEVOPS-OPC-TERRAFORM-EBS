def containerFolder = "${PROJECT_NAME}/Cloud_Provision/IaaS/EBS_Single_Node"
def createJob = freeStyleJob(containerFolder + '/Creating_EBS_Instance')

createJob.with {
    description('')
    parameters {
        stringParam('OPC_USERNAME', '', '')
        stringParam('OPC_PASSWORD', '', '')
        stringParam('DOMAIN', '', '')
        stringParam('ENDPOINT_URL', '', '')
        stringParam('INSTANCE_NAME', '', '')
        stringParam('SSH_KEY', '', '')
		stringParam('IMAGE', '', '')
		stringParam('SHAPE', '', '')
        stringParam('BOOT_STORAGE_SIZE', '', '')
        stringParam('NON_BOOT_STORAGE_SIZE', '', '')
		stringParam('SECURITY_LIST', '', '')
		stringParam('CUSTOM_WORKSPACE', '', '')
	}
	
    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }

    concurrentBuild(true)
    label('postgres')
    customWorkspace('$CUSTOM_WORKSPACE')

    wrappers {
        preBuildCleanup() 
        colorizeOutput('css')
    }

    steps {

        copyArtifacts('Set_Instance_Parameters') {
            includePatterns('**/*')
            fingerprintArtifacts(true)
            buildSelector {
                upstreamBuild(true)
                latestSuccessful(false)
            }
        }

        shell('''#!/bin/bash
terraform init
terraform plan -var "user=${OPC_USERNAME}" -var "password=${OPC_PASSWORD}" --var-file=credentials.tfvars
terraform apply -var "user=${OPC_USERNAME}" -var "password=${OPC_PASSWORD}" --var-file=credentials.tfvars

        ''')
    }
}
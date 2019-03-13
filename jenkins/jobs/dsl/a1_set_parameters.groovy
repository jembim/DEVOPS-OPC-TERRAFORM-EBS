def createJob = freeStyleJob("${PROJECT_NAME}/Cloud_Provision/IaaS/EBS_Single_Node/Set_Instance_Parameters")
def scmProject = "git@gitlab:${WORKSPACE_NAME}/Oracle_Tech.git"
def scmCredentialsId = "adop-jenkins-master"

Closure passwordParam(String paramName, String paramDescription, String paramDefaultValue) {
    return { project ->
        project / 'properties' / 'hudson.model.ParametersDefinitionProperty' / 'parameterDefinitions' << 'hudson.model.PasswordParameterDefinition' {
            'name'(paramName)
      		'description'(paramDescription)
        	'defaultValue'(paramDefaultValue)
        }
    }
}

folder("${PROJECT_NAME}/Cloud_Provision") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

folder("${PROJECT_NAME}/Cloud_Provision/IaaS") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

folder("${PROJECT_NAME}/Cloud_Provision/IaaS/EBS_Single_Node") {
  configure { folder ->
    folder / icon(class: 'org.example.MyFolderIcon')
  }
}

createJob.with {
    description('')
    parameters {
        stringParam('OPC_USERNAME', '', 'Account\'s username in OPC (Oracle Public Cloud).')
        stringParam('DOMAIN', '580502591', 'Oracle Public Cloud Account\'s Identity Domain. (eg. a424647)')
        stringParam('ENDPOINT_URL', 'https://compute.aucom-east-1.oraclecloud.com', 'Oracle Public Cloud Endpoint URL. (eg. https://compute.aucom-east-1.oraclecloud.com)')
        stringParam('INSTANCE_NAME', '', 'Unique name for the instance. (eg. test_gft_server)')
        stringParam('SSH_KEY', 'rsa-key-tollgftdev-new', 'The key must be existing in Oracle Cloud and has no passphrase in it. (eg. oracle_key)')
		stringParam('IMAGE', 'OPC_OL6_8_EBS_1226_FRESH_INSTALL_SINGLE_TIER_11302016', 'Private EBS Image. (eg. OL_6.8_UEKR4_x86_64)')
		stringParam('SHAPE', 'oc2m', 'Instance shape (OCPU and memory).')
        stringParam('BOOT_STORAGE_SIZE', '323', 'Bootable Storage size in GigaByte. (Note: The required size for the image boot storage is 323 GB and higher.')
        stringParam('NON_BOOT_STORAGE_SIZE', '300', 'Non-bootable Storage size in GigaByte.')
		stringParam('SECURITY_LIST', '', 'Name of the security list to be added in the instance. (Note: EBS_security_list has all EBS required ports)')
    }
    configure passwordParam("OPC_PASSWORD", "Account\'s password in OPC (Oracle Public Cloud)", "")
	
    logRotator {
        numToKeep(10)
        artifactNumToKeep(10)
    }

    concurrentBuild(true)
    label('postgres')

    scm {
        git {
            remote {
                url(scmProject)
                credentials(scmCredentialsId)
            }
            branch('*/master')
        }
    }

    wrappers {
        preBuildCleanup() 
        colorizeOutput('css')
    }

    steps {
		shell('''#!/bin/bash

echo "CUSTOM_WORKSPACE=${WORKSPACE}" > props
mv ${WORKSPACE}/Cloud_Provision/IaaS/ebsr12-terraform/* .
find . -type d -exec rm -rf {} +

cat > credentials.tfvars <<-EOF
domain = "${DOMAIN}"
endpoint = "${ENDPOINT_URL}"
sshkey = "/Compute-${DOMAIN}/${OPC_USERNAME}/${SSH_KEY}"
instance_name = "${INSTANCE_NAME}"
image_list = "/Compute-${DOMAIN}/${OPC_USERNAME}/${IMAGE}"
shape = "${SHAPE}"
ebs_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/${SECURITY_LIST}"
http_seclist = "/Compute-${DOMAIN}/${OPC_USERNAME}/defaultHTTP"
boot_storage_size = "${BOOT_STORAGE_SIZE}"
storage_size = "${NON_BOOT_STORAGE_SIZE}"
EOF

		''')
	
        environmentVariables {
            propertiesFile('props')
        }
    }

    publishers {

        archiveArtifacts('**/*')

        downstreamParameterized {
            trigger('Creating_EBS_Instance') {
                condition('SUCCESS')
                parameters {
                    currentBuild()
                    propertiesFile('props', true)
                }
            }
        }
    }

}
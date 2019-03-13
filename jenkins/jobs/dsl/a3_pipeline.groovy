def containerFolder = "${PROJECT_NAME}/Cloud_Provision/IaaS/EBS_Single_Node"

buildPipelineView(containerFolder + '/Provision_Oracle_EBS') {
    title('Provision Oracle EBS')
    displayedBuilds(10)
    selectedJob('Set_Instance_Parameters')
	showPipelineDefinitionHeader()
    showPipelineParameters()
	consoleOutputLinkStyle(OutputStyle.NewWindow)
    refreshFrequency(3)
}
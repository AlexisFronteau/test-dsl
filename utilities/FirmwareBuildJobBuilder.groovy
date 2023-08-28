package utilities
import groovy.json.JsonSlurper

/**
 * Generates jobs that check if code is correclty formatted
 */

class FirmwareBuildJobBuilder {

    // static def PARAMS_LIST = [
    //     "FULL_BUILD": [ type: "boolean", defaultValue: false, description: "Check to clean whole project before building. Otherwise it will only rebuild the applicative part of the firmware" ],
    //     "FEATURE": [ type: "string", defaultValue: "None", description: "The feature (branch) to select if it exists" ],
    //     "BASE_BRANCH": [ type: "string", defaultValue: "master", description: "The branch on which to base the build" ],
    //     "PLATFORM": [ type: "string", defaultValue: "None", description: "The platform to be built" ]
    // ]

    String m_sDirectory = ''
    String m_sJobName = ''
    String m_sRepo_name = ''
    boolean m_bLaunchNightly = true
    String m_sLaunchAfterJob = ''
    String m_sDefaultBaseBranch = 'master'
    String m_sType = ''
    boolean m_bHasDefaultParams = true
    def m_lParamsList = [:]

    FirmwareBuildJobBuilder(dslFactory, base_dir, type, json) {
        m_sDirectory = base_dir
        if (json.keySet().contains('path')) {
            m_sDirectory += ('/' + json.path)
        }

        if (json.keySet().contains('jobName')) {
            m_sJobName = json.jobName
        }
        else if (json.keySet().contains('repo')) {
            m_sJobName = json.repo.toUpperCase()
        }

        if (json.keySet().contains('repo')) {
            m_sRepo_name = json.repo
        }

        m_sType = type

        if (json.keySet().contains('launchAfterJob')) {
            m_sLaunchAfterJob = json.launchAfterJob
        }

        if (json.keySet().contains('launchNightly')) {
            m_bLaunchNightly = json.launchNightly
        }

        if (json.keySet().contains('hasDefaultParams')) {
            m_bHasDefaultParams = json.hasDefaultParams
        }

        if (json.keySet().contains('baseBranch')) {
            m_sDefaultBaseBranch = json.baseBranch
        }

        def param_list = new JsonSlurper().parseText(dslFactory.readFileFromWorkspace('job_parameters.json'))
        
        // Set FEATURE and BASE_BRANCH as default parameters 
        if (m_bHasDefaultParams) {
            param_list.default.each { key, value->
                m_lParamsList[key] = value
            }
        }

        if (json.keySet().contains('additionalParams')) {
            json.additionalParams.each { paramName ->
                if (!param_list.additional.keySet().contains(paramName)) {
                    throw new Exception("Additional param not exists in list - need to had it")
                }

                dslFactory.out.println(paramName)
                dslFactory.out.println(param_list.additional[paramName])
                m_lParamsList[paramName] = param_list.additional[paramName]

                if (paramName == "PLATFORM") {
                    if (json.keySet().contains('platformParam')) {
                        if (!(json.platformParam instanceof List))
                        {
                            throw new Exception("platformParam must be a List")
                        }

                        m_lParamsList[paramName].defaultValue = json.platformParam[0]
                        m_lParamsList[paramName].description = m_lParamsList[paramName].description + ' [ ' + json.platformParam.join(", ") + ' ]'
                    }
                }
            }
        }
    }

    private String BuildDescription() {
        String str = ''

        switch (m_sType) {
            case "build":
                str += "Build firmware from ${m_sRepo_name}"
                break
            case "dockersdk" :
                str += "Build docker image containing already built builroot sdk from ${m_sLaunchAfterJob} and push it to registry"
                break;
        }

        return str
    }

    void generate_pipeline(dslFactory) {

        dslFactory.folder(m_sDirectory)

        def job = dslFactory.pipelineJob(m_sDirectory + '/' + m_sJobName)

        job.with {
            description(BuildDescription())
    
            disabled(true)

            if (!m_lParamsList.isEmpty())
            {
                parameters {
                    m_lParamsList.each { key, param -> 
                        switch (param.type) {
                            case "string":
                                stringParam {
                                    name(key)
                                    defaultValue(param.defaultValue)
                                    description(param.description)
                                    trim(false)
                                }
                                break
                            case "boolean":
                                booleanParam {
                                    name(key)
                                    defaultValue(param.defaultValue)
                                    description(param.description)
                                }
                                break;
                        }
                    }
                }
            }
            
            logRotator {
                numToKeep(5)
                daysToKeep(-1)
                artifactDaysToKeep(-1)
                artifactNumToKeep(-1)
            }

            properties {
                disableConcurrentBuilds {
                    abortPrevious(false)
                }
                
                durabilityHint {
                    hint('PERFORMANCE_OPTIMIZED')
                }

                if (m_bLaunchNightly == true) {
                    pipelineTriggers {
                        triggers {
                            cron {
                                spec("H 0 * * *")
                            }
                        }
                    }
                }

                if (m_sType == "dockersdk") {
                    if (m_sLaunchAfterJob.isEmpty()) {
                        throw new Exception('Dockersdk job must have a "launchAfterJob" parameter')
                    }

                    copyArtifactPermission {
                        projectNames("${m_sLaunchAfterJob}")
                    } 
                }

                if (!m_sLaunchAfterJob.isEmpty()) {
                    pipelineTriggers {
                        triggers {
                            upstream {
                                upstreamProjects("${m_sLaunchAfterJob}")
                                threshold('SUCCESS')
                            }
                        }
                    }
                }
            }
            
            definition {
                cpsScm {
                    scm {
                        git {
                            branch("${m_sDefaultBaseBranch}")
                            remote {
                                credentials('jenkins-github-ssh')
                                github("xofym/${m_sRepo_name}", 'ssh', 'github.com')
                            }
                            extensions {
                                localBranch {
                                    localBranch("${m_sDefaultBaseBranch}")
                                }
                            }
                        }
                    }
                    lightweight(true)
                    scriptPath('ci/Jenkinsfile.' + m_sType)
                }
            }
        }
    }

    static void generate_build_server_docker(dslFactory, dirpath) {
        dslFactory.folder(dirpath)

        def job = dslFactory.pipelineJob(dirpath + '/BUILD-SERVER-DOCKER')

        job.with {
            description("Build docker image that will be used by CI")
    
            disabled(true)
            
            properties {
                disableConcurrentBuilds {
                    abortPrevious(false)
                }
                
                durabilityHint {
                    hint('PERFORMANCE_OPTIMIZED')
                }
                
                pipelineTriggers {
                    triggers {
                        githubPush()
                    }
                }
            }
            
            definition {
                cpsScm {
                    scm {
                        git {
                            branch('master')
                            remote {
                                credentials('jenkins-github-ssh')
                                github("xofym/BE-CI-Tools", 'ssh', "github.com")
                            }
                            extensions {
                                pathRestriction {
                                    includedRegions('jenkins_pipelines/docker_fw_build/.*\njenkins_slave_builder/.*')
                                    excludedRegions('')
                                }
                            }
                        }
                    }
                    lightweight(true)
                    scriptPath('jenkins_pipelines/docker_fw_build/Jenkinsfile')
                }
            }
        }
    }
}
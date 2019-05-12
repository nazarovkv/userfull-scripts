import jetbrains.buildServer.configs.kotlin.v2018_2.*
import jetbrains.buildServer.configs.kotlin.v2018_2.buildFeatures.vcsLabeling
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2018.2"

project {
    description = "Contains all other projects"

    features {
        feature {
            id = "PROJECT_EXT_1"
            type = "ReportTab"
            param("startPage", "coverage.zip!index.html")
            param("title", "Code Coverage")
            param("type", "BuildReportTab")
        }
    }

    cleanup {
        preventDependencyCleanup = false
    }

    subProject(RootProject)
    subProject(RootProject2)
}


object RootProject : Project({
    name = "Root Project"

    vcsRoot(RootProject_UsefullScripts)
    vcsRoot(RootProject_UsefullScripts02)
    vcsRoot(RootProject_UsefullScripts03)

    buildType(RootProject_StartAll02)
    buildType(RootProject_StartAll04)
    buildType(RootProject_StartAll03)

    params {
        param("system.app02.branch", "master")
        param("system.versions.list", """
            Test 01 => BRANCH01
            Test 02 => BRANCH02
            Test 03 => BRANCH03
        """.trimIndent())
        param("system.app01.branch", "master")
    }

    subProject(RootProject_BuildComponents)
})

object RootProject_StartAll02 : BuildType({
    name = "Start All - 02"

    params {
        text("reverse.dep.RootProject_BuildComponents_App02.system.app02.branch", "Test_TC02", label = "Branch - App 02", display = ParameterDisplay.PROMPT, allowEmpty = true)
        text("reverse.dep.RootProject_BuildComponents_App01.system.app01.branch", "Test_TC01", label = "Branch - App 01", display = ParameterDisplay.PROMPT, allowEmpty = true)
    }

    dependencies {
        artifacts(RootProject_BuildComponents_App01) {
            artifactRules = "+.: "
            enabled = false
        }
    }
})

object RootProject_StartAll03 : BuildType({
    name = "Start All - 03"

    dependencies {
        artifacts(RootProject_BuildComponents_App01) {
            artifactRules = "+.: "
            enabled = false
        }
    }
})

object RootProject_StartAll04 : BuildType({
    name = "Start All - 04"

    allowExternalStatus = true
    enablePersonalBuilds = false
    maxRunningBuilds = 1

    params {
        param("reverse.dep.*.checkout.dir", "git-repos-all")
        param("env.checkout.dir", "git-repos-all")
        param("reverse.dep.*.checkout.dir.common", "local-build")
        param("system.app01.branch", "Test_TC-Start-branch")
        text("reverse.dep.RootProject_BuildComponents_App01.system.app02.branch", "master", label = "App 02 - branch", display = ParameterDisplay.PROMPT, allowEmpty = true)
    }

    vcs {
        checkoutMode = CheckoutMode.ON_SERVER
        showDependenciesChanges = true
    }

    steps {
        script {
            name = "01 step"
            enabled = false
            scriptContent = """echo "##teamcity[setParameter name='dep.RootProject_BuildComponents_App01.system.app01.branch' value='%app01.branch%']""""
        }
        script {
            scriptContent = "echo Hello"
        }
    }

    dependencies {
        snapshot(RootProject_BuildComponents_App01) {
            reuseBuilds = ReuseBuilds.NO
        }
    }
})

object RootProject_UsefullScripts : GitVcsRoot({
    name = "usefull-scripts_01"
    url = "git@github.com:nazarovkv/userfull-scripts.git"
    branch = "%system.app01.branch%"
    branchSpec = "+:*"
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "github-nkv"
    }
})

object RootProject_UsefullScripts02 : GitVcsRoot({
    name = "usefull-scripts_02"
    url = "github.com:nazarovkv/userfull-scripts.git"
    branch = "%system.app02.branch%"
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "github-nkv"
    }
})

object RootProject_UsefullScripts03 : GitVcsRoot({
    name = "usefull-scripts_03"
    url = "github.com:nazarovkv/userfull-scripts.git"
    branch = "%system.app03.branch%"
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "github-nkv"
    }
})


object RootProject_BuildComponents : Project({
    name = "Build components"

    buildType(RootProject_BuildComponents_App01)
    buildType(RootProject_BuildComponents_App03)
    buildType(RootProject_BuildComponents_App02)
    buildType(RootProject_BuildComponents_GetLatest)
    buildTypesOrder = arrayListOf(RootProject_BuildComponents_GetLatest, RootProject_BuildComponents_App01, RootProject_BuildComponents_App02, RootProject_BuildComponents_App03)
})

object RootProject_BuildComponents_App01 : BuildType({
    name = "Init"

    enablePersonalBuilds = false
    type = BuildTypeSettings.Type.DEPLOYMENT
    maxRunningBuilds = 1

    params {
        param("env.checkout.dir", "test")
    }

    vcs {
        root(RootProject_UsefullScripts, "+:init-app")

        checkoutMode = CheckoutMode.ON_SERVER
        checkoutDir = "%checkout.dir.common%"
        excludeDefaultBranchChanges = true
        showDependenciesChanges = true
    }

    steps {
        script {
            scriptContent = """echo "#teamcity[setParameter name='ddd' value='fff']""""
        }
    }

    features {
        vcsLabeling {
            vcsRootId = "${RootProject_UsefullScripts.id}"
        }
    }

    dependencies {
        snapshot(RootProject_BuildComponents_App02) {
            runOnSameAgent = true
            reuseBuilds = ReuseBuilds.NO
        }
        snapshot(RootProject_BuildComponents_App03) {
            reuseBuilds = ReuseBuilds.NO
        }
    }
})

object RootProject_BuildComponents_App02 : BuildType({
    name = "App 02 (Test_TC02)"

    vcs {
        root(RootProject_UsefullScripts02, "+:=>app01")

        checkoutMode = CheckoutMode.ON_SERVER
        checkoutDir = "%checkout.dir.common%"
    }
})

object RootProject_BuildComponents_App03 : BuildType({
    name = "App 03 (Test_TC03)"

    params {
        param("system.app03.branch", "master")
    }

    vcs {
        root(RootProject_UsefullScripts03)

        checkoutMode = CheckoutMode.ON_SERVER
        checkoutDir = "%checkout.dir.common%"
    }
})

object RootProject_BuildComponents_GetLatest : BuildType({
    name = "GetLatest"

    vcs {
        root(RootProject_UsefullScripts, "+:. => git1")
        root(RootProject_UsefullScripts02, "+:. => git2")

        checkoutMode = CheckoutMode.ON_SERVER
        checkoutDir = "git-repos"
        showDependenciesChanges = true
    }
})


object RootProject2 : Project({
    name = "Second Root Project"

    buildType(RootProject2_App02)
    buildType(RootProject2_App01)

    subProject(RootProject2_LocalBuild)
})

object RootProject2_App01 : BuildType({
    name = "App01"
})

object RootProject2_App02 : BuildType({
    name = "App02"
})


object RootProject2_LocalBuild : Project({
    name = "Local Build"

    buildType(RootProject2_LocalBuild_Release001)
})

object RootProject2_LocalBuild_Release001 : BuildType({
    name = "Release 0.0.1"
})

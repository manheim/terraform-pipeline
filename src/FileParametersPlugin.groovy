import static TerraformEnvironmentStage.ALL

import groovy.text.StreamingTemplateEngine

class FileParametersPlugin implements TerraformEnvironmentStagePlugin {
    public static void init() {
        FileParametersPlugin plugin = new FileParametersPlugin()

        TerraformEnvironmentStage.addPlugin(plugin)
    }

    @Override
    public void apply(TerraformEnvironmentStage stage) {
        String environment = stage.getEnvironment()

        stage.decorate(ALL, addEnvironmentSpecificVariables(environment))
    }

    public Closure addEnvironmentSpecificVariables(String environment) {
        String environmentFilename = "${environment}.properties"

        return { closure ->
            if (fileExists(environmentFilename)) {
                echo "Found file: ${environmentFilename} - loading the contents as environment variables."
                String fileContent = readFile(environmentFilename)
                List variables = getVariables(fileContent)

                withEnv(variables) { closure() }
            } else {
                echo "No environment properties file found.  Create a ${environmentFilename} file to add environment-specific variables to this stage."
                closure()
            }
        }
    }

    public List getVariables(String fileContent) {
        return fileContent.split('\\r?\\n').collect { String value -> interpolate(value) }
    }

    public String interpolate(String value) {
        return new StreamingTemplateEngine().createTemplate(value).make([env: getEnv()]).toString()
    }

    public getEnv() {
        return (Jenkinsfile.instance != null) ? Jenkinsfile.instance.getEnv() : [:]
    }
}

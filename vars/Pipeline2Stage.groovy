def call(args) {
    pipeline {
        agent none
        options { preserveStashes() }

        stages {
            stage('1') {
                steps {
                    script {
                        ((Stage)args.getAt(0)).build()
                    }
                }
            }

            stage('2') {
                steps {
                    script {
                        ((Stage)args.getAt(1)).build()
                    }
                }
            }
        }
    }
}

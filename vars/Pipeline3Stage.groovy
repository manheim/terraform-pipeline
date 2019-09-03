def call(args) {
    pipeline {
        agent none

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

            stage('3') {
                steps {
                    script {
                        ((Stage)args.getAt(2)).build()
                    }
                }
            }
        }
    }
}


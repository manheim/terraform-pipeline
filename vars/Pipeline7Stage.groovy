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

            stage('3') {
                steps {
                    script {
                        ((Stage)args.getAt(2)).build()
                    }
                }
            }

            stage('4') {
                steps {
                    script {
                        ((Stage)args.getAt(3)).build()
                    }
                }
            }

            stage('5') {
                steps {
                    script {
                        ((Stage)args.getAt(4)).build()
                    }
                }
            }

            stage('6') {
                steps {
                    script {
                        ((Stage)args.getAt(5)).build()
                    }
                }
            }

            stage('7') {
                steps {
                    script {
                        ((Stage)args.getAt(6)).build()
                    }
                }
            }
        }
    }
}


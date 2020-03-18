def call(closure) {
    closure.delegate = this
    closure.call()
}

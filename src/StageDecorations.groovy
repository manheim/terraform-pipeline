class StageDecorations {
    private Map<String,Closure> decorations = [:]

    public apply(String group = null, Closure innerClosure) {
        def currentDecorations = decorations.get(group) ?: { stage -> stage() }
        currentDecorations.delegate = innerClosure.owner
        currentDecorations(innerClosure)
    }

    public add(String group = null, Closure decoration) {
        def existingDecoration = decorations.get(group) ?: { stage -> stage() }
        def newDecoration = { stage ->
            decoration.delegate = delegate
            decoration() {
                stage.delegate = delegate
                existingDecoration.delegate = delegate
                existingDecoration(stage)
            }
        }

        decorations.put(group, newDecoration)
    }
}

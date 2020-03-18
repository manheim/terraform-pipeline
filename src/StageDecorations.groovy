class StageDecorations {
    private Map<String,Closure> decorations = [:]

    public apply(String group = null, Closure innerClosure) {
        def currentDecorations = decorations.get(group)

        if (currentDecorations != null) {
            currentDecorations(innerClosure)
        } else {
            innerClosure()
        }
    }

    public add(String group = null, Closure decoration) {
        def existingDecoration = decorations.get(group) ?: { stage -> stage() }
        def newDecoration = { stage ->
            decoration() {
                existingDecoration(stage)
            }
        }

        decorations.put(group, newDecoration)
    }
}

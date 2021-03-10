import org.codehaus.groovy.transform.trait.Traits
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.reflections.Reflections
import groovy.transform.Memoized

public class ResetStaticStateExtension implements BeforeEachCallback,
                                                  AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        reset()
    }

    @Override
    public void afterEach(ExtensionContext context) {
        reset()
    }

    public reset() {
        def resetList = findAllResettableClasses()

        resetList.each { needsReset ->
            needsReset.reset()
        }
    }

    @Memoized
    public findAllResettableClasses() {
        // There's an interesting problem here.
        def resettableClasses = new Reflections(Resettable.getPackage().getName()).getSubTypesOf( Resettable.class )
        def traits = resettableClasses.find { Traits.isTrait(it) }
        traits.each {
            resettableClasses += new Reflections(it.getPackage().getName()).getSubTypesOf( it )
            resettableClasses.removeElement(it)
        }
        return resettableClasses
    }
}

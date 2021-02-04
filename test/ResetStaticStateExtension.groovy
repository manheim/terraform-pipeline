import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.reflections.Reflections

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

    public findAllResettableClasses() {
        return new Reflections(Resettable.getPackage().getName()).getSubTypesOf( Resettable.class )
    }
}

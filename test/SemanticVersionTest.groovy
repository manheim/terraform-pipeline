import static org.junit.Assert.assertEquals

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized.class)
class SemanticVersionTest {

    @Parameterized.Parameters
    static data() {
        Arrays.asList( new Object[100][])
    }

    SemanticVersionTest() { }

    @Test
    void sortsCorrectly() {
        List<SemanticVersion> versions = SORTED.clone().collect {v -> new SemanticVersion(v)}
        // Randomize
        Collections.shuffle(versions)
        // Then sort
        versions.sort()

        def result = versions.collect {sv -> sv.version}
        assertEquals(SORTED,result)
    }

    static SORTED = [
            'fixme',
            '0.2-beta',
            '0.1',
            '0.1.0.2',
            '0.1.0.2',
            '0.1.0.10',
            '0.1.0.23',
            '0.2', 
            '0.2.0.3.1', 
            '0.2.0.4',
            '1.0RC1', 
            '1.0RC2', 
            '1.0', 
            '1.0.1.2', 
            '1.0.2', 
            '1.2.2.3',
            '1.2.3', 
            '1.5.2_04', 
            '1.5.2_05', 
            '1.5.2_10', 
            '1.6.0_01',
            '2.0', 
            '2.0.0_02', 
            '3.1'
    ]
}

import java.util.Comparator;

class SemanticVersion implements Comparable<SemanticVersion> {

    private String version
    private List<String> components

    SemanticVersion(String version) {
        this.version = version
        this.components = version.tokenize(/._/)
    }

    String getVersion() {
        this.version
    }
    
    @Override
    int compareTo(SemanticVersion other) {
        for(i in 0..<Math.max(this.components.size(), other.components.size())) {

            if(i == this.components.size()) {
                return other.components[i].isInteger() ? -1 : 1 //1.0 <=> 1.0.1 : 1.0.1 <=> 1.0.1-rc1
            } else if (i == other.components.size()) {
                return this.components[i].isInteger() ? 1 : -1 //1.0.1 <=> 1.0 : 1.0.1-rc1 <=> 1.0.1
            }

            if(this.components[i].isInteger() && other.components[i].isInteger()) { //1.0 <=> 1.1 or //1.1 <=> 1.0
                int diff = (this.components[i] as int) <=> (other.components[i] as int)
                if(diff != 0) {
                    return diff
                }
            } else if(this.components[i].isInteger()) { //1.0.3.4 <=> 1.0.3.4b goes to the former. Hmm.
                return 1
            } else if(other.components[i].isInteger()) { //1.0.3.4-rc3 <=> 1.0.3.4 goes to the later.
                return -1
            } else {
                int diff = this.components[i] <=> other.components[i] //1.0.3.3 <=> 1.0.3.4 works at least. :-/
                if(diff != 0) {
                    return diff
                }
            }
        }
        return 0
    }
}

import java.io.Serializable;

class Pair<F,  S> implements Serializable{

    private F first;
    private S second;

    /**
     * Creates a new Pair object compound by two objects:
     *
     * 1- First: the first object of the Pair.
     * 2- Second: the second object of the Pair.
     */
    Pair(F first, S second) {

        this.first = first;
        this.second = second;
    }

    /**
     *
     * @param first first member of the pair to set.
     */
    void setFirst(F first) {
        this.first = first;
    }

    /**
     *
     * @param second second member of the pair to set.
     */
    void setSecond(S second) {
        this.second = second;
    }

    /**
     * @return the first member of the pair.
     */
    F getFirst() { return first; }

    /**
     * @return the second member of the pair.
     */
    S getSecond() {
        return second;
    }
}

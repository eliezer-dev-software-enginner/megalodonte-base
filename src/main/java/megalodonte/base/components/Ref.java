package megalodonte.base.components;

public class Ref<T extends Component> {
    private T current;

    public Ref() {
    }

    public Ref(T initial) {
        this.current = initial;
    }

    public T current() {
        return current;
    }

    public void setCurrent(T component) {
        this.current = component;
    }
}

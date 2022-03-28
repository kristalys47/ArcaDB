package orc.helper.classes;

public class HashNode<T> {
    private HashNode<T> next;
    private HashNode<T> prev;
    private T element;

    public HashNode(T element, HashNode next) {
        this.next = next;
        this.element = element;
    }

    public HashNode<T> getPrev() {
        return prev;
    }

    public void setPrev(HashNode<T> prev) {
        this.prev = prev;
    }

    public HashNode<T> getNext() {
        return next;
    }

    public void setNext(HashNode<T> next) {
        this.next = next;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }
}

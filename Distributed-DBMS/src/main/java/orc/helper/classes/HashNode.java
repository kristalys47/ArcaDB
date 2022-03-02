package orc.helper.classes;

public class HashNode<T> {
    private HashNode next;
    private HashNode prev;
    private T element;

    public HashNode(T element, HashNode next) {
        this.next = next;
        this.element = element;
    }

    public HashNode getPrev() {
        return prev;
    }

    public void setPrev(HashNode prev) {
        this.prev = prev;
    }

    public HashNode getNext() {
        return next;
    }

    public void setNext(HashNode next) {
        this.next = next;
    }

    public T getElement() {
        return element;
    }

    public void setElement(T element) {
        this.element = element;
    }
}

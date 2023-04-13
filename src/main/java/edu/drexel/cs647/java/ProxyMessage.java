package edu.drexel.cs647.java;

public sealed interface ProxyMessage {
    // Recent versions of Java have *record* classes: https://docs.oracle.com/en/java/javase/15/language/records.html
    // Basically, record classes automatically get private fields to store their constructor arguments, and getter methods (named after the constructor arguement) are automatically generated
    public final record Ack(String msg) implements ProxyMessage {}
    public final record Repeat() implements ProxyMessage {}
    public final record Request(String msg) implements ProxyMessage{}
    public final record Shutdown() implements ProxyMessage {}
}
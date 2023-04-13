package edu.drexel.cs647.java;

public sealed interface SbtRepro {
    public final record R(String msg) implements SbtRepro {}
}
package com.chao.failure.condition;

import java.util.function.Consumer;

/**
 * Predicate class is a utility class for building and evaluating boolean expressions.
 * It supports combining multiple boolean values through AND and OR operations, and provides a chained way to build complex boolean expressions.
 *
 * @author Kyrie Chao
 * @version 1.3.0
 */
public final class Predicate {

    // Root node for storing the structure of boolean expressions
    private Node root;

    /**
     * Default constructor, creates an empty Predicate object.
     */
    public Predicate() {
    }

    /**
     * Private constructor, used to create a Predicate object with a specified root node.
     * @param root Root node of the boolean expression
     */
    private Predicate(Node root) {
        this.root = root;
    }

    /**
     * Create a Predicate object representing the specified boolean value.
     * @param value Boolean value to represent
     * @return Predicate object containing the specified value
     */
    public static Predicate of(boolean value) {
        return new Predicate(new Value(value));
    }

    /**
     * Create a Predicate object representing the AND combination of all given boolean values.
     * @param values Array of boolean values to combine
     * @return Predicate object representing the AND combination of all boolean values
     */
    public static Predicate allOf(boolean... values) {
        Predicate c = new Predicate();
        if (values == null) return c;
        for (boolean v : values) {
            c = c.and(v);
        }
        return c;
    }

    /**
     * Create a Predicate object representing the OR combination of all given boolean values.
     * @param values Array of boolean values to combine
     * @return Predicate object representing the OR combination of all boolean values
     */
    public static Predicate anyOf(boolean... values) {
        Predicate c = new Predicate();
        if (values == null) return c;
        boolean first = true;
        for (boolean v : values) {
            if (first) {
                c = c.and(v);
                first = false;
            } else {
                c = c.or(v);
            }
        }
        return c;
    }

    /**
     * Create a Predicate object and configure it through a Consumer builder.
     * @param builder Consumer for configuring the Predicate
     * @return Predicate object configured according to the builder
     */
    public static Predicate group(Consumer<Predicate> builder) {
        Predicate c = new Predicate();
        if (builder != null) {
            builder.accept(c);
        }
        return c;
    }

    /**
     * Perform AND operation between current Predicate and specified boolean value.
     * @param value Boolean value to perform AND operation with
     * @return New Predicate object after AND operation
     */
    public Predicate and(boolean value) {
        return and(new Value(value));
    }

    /**
     * Perform OR operation between current Predicate and specified boolean value.
     * @param value Boolean value to perform OR operation with
     * @return New Predicate object after OR operation
     */
    public Predicate or(boolean value) {
        return or(new Value(value));
    }

    /**
     * Perform AND operation between current Predicate and another Predicate.
     * @param other Another Predicate to perform AND operation with
     * @return New Predicate object after AND operation
     */
    public Predicate and(Predicate other) {
        if (other == null || other.root == null) return this;
        return and(other.root);
    }

    /**
     * Perform OR operation between current Predicate and another Predicate.
     * @param other Another Predicate to perform OR operation with
     * @return New Predicate object after OR operation
     */
    public Predicate or(Predicate other) {
        if (other == null || other.root == null) return this;
        return or(other.root);
    }

    /**
     * Evaluate the value of current boolean expression.
     * @return Calculation result of the boolean expression
     */
    public boolean evaluate() {
        return root == null || root.eval();
    }

    /**
     * Perform AND operation between current Predicate and specified node.
     * @param node Node to perform AND operation with
     * @return New Predicate object after AND operation
     */
    private Predicate and(Node node) {
        if (node == null) return this;
        Node newRoot = (root == null) ? node : new And(root, node);
        return new Predicate(newRoot);
    }

    /**
     * Perform OR operation between current Predicate and specified node.
     * @param node Node to perform OR operation with
     * @return New Predicate object after OR operation
     */
    private Predicate or(Node node) {
        if (node == null) return this;
        Node newRoot = (root == null) ? node : new Or(root, node);
        return new Predicate(newRoot);
    }

    /**
     * Node interface is the base interface for all nodes in the boolean expression tree.
     * It uses the sealed keyword to restrict only Value, And, and Or to implement it.
     */
    private sealed interface Node permits Value, And, Or {
        boolean eval();
    }

    /**
     * Value class represents a boolean value node.
     * It is a record type used to store a boolean value and implement the Node interface.
     */
    private record Value(boolean value) implements Node {
        @Override
        public boolean eval() {
            return value;
        }
    }

    /**
     * And class represents an AND operation node.
     * It is a record type that stores left and right child nodes, and performs their AND operation in the eval method.
     */
    private record And(Node left, Node right) implements Node {
        @Override
        public boolean eval() {
            return left.eval() && right.eval();
        }
    }

    /**
     * Or class represents an OR operation node.
     * It is a record type that stores left and right child nodes, and performs their OR operation in the eval method.
     */
    private record Or(Node left, Node right) implements Node {
        @Override
        public boolean eval() {
            return left.eval() || right.eval();
        }
    }
}

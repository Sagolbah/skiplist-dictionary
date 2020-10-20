package ru.ifmo.crypto.skiplist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IntSkipList implements SkipList<Integer> {
    private final Random rng = new Random();
    private List<Node> layers;

    public IntSkipList() {
        layers = new ArrayList<>();
        layers.add(makeInfinityPair());
    }

    public IntSkipList(final List<Integer> source) {
        this();
        build(source);
    }

    private void build(final List<Integer> source) {
        source.forEach(this::insertToBottom);
        Node lastLayer = layers.get(layers.size() - 1);
        while (!isLayerEmpty(lastLayer)) {
            boolean changed = false;
            Node nextLayer = makeInfinityPair();
            nextLayer.setDown(lastLayer);  // Link left infinity
            Node lastInLayer = nextLayer;
            Node cur = lastLayer.right;
            while (cur.right != null) {
                if (rng.nextBoolean()) {  // Keep alive
                    Node newNode = new Node(cur.getData(), lastInLayer.right, cur);
                    lastInLayer.setRight(newNode);
                    lastInLayer = lastInLayer.right;
                } else {
                    changed = true;
                }
                cur = cur.right;
            }
            lastInLayer.getRight().setDown(cur);  // Link right infinity
            if (changed) {
                layers.add(nextLayer);
            }
            lastLayer = layers.get(layers.size() - 1);
        }
    }

    private void insertToBottom(final int key) {
        Node cur = layers.get(0);
        while (cur.right.getData() < key) {
            cur = cur.right;
        }
        cur.right = new Node(key, cur.right, null);
    }

    private Node makeInfinityPair() {
        Node rightSentinel = new Node(Integer.MAX_VALUE);
        return new Node(Integer.MIN_VALUE, rightSentinel, null);
    }

    private boolean isLayerEmpty(final Node beginning) {
        return beginning.right.getData() == Integer.MAX_VALUE;
    }

    @Override
    public boolean find(Integer key) {
        Node cur = layers.get(layers.size() - 1);
        while (true) {
            while (cur.right.getData() < key) {
                cur = cur.right;
            }
            if (cur.getDown() == null) {
                return cur.right.getData() == key;
            }
            cur = cur.getDown();
        }
    }

    @Override
    public void insert(Integer elem) {
        // NOTE: Currently we assume that our skip list stores unique items.
        if (find(elem)) {
            return;
        }
        Node lastLayer = layers.get(layers.size() - 1);
        insertImpl(lastLayer, elem);
        if (!isLayerEmpty(lastLayer)) {
            Node newLayer = makeInfinityPair();
            newLayer.setDown(lastLayer);
            newLayer.right.setDown(lastLayer.getRight().getRight());
            layers.add(newLayer);
        }
    }

    private Node insertImpl(Node cur, int key) {
        while (cur.getRight().getData() < key) {
            cur = cur.right;
        }
        if (cur.getDown() == null) {
            cur.setRight(new Node(key, cur.getRight(), null));
        } else {
            Node res = insertImpl(cur.getDown(), key);
            if (res != null) {
                cur.setRight(new Node(key, cur.getRight(), res));
            } else {
                return null;
            }
        }
        return rng.nextBoolean() ? cur.getRight() : null;
    }

    @Override
    public void delete(Integer elem) {
        if (!find(elem)) {
            return;
        }
        Node cur = layers.get(layers.size() - 1);
        while (true) {
            while (cur.getRight().getData() < elem) {
                cur = cur.right;
            }
            if (cur.down == null) {
                // Deleting cur.right
                cur.setRight(cur.getRight().getRight());
                break;
            }
            cur = cur.down;
        }
    }

    private static class Node {
        private final int data;
        private Node right = null;
        private Node down = null;

        Node(final int data) {
            this.data = data;
        }

        Node(final int data, final Node right, final Node down) {
            this.data = data;
            this.right = right;
            this.down = down;
        }


        public Node getRight() {
            return right;
        }

        public void setRight(Node right) {
            this.right = right;
        }

        public Node getDown() {
            return down;
        }

        public void setDown(Node down) {
            this.down = down;
        }

        public int getData() {
            return data;
        }
    }


}

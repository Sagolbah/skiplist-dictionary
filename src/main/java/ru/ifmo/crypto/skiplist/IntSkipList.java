package ru.ifmo.crypto.skiplist;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IntSkipList implements SkipList<Integer> {
    private final Random rng = new Random();
    private static final byte[] NIL = new byte[]{};
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
        while (isLayerNonEmpty(lastLayer)) {
            boolean changed = false;
            Node nextLayer = makeInfinityPair();
            lastLayer.setPlateau(false);
            nextLayer.setDown(lastLayer);  // Link left infinity
            Node lastInLayer = nextLayer;
            Node cur = lastLayer.right;
            while (cur.right != null) {
                if (rng.nextBoolean()) {  // Keep alive
                    Node newNode = new Node(cur.getData(), lastInLayer.right, cur);
                    lastInLayer.setRight(newNode);
                    lastInLayer = lastInLayer.right;
                    cur.setPlateau(false);
                } else {
                    changed = true;
                }
                cur = cur.right;
            }
            lastInLayer.getRight().setDown(cur);  // Link right infinity
            cur.setPlateau(false);
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

    private boolean isLayerNonEmpty(final Node beginning) {
        return beginning.right.getData() != Integer.MAX_VALUE;
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
        if (isLayerNonEmpty(lastLayer)) {
            Node newLayer = makeInfinityPair();
            lastLayer.setPlateau(false);
            lastLayer.getRight().getRight().setPlateau(false);
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
                res.setPlateau(false);
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

    public Proof makeProof(final int key) {
        List<Node> pList = new ArrayList<>();
        Node cur = layers.get(layers.size() - 1);
        pList.add(cur);
        while (true) {
            while (cur.right.getData() <= key) {
                cur = cur.right;
                pList.add(cur);
            }
            if (cur.getDown() == null) {
                break;
            }
            cur = cur.getDown();
            pList.add(cur);
        }
        Collections.reverse(pList);
        List<byte[]> qList = new ArrayList<>();
        // Creating Q array with proof
        Node cur_w = pList.get(0).getRight();
        if (cur_w.isPlateau()) {
            qList.add(cur_w.getHash());
        } else {
            if (cur_w.getRight() == null) {
                qList.add(new byte[]{});
            } else {
                qList.add(intToBytes(cur_w.getData()));
            }
        }
        qList.add(intToBytes(cur.getData()));
        for (int i = 1; i < pList.size() - 1; i++) {
            Node cur_v = pList.get(i);
            cur_w = cur_v.getRight();
            if (cur_w.isPlateau()) {
                if (cur_w != pList.get(i - 1)) {
                    qList.add(cur_w.getHash());
                } else {
                    if (cur_v.getDown() == null) {
                        qList.add(intToBytes(cur_v.getData()));
                    } else {
                        qList.add(cur_v.getDown().getHash());
                    }
                }
            }
        }
        return new Proof(qList);
    }

    public Confirmation getConfirmation() {
        return new Confirmation(calcHash(layers.get(layers.size() - 1)));
    }

    private byte[] calcHash(Node v) {
        Node nxt = v.getRight();
        Node dwn = v.getDown();
        if (v.getRight() == null) {
            return new byte[]{};
        }
        if (dwn == null) {
            if (nxt.isPlateau()) {
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), calcHash(nxt)));
            } else {
                byte[] newBytes = (nxt.getRight() == null) ? NIL : intToBytes(nxt.getData());
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), newBytes));
            }
            return v.getHash();
        }
        if (nxt.isPlateau()) {
            v.setHash(CommutativeHashing.SHA256(calcHash(dwn), calcHash(nxt)));
        } else {
            v.setHash(calcHash(dwn));
        }
        return v.getHash();
    }

    private byte[] intToBytes(final int x) {
        return ByteBuffer.allocate(4).putInt(x).array();
    }

    private static class Node {
        private final int data;
        private Node right = null;
        private Node down = null;
        private boolean isPlateau = true;
        private byte[] hash = new byte[]{};

        public byte[] getHash() {
            return hash;
        }

        public void setHash(byte[] hash) {
            this.hash = hash;
        }

        Node(final int data) {
            this.data = data;
        }

        Node(final int data, final Node right, final Node down) {
            this.data = data;
            this.right = right;
            this.down = down;
        }

        public boolean isPlateau() {
            return isPlateau;
        }

        public void setPlateau(boolean plateau) {
            isPlateau = plateau;
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

package ru.ifmo.crypto.skiplist;

import java.nio.ByteBuffer;
import java.util.*;

/**
 * Implementation for {@link AuthDict} interface of {@link Integer} elements
 *
 * @author Daniil Boger (Sagolbah)
 */
public class IntAuthDict implements AuthDict<Integer> {
    private final Random rng = new Random();
    private static final byte[] NIL = new byte[]{};
    private long lastChangeTimestamp = 0;
    private Node root;

    /**
     * Creates empty {@link IntAuthDict}
     */
    public IntAuthDict() {
        init();
        createHashes(root);
    }

    private void init() {
        root = makeInfinityPair();
    }

    /**
     * Creates {@link IntAuthDict} with given values
     *
     * @param source list of initial values
     */
    public IntAuthDict(final List<Integer> source) {
        init();
        build(source);
    }

    private void build(final List<Integer> source) {
        buildBottom(new ArrayList<>(source));
        Node lastLayer = root;
        while (isLayerNonEmpty(lastLayer)) {
            boolean changed = false;
            Node nextLayer = makeInfinityPair();
            lastLayer.setPlateau(false);
            nextLayer.setDown(lastLayer);  // Link left infinity
            Node lastInLayer = nextLayer;
            Node cur = lastLayer.getRight();
            while (cur.getRight() != null) {
                if (rng.nextBoolean()) {  // Keep alive
                    Node newNode = new Node(cur.getData(), lastInLayer.right, cur);
                    lastInLayer.setRight(newNode);
                    lastInLayer = lastInLayer.right;
                    cur.setPlateau(false);
                } else {
                    cur.setPlateau(true);
                    changed = true;
                }
                cur = cur.getRight();
            }
            lastInLayer.getRight().setDown(cur);  // Link right infinity
            cur.setPlateau(false);
            if (changed) {
                root = nextLayer;
            }
            lastLayer = root;
        }
        createHashes(lastLayer);
    }

    private void buildBottom(final List<Integer> source) {
        Collections.sort(source);
        Node cur = root;
        for (int key : source) {
            cur.setRight(new Node(key, cur.getRight(), null));
            cur = cur.getRight();
        }
    }


    private Node makeInfinityPair() {
        Node rightSentinel = new Node(Integer.MAX_VALUE);
        return new Node(Integer.MIN_VALUE, rightSentinel, null);
    }

    private boolean isLayerNonEmpty(final Node beginning) {
        return beginning.right.getData() != Integer.MAX_VALUE;
    }

    // Public - for testing purposes only.
    public boolean find(Integer key) {
        Node cur = root;
        while (true) {
            while (cur.getRight().getData() < key) {
                cur = cur.getRight();
            }
            if (cur.getDown() == null) {
                return cur.getRight().getData() == key;
            }
            cur = cur.getDown();
        }
    }

    /**
     * Inserts key in {@link IntAuthDict} and rehashes skip list
     *
     * @param elem element for inserting
     */
    @Override
    public void insert(Integer elem) {
        if (find(elem)) {
            return;
        }
        Node lastLayer = root;
        List<Node> backtrack = new ArrayList<>();
        insertImpl(lastLayer, elem, backtrack);
        if (isLayerNonEmpty(lastLayer)) {
            Node newLayer = makeInfinityPair();
            lastLayer.setPlateau(false);
            lastLayer.getRight().getRight().setPlateau(false);
            newLayer.setDown(lastLayer);
            newLayer.getRight().setDown(lastLayer.getRight().getRight());
            doBacktracking(backtrack);
            recalcHash(newLayer);
            root = newLayer;
        } else {
            doBacktracking(backtrack);
        }
        lastChangeTimestamp++;
    }

    private void doBacktracking(List<Node> backtrack) {
        for (int i = backtrack.size() - 1; i >= 0; i--) {
            Node rec = backtrack.get(i);
            if (rec.getRight() != null) {
                recalcHash(rec.getRight());
            }
            recalcHash(rec);
        }
    }


    private Node insertImpl(Node cur, int key, List<Node> backtrack) {
        backtrack.add(cur);
        while (cur.getRight().getData() < key) {
            cur = cur.getRight();
            backtrack.add(cur);
        }
        if (cur.getDown() == null) {
            cur.setRight(new Node(key, cur.getRight(), null));
        } else {
            Node res = insertImpl(cur.getDown(), key, backtrack);
            if (res != null) {
                cur.setRight(new Node(key, cur.getRight(), res));
                res.setPlateau(false);
            } else {
                return null;
            }
        }
        return rng.nextBoolean() ? cur.getRight() : null;
    }

    /**
     * Removes key in {@link IntAuthDict}. If key is not in the skip list, nothing happens.
     *
     * @param elem element for deleting
     */
    @Override
    public void delete(Integer elem) {
        if (!find(elem)) {
            return;
        }
        Node cur = root;
        Deque<Node> backtrack = new ArrayDeque<>();
        while (true) {
            backtrack.push(cur);
            while (cur.getRight().getData() < elem) {
                cur = cur.getRight();
                backtrack.push(cur);
            }
            if (cur.getDown() == null) {
                // Deleting cur.right
                cur.setRight(cur.getRight().getRight());
                recalcHash(cur);
                break;
            }
            cur = cur.getDown();
        }
        while (!backtrack.isEmpty()) {
            Node nextNode = backtrack.pop();
            if (nextNode.getRight().getData() == elem) {
                nextNode.setRight(nextNode.getRight().getRight());
            }
            recalcHash(nextNode);
        }
        lastChangeTimestamp++;
    }

    /**
     * Creates {@link Proof} for given key.
     *
     * @param key key for proof generation
     * @return {@link Proof} for given key
     */
    public Proof makeProof(final Integer key) {
        List<Node> pList = new ArrayList<>();
        Node cur = root;
        pList.add(cur);
        while (true) {
            while (cur.getRight().getData() <= key) {
                cur = cur.getRight();
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
        boolean isPresent = cur.getData() == key;
        if (cur_w.isPlateau()) {
            qList.add(cur_w.getHash());
        } else {
            if (cur_w.getRight() == null) {
                qList.add(NIL);
            } else {
                qList.add(intToBytes(cur_w.getData()));
            }
        }
        qList.add(intToBytes(cur.getData()));
        for (int i = 1; i < pList.size(); i++) {
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
        return new Proof(key, lastChangeTimestamp, qList, isPresent);
    }

    private void recalcHash(final Node v) {
        if (v.getRight() == null) {
            v.setHash(NIL);
            return;
        }
        if (v.getDown() == null) {
            if (v.getRight().isPlateau()) {
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), v.getRight().getHash()));
            } else {
                byte[] newBytes = (v.getRight().getRight() == null) ? NIL : intToBytes(v.getRight().getData());
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), newBytes));
            }
        } else {
            if (!v.getRight().isPlateau()) {
                v.setHash(v.getDown().getHash());
            } else {
                v.setHash(CommutativeHashing.SHA256(v.getDown().getHash(), v.getRight().getHash()));
            }
        }
    }

    /**
     * Creates most up-to-date confirmation of {@link IntAuthDict}
     *
     * @return {@link Confirmation} of given list
     */
    public Confirmation getConfirmation() {
        //return new Confirmation(createHashes(layers.get(layers.size() - 1)));
        return new Confirmation(lastChangeTimestamp, root.getHash());
    }

    private byte[] createHashes(Node v) {
        Node nxt = v.getRight();
        Node dwn = v.getDown();
        if (v.getRight() == null) {
            return NIL;
        }
        if (dwn == null) {
            if (nxt.isPlateau()) {
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), createHashes(nxt)));
            } else {
                byte[] newBytes = (nxt.getRight() == null) ? NIL : intToBytes(nxt.getData());
                v.setHash(CommutativeHashing.SHA256(intToBytes(v.getData()), newBytes));
            }
            return v.getHash();
        }
        if (nxt.isPlateau()) {
            v.setHash(CommutativeHashing.SHA256(createHashes(dwn), createHashes(nxt)));
        } else {
            v.setHash(createHashes(dwn));
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
        private boolean isPlateau = true;  // v is Plateau <=> there is no such element k that down(k) = v
        private byte[] hash = NIL;

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

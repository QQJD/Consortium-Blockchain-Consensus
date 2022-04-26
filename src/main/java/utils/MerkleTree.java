package utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MerkleTree {

    /**
     * merkle树深度
     */
    private byte depth;
    /**
     * merkle树节点数组，每个节点的值为byte[]类型，按层序遍历顺序索引存储
     */
    private byte[][] tree;

    /**
     * 根据需要被包含的内容构造merkle树
     * @param leafs 需要被包含进merkle树的内容列表
     * @param digestAlgorithm 摘要算法
     */
    public MerkleTree(byte[][] leafs, String digestAlgorithm) {

        // 计算merkle树的深度
        depth = 1;
        int temp = leafs.length - 1;
        while (temp > 0) {
            temp >>= 1;
            depth++;
        }

        // 如果叶节点数量不正好是2^n，补齐到2^n个，以便merkle树父节点的哈希计算
        int filledLen = 1 << depth - 1;
        byte[][] filledLeafs = new byte[filledLen][leafs[0].length];
        for (int i = 0; i < filledLen; i++) {
            if (i < leafs.length) filledLeafs[i] = leafs[i];
            else filledLeafs[i] = new byte[leafs[0].length];
        }

        // 计算merkle树最末层节点，其值为对应的filledLeaf元素哈希
        int treeLen = filledLen * 2 - 1;
        tree = new byte[treeLen][32];
        for (int i = treeLen / 2; i < treeLen; i++) {
            tree[i] = CryptoUtils.digest(digestAlgorithm, filledLeafs[i - treeLen / 2]);
        }

        // 自底向上计算merkle树节点值
        for (int i = depth - 1; i >= 1; i--) {
            for (int j = (1 << i - 1) - 1; j < (1 << i) - 1; j++) {
                byte[] combined = new byte[32];
                for (int k = 0; k < 32; k++) {
                    // 两hash相加的方式：按位与
                    combined[k] = (byte) (tree[2 * j + 1][k] & tree[2 * j + 2][k]);
                }
                tree[j] = CryptoUtils.digest(digestAlgorithm, combined);
            }
        }

    }

    /**
     * 获取proof
     * @param index 被包含内容的索引
     * @return 能够证明内容被包含在merkle树中的proof
     */
    public byte[][] getProof(int index) {

        // proof路径长度为merkle树深度，除去根节点需要减1
        byte[][] proof = new byte[depth - 1][32];

        // eg.对于深度为4的merkle树，叶节点索引0~7，对应到tree中的索引就是7~14，因为tree中包含父节点，需要转换一下索引
        index = index + (1 << depth - 1) - 1;

        // 构造proof
        for (int i = 0; i < depth - 1; i++) {
            // 如果index是偶数，那么需要的proof就是左边的节点
            if ((index & 1) == 0) {
                proof[i] = tree[index - 1];
                index = (index >> 1) - 1;
            }
            // 如果index是奇数，那么需要的proof就是右边的节点
            else {
                proof[i] = tree[index + 1];
                index = index >> 1;
            }
        }

        return proof;

    }

    /**
     * 获取merkle树根节点
     * @return merkle树根节点
     */
    public byte[] getRoot() {
        return tree[0];
    }

    /**
     * 验证merkle树proof的正确性
     * @param digestAlgorithm 摘要算法
     * @param data 待验证是否被包含在merkle树中的内容
     * @param root merkle树根节点
     * @param proof 证明路径
     * @return proof是否有效
     */
    public static boolean isValidProof(String digestAlgorithm, byte[] data, byte[] root, byte[][] proof) {

        // data取哈希后，按proof路径依次合并（按位与）并取哈希，最后得到根哈希值
        byte[] curr = CryptoUtils.digest(digestAlgorithm, data);
        for (int i = 0; i < proof.length; i++) {
            for (int j = 0; j < 32; j++) {
                curr[j] = (byte) (curr[j] & proof[i][j]);
            }
            curr = CryptoUtils.digest(digestAlgorithm, curr);
        }

        // 将计算所得的根哈希值与消息给出的根哈希值比较，若相等则表示proof正确
        for (int i = 0; i < 32; i++) {
            if (curr[i] != root[i]) return false;
        }
        return true;

    }

    @Override
    public String toString() {
        return "MerkleTree{" +
                "depth=" + depth +
                ", tree=" + "byte[" + tree.length + "]" + "[" + tree[0].length + "]" +
                '}';
    }

}

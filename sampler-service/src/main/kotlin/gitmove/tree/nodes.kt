package gitmove.tree

class Ref(
    val name: String,
    val commit: Commit
)

class Commit(val name: String, val node: Node, val parent: Commit? = null)

sealed class Node(
    val name: String
)

class Tree(
    name: String,
    val children: Map<String, Node>
) : Node(name)

class Blob(name: String) : Node(name)

fun main() {
    val repo = Ref(
            "master",
            Commit(
                    "init",
                    Blob("readme.md")
            )
    )
}

fun extractPath(tree: Tree, dir: String, other: String?): Node? {
    val node = tree.children[dir]
    return when (node) {
        null -> null
        is Tree -> {
            if (other == null) {
                node
            } else {
                val (_dir, _other) = other.split("/", limit = 2)
                extractPath(node, _dir, _other)
            }
        }
        is Blob -> if (other == null) node else null
    }
}
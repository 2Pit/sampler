package gitmove.tree

class Ref(val name: String, val commit: Commit)
class Commit(val message: String, val node: Node, val parent: List<Commit>)

sealed class Node(val name: String)
class Tree(name: String, val children: Map<String, Node>) : Node(name)
class Blob(name: String) : Node(name)

fun main() {
    val repo = Ref(
            "master",
            Commit(
                    "init",
                    Blob("readme.md"),
                    emptyList()
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
                val split = other.split("/", limit = 2)
                extractPath(node, split.first(), if (split.size == 1) null else split[1])
            }
        }
        is Blob -> if (other == null) node else null
    }
}
package com.github.lure0xaos.util.xml

import com.github.lure0xaos.util.log.Log
import org.w3c.dom.Attr
import org.w3c.dom.Comment
import org.w3c.dom.Element
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.w3c.dom.Text

inline fun <reified N : Node> NodeList.findChildren(): List<N> =
    (0 until length).map { item(it) }.filterIsInstance<N>()

inline fun <reified N : Node> NodeList.forEachIndexed(action: (index: Int, node: N) -> Unit) {
    var n = 0
    for (i: Int in (0 until length)) {
        val item = item(i)
        if (item is N) {
            action(n, item)
            n++
        }
    }
}

inline fun <reified N : Node> Node.findChildren(tagName: String): List<N> =
    (0 until childNodes.length).map { childNodes.item(it) }.filterIsInstance<N>().filter { tagName == it.nodeName }

inline fun <reified N : Node> Node.findFistChild(tagName: String): N? =
    findChildren<N>(tagName).firstOrNull()


fun Element.getTextChildOrAttribute(name: String): String =
    findFistChild<Element>(name)?.textContent?.trim() ?: getAttribute(name)

fun Element.getTextChildOrAttribute(name: String, def: String): String =
    getTextChildOrAttribute(name).ifEmpty { def }

fun Node.text(join: String = NL): String =
    childNodes.findChildren<Node>().joinToString(join) { node: Node ->
        when (node) {
            is Text -> node.wholeText
            is Comment -> ""
            else -> buildString {
                append(TAG_OPEN)
                append(node.nodeName)
                node.attributes?.forEach { attr: Attr ->
                    append(" ")
                    append(attr.name)
                    append(EQ)
                    append(QUOTE)
                    append(attr.value)
                    append(QUOTE)
                }
                append(TAG_CLOSE)
                append(node.text(join))
                append(TAG_OPEN)
                append(SLASH)
                append(node.nodeName)
                append(TAG_CLOSE)
            }.also {
                Log.debug { it }
            }
        }
    }

private fun <R : Any> NamedNodeMap.map(action: (node: Attr) -> R): List<R> =
    (0 until length).map { item(it) as Attr }.map { action(it) }

private fun NamedNodeMap.forEach(action: (node: Attr) -> Unit) {
    for (i: Int in (0 until length)) {
        val attr: Attr = item(i) as Attr
        action(attr)
    }
}

private fun NamedNodeMap.forEachIndexed(action: (index: Int, node: Attr) -> Unit) {
    for (i: Int in (0 until length)) {
        val attr: Attr = item(i) as Attr
        action(i, attr)
    }
}

fun Node.textTrim(join: String = NL): String =
    text(join).trimIndent().trim()

private const val NL = "\n"
private const val TAG_OPEN = '<'
private const val TAG_CLOSE = '>'
private const val SLASH = '/'
private const val EQ = '='
private const val QUOTE = '\"'

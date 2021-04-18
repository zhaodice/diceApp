package org.mirai.zhao.dice

class SentencesThem internal constructor(var tag: String, var tag_view: String) {
    override fun toString(): String {
        return tag_view
    }
}
package net.mamoe.mirai.console.compiler.common

/**
 * @suppress 这是内部 API. 可能在任意时刻变动
 */
object CheckerConstants {
    @JvmField
    public val PLUGIN_ID_REGEX: Regex = Regex("""([a-zA-Z]\w*(?:\.[a-zA-Z]\w*)*)\.([a-zA-Z]\w*(?:-\w+)*)""")
    @JvmField
    public val PLUGIN_FORBIDDEN_NAMES: Array<String> = arrayOf("main", "console", "plugin", "config", "data")
}
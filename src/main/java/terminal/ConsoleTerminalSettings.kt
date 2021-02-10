/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
/*
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

package terminal

import java.util.*

@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
@MustBeDocumented
annotation class ConsoleTerminalExperimentalApi

@ConsoleTerminalExperimentalApi
object ConsoleTerminalSettings {
    @JvmField
    var setupAnsi: Boolean =
            System.getProperty("os.name")?.
            toLowerCase(Locale.ROOT)?.
            contains("windows") // Just for Windows
            ?: false

    @JvmField
    var noConsole: Boolean = false

    @JvmField
    var noAnsi: Boolean = false

    @JvmField
    var noConsoleSafeReading: Boolean = false

    @JvmField
    var noConsoleReadingReplacement: String = ""
}
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
        "INVISIBLE_MEMBER",
        "INVISIBLE_REFERENCE",
        "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
        "INVISIBLE_SETTER",
        "INVISIBLE_GETTER",
        "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
        "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER_WARNING",
        "EXPOSED_SUPER_CLASS"
)
@file:OptIn(ConsoleInternalApi::class, ConsoleFrontEndImplementation::class, ConsoleTerminalExperimentalApi::class)

package terminal


import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleFrontEndDescription
import net.mamoe.mirai.console.MiraiConsoleImplementation
import net.mamoe.mirai.console.data.MultiFilePluginDataStorage
import net.mamoe.mirai.console.data.PluginDataStorage
import net.mamoe.mirai.console.plugin.jvm.JvmPluginLoader
import net.mamoe.mirai.console.plugin.loader.PluginLoader
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.utils.*
import org.fusesource.jansi.Ansi
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.impl.completer.NullCompleter
import org.jline.terminal.Terminal
import org.jline.terminal.TerminalBuilder
import org.jline.terminal.impl.AbstractWindowsTerminal
import org.mirai.zhao.dice.AppContext
import org.mirai.zhao.dice.console.AndroidLoginSolver
import org.mirai.zhao.dice.console.AndroidMiraiLogger
import terminal.noconsole.AllEmptyLineReader
import terminal.noconsole.NoConsole
import java.nio.file.Path
import java.nio.file.Paths


/**
 * mirai-console-terminal 后端实现
 *
 * @see MiraiConsoleTerminalLoader CLI 入口点
 */
@ConsoleExperimentalApi
class MiraiConsoleImplementationTerminal
@RequiresApi(Build.VERSION_CODES.O)
@Suppress("MISSING_DEPENDENCY_CLASS")
@JvmOverloads constructor(
        override val rootPath: Path = Paths.get(AppContext.miraiDir).toAbsolutePath(),
        override val builtInPluginLoaders: List<Lazy<PluginLoader<*, *>>> = listOf(lazy { JvmPluginLoader }),
        override val frontEndDescription: MiraiConsoleFrontEndDescription = ConsoleFrontEndDescImpl,
        override val consoleCommandSender: MiraiConsoleImplementation.ConsoleCommandSenderImpl = ConsoleCommandSenderImplTerminal,
        override val dataStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
        override val dataStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("data")),
        override val configStorageForJvmPluginLoader: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
        override val configStorageForBuiltIns: PluginDataStorage = MultiFilePluginDataStorage(rootPath.resolve("config")),
) : MiraiConsoleImplementation, CoroutineScope by CoroutineScope(
        NamedSupervisorJob("MiraiConsoleImplementationTerminal") +
                CoroutineExceptionHandler { coroutineContext, throwable ->
                    if (throwable is CancellationException) {
                        return@CoroutineExceptionHandler
                    }
                    val coroutineName = coroutineContext[CoroutineName]?.name ?: "<unnamed>"
                    MiraiConsole.mainLogger.error("Exception in coroutine $coroutineName", throwable)
                }) {
    override val consoleInput: ConsoleInput get() = ConsoleInputImpl

    override fun createLoginSolver(requesterBot: Long, configuration: BotConfiguration): LoginSolver {
        //return DefaultLoginSolver(input = { requestInput("LOGIN> ") })
        return AndroidLoginSolver()
    }

    @MiraiInternalApi
    override fun createLogger(identity: String?): MiraiLogger = LoggerCreator(identity)

    init {
        with(rootPath.toFile()) {
            mkdir()
            require(isDirectory) { "rootDir $absolutePath is not a directory" }
        }
    }
}
val loggerList = HashMap<String, AndroidMiraiLogger>()
val lineReader: LineReader by lazy {
    val terminal = terminal
    if (terminal is NoConsole) return@lazy AllEmptyLineReader

    LineReaderBuilder.builder()
        .terminal(terminal)
        .completer(NullCompleter())
        .build()
}
val mainLogger=AndroidMiraiLogger("android")
val terminal: Terminal = run {
    if (ConsoleTerminalSettings.noConsole) return@run NoConsole

    TerminalBuilder.builder()
        .name("Mirai Console")
        .system(true)
        .jansi(true)
        .dumb(true)
        .paused(true)
        .build()
        .let { terminal ->
            if (terminal is AbstractWindowsTerminal) {
                val pumpField = runCatching {
                    AbstractWindowsTerminal::class.java.getDeclaredField("pump").also {
                        it.isAccessible = true
                    }
                }.onFailure { err ->
                    err.printStackTrace()
                    return@let terminal.also { it.resume() }
                }.getOrThrow()
                var response = terminal
                terminal.setOnClose {
                    response = NoConsole
                }
                terminal.resume()
                val pumpThread = pumpField[terminal] as? Thread ?: return@let NoConsole
                @Suppress("ControlFlowWithEmptyBody")
                while (pumpThread.state == Thread.State.NEW);
                Thread.sleep(1000)
                terminal.setOnClose(null)
                return@let response
            }
            terminal.resume()
            terminal
        }
}

private object ConsoleFrontEndDescImpl : MiraiConsoleFrontEndDescription {
    override val name: String get() = "Terminal"
    override val vendor: String get() = "Mamoe Technologies"

    // net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.version
    // is console's version not frontend's version
    override val version: SemVersion = SemVersion(net.mamoe.mirai.console.internal.MiraiConsoleBuildConstants.versionConst)
}

private val ANSI_RESET = Ansi().reset().toString()

@MiraiInternalApi
internal val LoggerCreator: (identity: String?) -> MiraiLogger = {
    /*PlatformLogger(identity = it, output = { line ->
        lineReader.printAbove(line + ANSI_RESET)
    })*//*
    AndroidMiraiLogger(identity=it).apply {
        if(it==null)
            loggerList[""]=this
        else
            loggerList[it]=this
    }*/
    mainLogger
}

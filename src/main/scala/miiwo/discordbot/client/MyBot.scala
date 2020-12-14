package jellyfishe.beachbot.app

import ackcord._
import ackcord.commands.PrefixParser
import ackcord.gateway.GatewayIntents

object BeachBot extends App {

    println("Starting bot up...")

    val GeneralCommands = "!"

    val token = sys.env("BOT_TOKEN")
    val settings = ClientSettings(token, intents = GatewayIntents.AllNonPrivileged)
    import settings.executionContext

    settings.createClient()
        .foreach { client =>
            client.onEventSideEffectsIgnore {
                case APIMessage.Ready(_) => println("Ready to rock and roll.")
            }

            val myCommands      = new BasicCommands(client, client.requests)
            val myHelpCommand   = new MyHelpCommand(client.requests)

            // High Level API command.
            // Registers a command using the prefix given
            client.commands.runNewCommand(
                PrefixParser.structured(needsMention = false, Seq("!"), Seq("help")), 
                myHelpCommand.command
            )

            client.commands.bulkRunNamedWithHelp(
                myHelpCommand,
                myCommands.greetings,
                myCommands.ping,
                myCommands.queue
            )

            client.login()
        }
}
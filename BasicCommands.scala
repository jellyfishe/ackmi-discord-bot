package jellyfishe.beachbot.app

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import ackcord._
import ackcord.syntax._
import ackcord.commands._
import ackcord.data.{GuildId, Permission}
import ackcord.requests.{CreateMessage, Request}
import ackcord.NotUsed

import akka.stream.scaladsl.{Flow, Sink}


class BasicCommands(client: DiscordClient, requests: Requests) extends CommandController(requests) {

    // High Level Command API

    val greetings: NamedCommand[NotUsed] = 
        Command
            .named(Seq("m!"), Seq("hello")) // (symbol, alias) function turns this into a builder.
            .described("Hello", "Say hello") // (title, description)
            .withRequest(m => m.textChannel.sendMessage(s"Hello ${m.user.username}")) // m = message

    // Low Level Command API

    val shouldMentionMap = new TrieMap[GuildId, Boolean]
    val prefixSymbolsMap = new TrieMap[GuildId, Seq[String]]

    def needMentionInGuild(guildId: GuildId): Future[Boolean] =
        Future.successful(shouldMentionMap.getOrElseUpdate(guildId, false))

    def prefixSymbolsInGuild(guildId: GuildId): Future[Seq[String]] =
        Future.successful(prefixSymbolsMap.getOrElseUpdate(guildId, Seq("m!")))

    //Name info is stored in an object called StructuredPrefixParser
    //You can construct this like we showed above, or you can also use a future returning function
    //The future returning function is also available on the builder itself
    def dynamicPrefix(aliases: String*): StructuredPrefixParser =
    PrefixParser.structuredAsync(
      (c, m) => m.guild(c).fold(Future.successful(false))(g => needMentionInGuild(g.id)),
      (c, m) => m.guild(c).fold(Future.successful(Seq("m!")))(g => prefixSymbolsInGuild(g.id)),
      (_, _) => Future.successful(aliases)
    )

    val ping: NamedDescribedCommand[NotUsed] =
        Command
            .namedParser(dynamicPrefix("ping"))
            .described("Ping", "Checks if the bot is still alive")
            .toSink {
                Flow[CommandMessage[NotUsed]]
                    .map(m => CreateMessage.mkContent(m.message.channelId, "Pong"))
                    .to(requests.sinkIgnore)
            }

    

}
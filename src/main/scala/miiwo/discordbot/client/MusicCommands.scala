package jellyfishe.beachbot.app

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

import ackcord._
import ackcord.commands._
import ackcord.data.{GuildId}

import com.sedmelluq.discord.lavaplayer.player.{AudioPlayerManager, DefaultAudioPlayerManager}
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.{AudioPlaylist, AudioTrack}

class MusicCommands(client: DiscordClient, requests: Requests) extends CommandController(requests) {

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

    val playerManager: AudioPlayerManager = new DefaultAudioPlayerManager
        AudioSourceManagers.registerRemoteSources(playerManager)

    val queue: NamedDescribedCommand[String] =
        GuildVoiceCommand.namedParser(dynamicPrefix("queue", "q"))
        .described("Queue", "MusicPlaying")
        .parsing[String].streamed { r =>
            val guildId     = r.guild.id
            val url         = r.parsed
            val loadItem    = client.loadTrack(playerManager, url)
            val joinChannel = client.joinChannel(guildId, r.voiceChannel.id, playerManager.createPlayer())

            loadItem.zip(joinChannel).map {
                case (track: AudioTrack, player) =>
                    player.startTrack(track, true)
                    client.setPlaying(guildId, playing = true)
                case (playlist: AudioPlaylist, player) =>
                    if (playlist.getSelectedTrack != null) {
                    player.startTrack(playlist.getSelectedTrack, false)
                } else {
                    player.startTrack(playlist.getTracks.get(0), false)
                }
                client.setPlaying(guildId, playing = true)
                case _ => sys.error("Unknown audio item")
            }
        }
}
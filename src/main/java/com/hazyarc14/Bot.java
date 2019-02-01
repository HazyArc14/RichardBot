package com.hazyarc14;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Bot extends ListenerAdapter {
    public static final Logger log = LoggerFactory.getLogger(Bot.class);

    public static void main(String[] args) throws Exception {
        JDA jda = new JDABuilder(System.getenv("BOT_TOKEN")).build();
        jda.addEventListener(new Bot());
    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private Bot() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, guild);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        Guild guild = event.getGuild();
        VoiceChannel voiceChannel = null;

        if (guild != null) {

            if (command.length == 2 && command[1].startsWith("#")) {
                try {
                    voiceChannel = event.getGuild().getVoiceChannelById(command[1].substring(1));
                } catch (Exception e) {
                    log.error("Could not get voice channel by id " + command[1] + " :: ", e);
                }
            }

            if (voiceChannel == null) {
                voiceChannel = event.getMember().getVoiceState().getChannel();
            }

            if ("!help".equalsIgnoreCase(command[0])) {
                event.getMessage().delete().queue();
                guild.getDefaultChannel().sendMessage("```" +
                        "Audio Triggers:\n" +
                        "!doIt\n" +
                        "!hello\n" +
                        "!huuu\n" +
                        "!letsGo\n" +
                        "!yourMomAHo\n" +
                        "```")
                        .queue();

            }

            if (voiceChannel != null) {

                if ("!doIt".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !doIt");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, "https://raw.githubusercontent.com/HazyArc14/RichardBot/master/src/main/resources/audio/doIt.mp3");
                }
                if ("!hello".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !hello");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, "https://raw.githubusercontent.com/HazyArc14/RichardBot/master/src/main/resources/audio/hello.mp3");
                }
                if ("!huuu".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !huuu");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, "https://raw.githubusercontent.com/HazyArc14/RichardBot/master/src/main/resources/audio/huuu.mp3");
                }
                if ("!letsGo".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !letsGo");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, "https://raw.githubusercontent.com/HazyArc14/RichardBot/master/src/main/resources/audio/letsGo.mp3");
                }
                if ("!yourMomAHo".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !yourMomAHo");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, "https://raw.githubusercontent.com/HazyArc14/RichardBot/master/src/main/resources/audio/yourMomAHo.mp3");
                }
                
                if ("~play".equals(command[0]) && command.length == 2) {
                    log.info("User: " + event.getAuthor().getName() + " Command: ~play");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, command[1]);
                } else if ("~skip".equals(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: ~skip");
                    event.getMessage().delete().queue();
                    skipTrack(event.getTextChannel());
                } else if ("~leave".equals(command[0])) {
                    guild.getAudioManager().closeAudioConnection();
                }
            }
        }

        super.onMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final VoiceChannel voiceChannel, final String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
//                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                play(channel.getGuild(), musicManager, voiceChannel, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();

                play(channel.getGuild(), musicManager, voiceChannel, firstTrack);
            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, VoiceChannel voiceChannel, AudioTrack track) {
        connectVoiceChannel(guild.getAudioManager(), voiceChannel);

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

//        channel.sendMessage("Skipped to next track.").queue();
    }

    private static void connectVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            audioManager.openAudioConnection(voiceChannel);
        }
    }
}
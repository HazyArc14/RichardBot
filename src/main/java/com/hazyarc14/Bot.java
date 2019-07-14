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
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
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

        String githubAudioBaseURL = "https://raw.githubusercontent.com/HazyArc14/CollinBot/master/src/main/resources/audio/";
        String githubImageBaseURL = "https://raw.githubusercontent.com/HazyArc14/CollinBot/master/src/main/resources/images/";

        String[] command = event.getMessage().getContentRaw().split(" ", 2);
        Guild guild = event.getGuild();
        VoiceChannel voiceChannel = null;

        if (guild != null) {

            if (command[0] == "~play") {
                try {
                    voiceChannel = event.getGuild().getVoiceChannelById(command[2]);
                } catch (Exception e) {
                    log.error("Could not get voice channel by id " + command[2] + " :: ", e);
                }
            } else if (command.length == 2) {
                try {
                    voiceChannel = event.getGuild().getVoiceChannelById(command[1]);
                } catch (Exception e) {
                    log.error("Could not get voice channel by id " + command[1] + " :: ", e);
                }
            }

            if (voiceChannel == null) {
                voiceChannel = event.getMember().getVoiceState().getChannel();
            }

            if ("!help".equalsIgnoreCase(command[0])) {

                event.getMessage().delete().queue();

                String helpMessage = "```" +
                        "Audio Triggers:\n" +
                        "!doIt\n" +
                        "!hello\n" +
                        "!huuu\n" +
                        "!letsGo\n" +
                        "!yourMomAHo\n" +
                        "\nEmotes:\n" +
                        ";coggers;\n" +
                        ";crabPls;\n" +
                        ";dance;\n" +
                        ";pepeD;\n" +
                        ";pepeDance;\n" +
                        ";pepegaPls;\n" +
                        ";pepeJam;\n" +
                        ";pepoSabers;\n" +
                        ";ppHop;\n" +
                        ";rainbowWeeb;\n" +
                        ";schubertWalk;\n" +
                        ";triKool;\n" +
                        "```";

                event.getAuthor().openPrivateChannel().queue((channel) -> channel.sendMessage(helpMessage).queue());

            }

            if (voiceChannel != null) {

                if ("!doIt".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !doIt");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, githubAudioBaseURL + "doIt.mp3");
                }
                if ("!hello".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !hello");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, githubAudioBaseURL + "hello.mp3");
                }
                if ("!huuu".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !huuu");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, githubAudioBaseURL + "huuu.mp3");
                }
                if ("!letsGo".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !letsGo");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, githubAudioBaseURL + "letsGo.mp3");
                }
                if ("!yourMomAHo".equalsIgnoreCase(command[0])) {
                    log.info("User: " + event.getAuthor().getName() + " Command: !yourMomAHo");
                    event.getMessage().delete().queue();
                    loadAndPlay(event.getTextChannel(), voiceChannel, githubAudioBaseURL + "yourMomAHo.mp3");
                }

                if (event.getAuthor().getIdLong() == 148630426548699136L) {
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

            if (";coggers;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;coggers;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "coggers.gif");
            }
            if (";crabPls;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;crabPls;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "crabPls.gif");
            }
            if (";dance;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;dance;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "dance.gif");
            }
            if (";pepeD;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;pepeD;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "pepeD.gif");
            }
            if (";pepeDance;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;pepeDance;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "pepeDance.gif");
            }
            if (";pepegaPls;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;pepegaPls;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "pepegaPls.gif");
            }
            if (";pepeJam;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;pepeJam;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "pepeJam.gif");
            }
            if (";pepoSabers;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;pepoSabers;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "pepoSabers.gif");
            }
            if (";ppHop;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;ppHop;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "ppHop.gif");
            }
            if (";rainbowWeeb;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;rainbowWeeb;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "rainbowWeeb.gif");
            }
            if (";schubertWalk;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;schubertWalk;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "schubertWalk.gif");
            }
            if (";triKool;".equalsIgnoreCase(command[0])) {
                log.info("User: " + event.getAuthor().getName() + " Command: ;triKool;");
                event.getMessage().delete().queue();
                sendEmote(guild.getDefaultChannel(), "pepoSabers", githubImageBaseURL + "triKool.gif");
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

    private static void sendEmote(TextChannel channel, String emoteName, String emoteUrl) {

        File gif = new File(emoteName + ".gif");
        try {
            FileUtils.copyURLToFile(new URL(emoteUrl), gif);
            channel.sendFile(gif).queue();
        } catch (Exception e) {
            log.error("Error: ", e);
        }

    }
}
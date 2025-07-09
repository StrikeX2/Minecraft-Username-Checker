package com.strikex2.namelookup;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.net.HttpURLConnection;
import java.net.URL;

@Mod("namelookup")
public class NameLookupMod {

    public NameLookupMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onCommandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
            Commands.literal("checkname")
                .then(Commands.argument("username", StringArgumentType.word())
                    .executes(context -> {
                        String username = StringArgumentType.getString(context, "username");
                        CommandSourceStack source = context.getSource();

                        new Thread(() -> {
                            try {
                                // Validate username
                                if (!username.matches("^[a-zA-Z0-9_]+$") || username.length() < 3 || username.length() > 16) {
                                    MutableComponent message = Component.literal("Error: " + username + " is ")
                                        .append(Component.literal("INVALID").withStyle(ChatFormatting.YELLOW))
                                        .append(" (Usernames must be 3–16 characters, no spaces or special charaters other than underscores).");
                                    source.sendSuccess(() -> message, false);
                                    return;
                                }

                                // Check name availability
                                boolean available = isNameAvailable(username);
                                if (available) {
                                    MutableComponent message = Component.literal(username + " is ")
                                        .append(Component.literal("AVAILABLE").withStyle(ChatFormatting.GREEN))
                                        .append(".");
                                    source.sendSuccess(() -> message, false);
                                } else {
                                    MutableComponent message = Component.literal(username + " is ")
                                        .append(Component.literal("UNAVAILABLE").withStyle(ChatFormatting.RED))
                                        .append(".");
                                    source.sendSuccess(() -> message, false);
                                }
                            } catch (Exception e) {
                                source.sendFailure(Component.literal("Error checking name."));
                            }
                        }).start();

                        return 1;
                    }))
        );
    }

    public boolean isNameAvailable(String username) throws Exception {
        URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(3000);
        con.setReadTimeout(3000);

        return con.getResponseCode() == 404;
    }
}

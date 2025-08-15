package com.gmail.nudoing.nGDisplay;


import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.UUID;

public final class NGDisplay extends JavaPlugin implements Listener {


    HashMap<UUID, TextDisplay> textDisplays = new HashMap<>();

    HashMap<UUID, Component> tempTexts = new HashMap<>();


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);

//        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
//        if (protocolManager == null) {
//            getLogger().severe("ProtocolManager is not available. Please ensure ProtocolLib is installed.");
//            return;
//        }

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            // register your commands here ...
            LiteralCommandNode<CommandSourceStack> buildCommand = Commands.literal("ng_reset")
                    .executes(commandContext -> {
                        textDisplays.forEach((uuid, textDisplay) -> textDisplay.remove());
                        textDisplays.clear();
                        tempTexts.clear();

                        getServer().getOnlinePlayers().forEach(this::setupNGDisplay);
                        return Command.SINGLE_SUCCESS;
            }).build();
            commands.registrar().register(buildCommand);
        });



    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }



    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event){



    }


    void setupNGDisplay(Player player) {
        TextDisplay display = player.getWorld().spawn(player.getLocation(), TextDisplay.class, entity -> {
            Component textComponent = tempTexts.getOrDefault(
                    player.getUniqueId(),
                    Component.text("こいつには見えてない\n↓")
            );
            entity.text(textComponent);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setTransformationMatrix(
                    new Matrix4f().translate(0,0.3f,0)
            );
            entity.setVisibleByDefault(false);
            entity.setPersistent(true);
        });

        player.addPassenger(display);
        textDisplays.put(player.getUniqueId(), display);

        display.addScoreboardTag("NG");
        display.addScoreboardTag(player.getName());

        getServer().getOnlinePlayers().stream().filter(p -> !p.equals(player))
                .forEach(p -> p.showEntity(this,display));

        textDisplays.forEach((uuid, textDisplay) -> {
            if (!uuid.equals(player.getUniqueId())) {
                player.showEntity(this, textDisplay);
            }
        });

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        setupNGDisplay(player);

    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID pID = event.getPlayer().getUniqueId();

        TextDisplay textDisplay = textDisplays.get(pID);
        if (textDisplay != null) {
            tempTexts.put(pID, textDisplay.text());
            getLogger().info("remove text");
            textDisplay.remove();
            textDisplays.remove(pID);
        }


        getLogger().info("onQuit end");

    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event){
        Player player = event.getPlayer();
        UUID pID = player.getUniqueId();
        TextDisplay textDisplay = textDisplays.get(pID);
        if (textDisplay != null) {
            textDisplay.teleport(player);
            player.addPassenger(textDisplay);
        }

    }


}

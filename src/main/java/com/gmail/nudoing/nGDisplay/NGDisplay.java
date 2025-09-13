package com.gmail.nudoing.nGDisplay;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.UUID;

public final class NGDisplay extends JavaPlugin implements Listener {


    private final HashMap<UUID, TextDisplay> textDisplays = new HashMap<>();

    private final HashMap<UUID, Component> keepDisplayTexts = new HashMap<>();


    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        clearAll();
    }


    void clearAll(){
        textDisplays.values().forEach(Entity::remove);
        textDisplays.clear();
        keepDisplayTexts.clear();
    }



    /**
     * NGDisplay を 召喚します
     * @param player プレイヤー
     */
    void summonNGDisplay(Player player) {
        TextDisplay display = player.getWorld().spawn(player.getLocation(), TextDisplay.class, entity -> {
            Component textComponent = keepDisplayTexts.getOrDefault(
                    player.getUniqueId(),
                    Component.text("NGワード")
            );
            entity.text(textComponent);
            entity.setBillboard(Display.Billboard.CENTER);
            entity.setTransformationMatrix(
                    new Matrix4f().translate(0,0.3f,0)
            );
            entity.setVisibleByDefault(true);
            entity.setPersistent(false);

            entity.addScoreboardTag("NG");
            entity.addScoreboardTag(player.getName());
            player.hideEntity(this,entity);
            player.addPassenger(entity);

        });

        textDisplays.put(player.getUniqueId(), display);


    }

    /**
     * NGDisplay を 初期化して表示します
     * @param player 対象のプレイヤー
     */
    void createNGDisplay(Player player){
        removeDisplayPermanently(player);
        summonNGDisplay(player);
    }


    void removeTextDisplayEntity(Player player, boolean keepText){
        UUID pID = player.getUniqueId();


        TextDisplay textDisplay = textDisplays.get(pID);
        if(textDisplay != null){
            if(keepText) {
                //表示していたテキストを保存しておく
                keepDisplayTexts.put(pID, textDisplay.text());
            }
            textDisplay.remove();
            textDisplays.remove(pID);
        }

        if(!keepText){
            keepDisplayTexts.remove(pID);
        }


    }

    void hideDisplayTemporarily(Player player){
        removeTextDisplayEntity(player,true);
    }
    void removeDisplayPermanently(Player player){
        removeTextDisplayEntity(player,false);
    }

    boolean setText(Player player,String text){
        UUID pID = player.getUniqueId();
        TextDisplay textDisplay = textDisplays.get(pID);
        if(textDisplay != null){
            textDisplay.text(Component.text(text));
            keepDisplayTexts.put(pID,Component.text(text));
            return true;
        }
        return false;
    }

    //NGワード変更
    void changeNGWord(Player player,String text){

        if(setText(player,text)){
            //minecraft タイトル表示でtext設定されたことをお知らせ
            getServer().showTitle(
                    Title.title(
                            Component.text("NGワード変更", NamedTextColor.GOLD),
                            Component.text(player.getName(),NamedTextColor.AQUA)
                                    .append(Component.text(" のNGワードが変更されました",NamedTextColor.GREEN)),
                            10,100,10
                    )
            );
            getServer().playSound(Sound.sound(
                    Key.key("minecraft:entity.elder_guardian.curse"),
                    Sound.Source.MASTER,
                    1f,1f
            ));
        }


    }

    //プレイヤーアウト！
    void outPlayer(Player player){
        UUID pID = player.getUniqueId();
        TextDisplay textDisplay = textDisplays.get(pID);
        if(textDisplay != null){
            Component textComponent = Component.text(
                    PlainTextComponentSerializer.plainText().serialize(textDisplay.text()),
                    NamedTextColor.RED);
            textDisplay.text(textComponent);
            keepDisplayTexts.put(pID,textComponent);

            getServer().showTitle(
                    Title.title(
                            Component.text(player.getName() + " アウト", NamedTextColor.RED),
                            Component.text(""),
                            10,100,10
                    )
            );
            getServer().playSound(Sound.sound(
                    Key.key("minecraft:entity.wither.spawn"),
                    Sound.Source.MASTER,
                    1f,0.7f
            ));
            getServer().playSound(Sound.sound(
                    Key.key("koneshimaex:se.use_totem"),
                    Sound.Source.MASTER,
                    0.4f,0.5f
            ));

        }

    }

    /**
     * NGDisplay を 直します
     * @param player プレイヤー
     */
    void fixNGDisplay(Player player){
        UUID pID = player.getUniqueId();
        if(textDisplays.containsKey(pID) || keepDisplayTexts.containsKey(pID)){
            hideDisplayTemporarily(player);
            summonNGDisplay(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        fixNGDisplay(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        hideDisplayTemporarily(event.getPlayer());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event){
        fixNGDisplay(event.getPlayer());
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event){
        fixNGDisplay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        removeDisplayPermanently(event.getPlayer());
    }

}

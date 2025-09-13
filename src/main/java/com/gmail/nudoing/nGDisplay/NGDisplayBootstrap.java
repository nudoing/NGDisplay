package com.gmail.nudoing.nGDisplay;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unused")
public class NGDisplayBootstrap implements PluginBootstrap {


    @Override
    public void bootstrap(@NotNull BootstrapContext context) {

        context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,cms ->{

            LiteralCommandNode<CommandSourceStack> bypassCommand = Commands.literal("bp")
                    .requires(source -> source.getSender().hasPermission("ngdisplay.command"))
                    .then(Commands.argument("command",StringArgumentType.greedyString())
                            .executes(ctx ->{
                                String command = StringArgumentType.getString(ctx, "command");
                                NGDisplay plugin = NGDisplay.getPlugin(NGDisplay.class);
                                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),command);
                                return Command.SINGLE_SUCCESS;
                            })

                    ).build();
            cms.registrar().register(bypassCommand);

            LiteralCommandNode<CommandSourceStack> ngdisplayCommand = Commands.literal("ng")
                    .requires(source -> source.getSender().hasPermission("ngdisplay.command"))
                    .then(Commands.literal("create")
                            .then(Commands.argument("players", ArgumentTypes.players())
                                    .executes(ctx -> executeOnPlayers(ctx,PLAYERS_EXECUTE_TYPE.CREATE))
                            )
                    )
                    .then(Commands.literal("remove")
                            .then(Commands.argument("players", ArgumentTypes.players())
                                    .executes(ctx -> executeOnPlayers(ctx,PLAYERS_EXECUTE_TYPE.REMOVE))
                            )
                    )
                    .then(Commands.literal("set")
                            .then(Commands.argument("players", ArgumentTypes.players())
                                    .then(Commands.argument("text", StringArgumentType.greedyString())
                                            .executes(ctx ->{
                                                final List<Player> players = ctx.getArgument("players", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                                String text = StringArgumentType.getString(ctx, "text");
                                                NGDisplay plugin = NGDisplay.getPlugin(NGDisplay.class);
                                                players.forEach(p -> plugin.setText(p,text));
                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )

                    )
                    .then(Commands.literal("change")
                            .then(Commands.argument("player",ArgumentTypes.player())
                                    .then(Commands.argument("text",StringArgumentType.greedyString())
                                            .executes(ctx->{
                                                final List<Player> players = ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                                String text = StringArgumentType.getString(ctx, "text");
                                                NGDisplay plugin = NGDisplay.getPlugin(NGDisplay.class);
                                                if(!players.isEmpty()){
                                                    plugin.changeNGWord(players.getFirst(),text);
                                                }

                                                return Command.SINGLE_SUCCESS;
                                            })
                                    )
                            )
                    )
                    .then(Commands.literal("out")
                            .then(Commands.argument("player",ArgumentTypes.player())
                                    .executes(ctx ->{
                                        final List<Player> players = ctx.getArgument("player",PlayerSelectorArgumentResolver.class).resolve(ctx.getSource());
                                        NGDisplay plugin = NGDisplay.getPlugin(NGDisplay.class);
                                        if(!players.isEmpty()){
                                            plugin.outPlayer(players.getFirst());
                                        }

                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(Commands.literal("clear_all")
                            .executes(ctx ->{
                                NGDisplay.getPlugin(NGDisplay.class).clearAll();
                                return Command.SINGLE_SUCCESS;
                            })
                    )
                    .build();

            cms.registrar().register(ngdisplayCommand);
        });

    }


    enum PLAYERS_EXECUTE_TYPE {
        CREATE,
        REMOVE
    }

    int executeOnPlayers(CommandContext<CommandSourceStack> ctx, PLAYERS_EXECUTE_TYPE type) throws CommandSyntaxException {
        final PlayerSelectorArgumentResolver playerResolver = ctx.getArgument("players", PlayerSelectorArgumentResolver.class);
        final List<Player> players = playerResolver.resolve(ctx.getSource());

        NGDisplay plugin = NGDisplay.getPlugin(NGDisplay.class);

        switch (type){
            case CREATE -> players.forEach(plugin::createNGDisplay);
            case REMOVE -> players.forEach(plugin::removeDisplayPermanently);
        }

        return Command.SINGLE_SUCCESS;
    }
}

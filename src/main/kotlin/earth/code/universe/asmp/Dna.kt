package earth.code.universe.asmp

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.commands.Commands
import net.minecraft.commands.Commands.argument
import net.minecraft.commands.Commands.literal
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.network.chat.Component
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Rarity
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.entity.item.ItemEntity

class Dna : ModInitializer {

    object ModItems {
        val DNA = Registry.register(
            BuiltInRegistries.ITEM,
            ResourceLocation("asmp_dna", "dna"),
            DnaItem(Item.Properties().rarity(Rarity.EPIC).stacksTo(16).fireResistant())
        )

        fun init() {}
    }

    override fun onInitialize() {
        ModEvents.init()
        ModItems.init()
        println("ASMP DNA System Initialized")
        CommandRegistrationCallback.EVENT.register { dispatcher, registryAccess, env ->
            dispatcher.register(
                Commands.literal("dna")
                    .requires { source -> source.hasPermission(2) }
                    .then(
                        literal("change").then(
                            argument("Player", EntityArgument.player())
                                .executes { context ->
                                    val source = context.source
                                    val server = source.server
                                    val commandsExecutor = server.commands
                                    val target = EntityArgument.getPlayer(context, "Player").scoreboardName

                                    val runCommand = "scoreboard players add $target dna 1"


                                    // Elevate permission AND suppress vanilla output chat logs
                                    val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                    commandsExecutor.performPrefixedCommand(elevatedSource, runCommand)

                                    source.sendSystemMessage(Component.literal("Granted 1 DNA to $target."))
                                    1
                                }
                                .then(
                            argument("DNA_Points", IntegerArgumentType.integer())
                                .executes { context ->
                                val source = context.source
                                val server = source.server
                                val commandsExecutor = server.commands
                                val target = EntityArgument.getPlayer(context, "Player").scoreboardName

                                val points = IntegerArgumentType.getInteger(context, "DNA_Points")

                                var runCommand = "scoreboard players add $target dna $points"

                                if (points < 0) {
                                    val newPoints = -points
                                    runCommand = "scoreboard players remove $target dna $newPoints"
                                }

                                // Elevate permission AND suppress vanilla output chat logs
                                val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                commandsExecutor.performPrefixedCommand(elevatedSource, runCommand)

                                source.sendSystemMessage(Component.literal("Granted $points DNA to $target."))
                                1
                            })
                        )
                    )
                    .then(
                        literal("pay")
                            .then(
                                argument("Player", EntityArgument.player())
                                    .executes { context ->
                                        val source = context.source
                                        val server = source.server
                                        val commandExecutor = server.commands

                                        val target = EntityArgument.getPlayer(context, "Player")

                                        // Elevate permission AND suppress vanilla output chat logs
                                        val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                        val runCommand = arrayOf("scoreboard players add ${target.scoreboardName} dna 1",
                                            "scoreboard players remove ${source.player?.scoreboardName} dna 1")

                                        var dna = 0

                                        val scoreboard = server.scoreboard
                                        val objective = scoreboard.getObjective("dna")

                                        if (objective != null) {
                                            val score = scoreboard.getOrCreatePlayerScore(
                                                source.player?.scoreboardName,
                                                objective
                                            )

                                            dna = score.score
                                        }

                                        if (dna > 0) {
                                            runCommand.forEach { command ->
                                                commandExecutor.performPrefixedCommand(
                                                    elevatedSource,
                                                    command
                                                )
                                            }



                                            source.sendSystemMessage(Component.literal("You gave 1 DNA to ${target.scoreboardName}."))
                                            target.sendSystemMessage(Component.literal("You received 1 DNA from ${source.player?.scoreboardName}."))
                                        }
                                        else {
                                            source.sendSystemMessage(Component.literal("You don't have any dna left."))
                                        }
                                        1
                                    }
                                    .then(
                                    argument("DNA_Points", IntegerArgumentType.integer(1))
                                    .executes { context ->
                                        val source = context.source
                                        val server = source.server
                                        val commandExecutor = server.commands

                                        val target = EntityArgument.getPlayer(context, "Player")
                                        val points = IntegerArgumentType.getInteger(context, "DNA_Points")

                                        // Elevate permission AND suppress vanilla output chat logs
                                        val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                        val runCommand = arrayOf("scoreboard players add ${target.scoreboardName} dna $points",
                                            "scoreboard players remove ${source.player?.scoreboardName} dna $points")

                                        var dna = 0

                                        val scoreboard = server.scoreboard
                                        val objective = scoreboard.getObjective("dna")

                                        if (objective != null) {
                                            val score = scoreboard.getOrCreatePlayerScore(
                                                source.player?.scoreboardName,
                                                objective
                                            )

                                            dna = score.score
                                        }

                                        if (dna >= points) {
                                            runCommand.forEach { command ->
                                                commandExecutor.performPrefixedCommand(
                                                    elevatedSource,
                                                    command
                                                )
                                            }

                                            source.sendSystemMessage(Component.literal("You gave $points DNA to ${target.scoreboardName}."))
                                            target.sendSystemMessage(Component.literal("You received $points DNA from ${source.player?.scoreboardName}"))
                                        }
                                        else {
                                            source.sendSystemMessage(Component.literal("You don't have enough dna left."))
                                        }

                                        1
                                    }
                                )
                            )
                    )
                    .then(
                        literal("item")
                                    .executes { context ->
                                        val source = context.source
                                        val server = source.server
                                        val commandExecutor = server.commands

                                        // Elevate permission AND suppress vanilla output chat logs
                                        val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                        val runCommand = arrayOf("give ${source.player?.scoreboardName} asmp_dna:dna 1",
                                            "scoreboard players remove ${source.player?.scoreboardName} dna 1")

                                        var dna = 0

                                        val scoreboard = server.scoreboard
                                        val objective = scoreboard.getObjective("dna")

                                        if (objective != null) {
                                            val score = scoreboard.getOrCreatePlayerScore(
                                                source.player?.scoreboardName,
                                                objective
                                            )

                                            dna = score.score
                                        }

                                        if (dna > 0) {
                                            runCommand.forEach { command ->
                                                commandExecutor.performPrefixedCommand(
                                                    elevatedSource,
                                                    command
                                                )
                                            }



                                            source.sendSystemMessage(Component.literal("Converted 1 DNA Point into a Helix."))
                                        }
                                        else {
                                            source.sendSystemMessage(Component.literal("You don't have any dna left."))
                                        }
                                        1
                                    }
                                    .then(
                                        argument("DNA_Points", IntegerArgumentType.integer(1))
                                            .executes { context ->
                                                val source = context.source
                                                val server = source.server
                                                val commandExecutor = server.commands

                                                val points = IntegerArgumentType.getInteger(context, "DNA_Points")

                                                // Elevate permission AND suppress vanilla output chat logs
                                                val elevatedSource = source.withPermission(4).withSuppressedOutput()
                                                val runCommand = arrayOf("give ${source.player?.scoreboardName} asmp_dna:dna $points",
                                                    "scoreboard players remove ${source.player?.scoreboardName} dna $points")

                                                var dna = 0

                                                val scoreboard = server.scoreboard
                                                val objective = scoreboard.getObjective("dna")

                                                if (objective != null) {
                                                    val score = scoreboard.getOrCreatePlayerScore(
                                                        source.player?.scoreboardName,
                                                        objective
                                                    )

                                                    dna = score.score
                                                }

                                                if (dna >= points) {
                                                    runCommand.forEach { command ->
                                                        commandExecutor.performPrefixedCommand(
                                                            elevatedSource,
                                                            command
                                                        )
                                                    }

                                                    source.sendSystemMessage(Component.literal("Converted $points DNA Points into a Helices."))
                                                }
                                                else {
                                                    source.sendSystemMessage(Component.literal("You don't have enough DNA left."))
                                                }

                                                1
                                            }
                                    )
                            )
            )
        }
    }
    class DnaItem(properties: Properties) : Item(properties) {

        override fun use(
            level: Level,
            player: Player,
            hand: InteractionHand
        ): InteractionResultHolder<ItemStack> {

            val stack = player.getItemInHand(hand)

            if (!level.isClientSide) {
                if (player.isCrouching) {
                    val dna = player.getItemInHand(hand).count

                    player.getItemInHand(hand).count = 0

                    val server = player.createCommandSourceStack().server
                    val elevatedSource = player.createCommandSourceStack().withPermission(4).withSuppressedOutput()
                    val runCommand = "scoreboard players add ${player.scoreboardName} dna $dna"
                    val commandExecutor = server.commands

                    commandExecutor.performPrefixedCommand(elevatedSource, runCommand)

                    player.createCommandSourceStack().sendSystemMessage(Component.literal("Gained $dna DNA Points from Helices."))
                }
                else {
                    player.getItemInHand(hand).count--

                    val server = player.createCommandSourceStack().server
                    val elevatedSource = player.createCommandSourceStack().withPermission(4).withSuppressedOutput()
                    val runCommand = "scoreboard players add ${player.scoreboardName} dna 1"
                    val commandExecutor = server.commands

                    commandExecutor.performPrefixedCommand(elevatedSource, runCommand)

                    player.createCommandSourceStack().sendSystemMessage(Component.literal("Gained 1 DNA Point from the Helix."))
                }
            }

            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide)
        }
    }
    object ModEvents {
        fun init() {
            ServerLivingEntityEvents.AFTER_DEATH.register { entity, damageSource ->
                if (entity is Player && damageSource.entity is Player) {
                    val player = entity as Player

                    var dna = 0

                    val scoreboard = player.server!!.scoreboard
                    val objective = scoreboard.getObjective("dna")

                    if (objective != null) {
                        val score = scoreboard.getOrCreatePlayerScore(
                            player.scoreboardName,
                            objective
                        )

                        dna = score.score
                    }

                    if(dna > 0) {
                        val server = player.createCommandSourceStack().server
                        val elevatedSource = player.createCommandSourceStack().withPermission(4).withSuppressedOutput()
                        val runCommand = "scoreboard players remove ${player.scoreboardName} dna 1"
                        val commandExecutor = server.commands

                        commandExecutor.performPrefixedCommand(elevatedSource, runCommand)

                        val item = ItemStack(ModItems.DNA)

                        val itemEntity = ItemEntity(
                            player.level(),
                            player.x,
                            player.y,
                            player.z,
                            item
                        )

                        player.level().addFreshEntity(itemEntity)
                    }
                }
            }
        }
    }
}
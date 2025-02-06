package me.barsam.dragonDiscordCore

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import java.awt.Color

class DiscordBotListener(private val plugin: DragonDiscordCore) : ListenerAdapter() {

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val message = event.message.contentRaw
        val author = event.author

        // Discord command: !linkaccount
        if (message.startsWith("!asdklajlkwjep1oi2j312hdoi192e8hadusdkj12h3io1uhds1iu2he1i2ueb1j231k2ush124y12412431891h123jkh") && !author.isBot) {
            // Check if this user is already linked to a Minecraft account
            val linkedAccounts = plugin.config.getConfigurationSection("linked")?.getValues(false) ?: emptyMap<String, Any>()
            val alreadyLinked = linkedAccounts.values.any { (it as? Map<*, *>)?.get("discordId") == author.id }

            if (alreadyLinked) {
                event.channel.sendMessage("You have already linked your Discord account to a Minecraft account.").queue()
                return
            }

            // Create and send an embed with a button
            val embed: MessageEmbed = EmbedBuilder()
                .setTitle("Account Linking")
                .setDescription("Click the button below to link your Discord and Minecraft accounts.")
                .setColor(Color.BLUE)
                .build()

            val button: Button = Button.primary("link-account", "Link Account")
            val syncButton: Button = Button.secondary("sync-account", "Sync Account")

            event.channel.sendMessageEmbeds(embed)
                .setActionRow(button, syncButton)
                .queue()
        }

        // Check if the message is a 7-digit code
        if (message.matches(Regex("\\d{7}"))) { // Match exactly 7 digits
            val code = message
            val playerName = plugin.config.getString("$code.username")
            val discordId = author.id

            if (playerName != null && plugin.config.getString("$code.discordId") == "NoDiscordLinked") {
                // Update the configuration to link accounts
                plugin.config.set("linked.$playerName.discordId", discordId)  // Save under player's name
                plugin.config.set("linked.$playerName.username", playerName)

                // Remove the old code entry
                plugin.config.set(code, null)
                plugin.saveConfig()
                plugin.syncAccount(playerName, discordId)
                // Notify the user
                event.channel.sendMessage("Your Minecraft account **$playerName** has been successfully linked to your Discord account!").queue()

            } else {
                event.channel.sendMessage("Invalid code or this code is already linked to a Discord account.").queue()
            }
        }
    }

    // Handle the button click event for account linking
// Handle the button click event for account linking
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        val member = event.member
        val buttonId = event.componentId

        if (buttonId == "link-account" && member != null) {
            event.reply("To link your Minecraft account, please send your 7-digit linking code in a DM.").setEphemeral(true).queue()
        } else if (buttonId == "sync-account" && member != null) {

            val discordId = event.user.id


            val minecraftUsername = plugin.getMinecraftUsernameByDiscordId(discordId)

            if (minecraftUsername != null) {
                println("Found Minecraft username: $minecraftUsername")

            } else {
                println("No linked Minecraft account found.")
            }

            if (minecraftUsername != null) {
                // Sync the account with the found username
                plugin.syncAccount(minecraftUsername, discordId)
                event.reply("Syncing your account with Minecraft...").setEphemeral(true).queue()
            } else {
                event.reply("No Minecraft account linked to your Discord.").setEphemeral(true).queue()
            }
        }
    }

}

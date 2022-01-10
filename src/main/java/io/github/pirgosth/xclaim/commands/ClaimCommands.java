package io.github.pirgosth.xclaim.commands;

import io.github.pirgosth.liberty.core.api.commands.CommandExecutor;
import io.github.pirgosth.liberty.core.api.commands.ICommandArgument;
import io.github.pirgosth.liberty.core.api.commands.ICommandListener;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommand;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommandArgument;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommandExecutor;
import io.github.pirgosth.liberty.core.api.commands.annotations.LibertyCommandPermission;
import io.github.pirgosth.liberty.core.api.utils.ChatUtils;
import io.github.pirgosth.liberty.core.commands.CommandParameters;
import io.github.pirgosth.xclaim.Messages;
import io.github.pirgosth.xclaim.cache.IPlayerClaimCache;
import io.github.pirgosth.xclaim.cache.PlayerClaimCacheManager;
import io.github.pirgosth.xclaim.config.*;
import io.github.pirgosth.xclaim.math.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClaimCommands implements ICommandListener {

    private boolean preventCommand(Entity entity) {
        return !XClaimConfig.getConfiguration().isWorldEnabled(entity.getWorld());
    }

    @LibertyCommand(command = "claim.create")
    @LibertyCommandPermission(permission = "xclaim.commands.create")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.Integer)
    public boolean createClaimCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if (this.preventCommand(player)) return true;

        String name = params.args[0];
        int radius = Integer.parseInt(params.args[1]);

        if (radius < 0) {
            //TODO: Send error message to player
            ChatUtils.sendPlayerColorMessage(player, "&cClaim size must be greater than 0.");
//            Messages.sendInformation("on-invalid-claim-size", entity, Integer.toString(rmax));
            return true;
        }

        CuboidRegion claimRegion = new CuboidRegion(player.getLocation(), radius);
        @NotNull WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));

        //If claim is too near to another
        if (!worldSection.isLandAvailable(claimRegion)) {
            //TODO: Send error message to player
            ChatUtils.sendPlayerColorMessage(player, "&cThis location is to near to another claim.");
//            Messages.sendInformation("on-too-near", player);
            return true;
        }

        PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration(player);

        if (!player.hasPermission("xclaim.claims.count.unlimited") || playerConfiguration.getClaimCount() >= XClaimConfig.getConfiguration().getClaimCountPerPlayer()) {
            //TODO: Send error message to player
            ChatUtils.sendPlayerColorMessage(player, "&cYou already reached the maximum amount of claims.");
//            Messages.sendInformation("on-too-many-claims", player);
            return true;
        }

        worldSection.createClaim(player, name, claimRegion);

        /* TODO:
        Refresh online players cache to take the new claim into account.
        */

        //TODO: Send success message to player
        ChatUtils.sendPlayerColorMessage(player, String.format("&3Claim &2%s &3successfully created.", name));
//        Messages.sendInformation("on-claim-created", player, Integer.toString(r));
        return true;
    }

    private void removeClaim(WorldSection worldSection, ClaimConfiguration claimConfiguration) {
        List<ClaimMember> members = claimConfiguration.getMembers();

        for (ClaimMember member : members) {
            PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration(member.getSpigotPlayer());
            playerConfiguration.removeClaimConfiguration(claimConfiguration);
            if (member.getSpigotPlayer().isOnline()) {
                Player onlinePlayer = member.getSpigotPlayer().getPlayer();
                ChatUtils.sendPlayerColorMessage(onlinePlayer, String.format("&a%s &3claim was removed.", claimConfiguration.getName()));
                //TODO: Send message to members to notify them from the claim deletion.
            }
        }

        worldSection.removeClaim(claimConfiguration);

        //TODO: Refresh claimCache from online players to remove outdated claim.
    }

    private boolean removeClaimByName(Entity entity, String name) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(entity.getWorld()));
        PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration((OfflinePlayer) entity);
        ClaimConfiguration claimConfiguration = playerConfiguration.getClaimConfigurationByName(name);

        //TODO: Send error message for no claim.
        if (claimConfiguration == null){
            ChatUtils.sendPlayerColorMessage((Player) entity, String.format("&cYou do not have any claim named %s.", name));
            return true;
        }

        ClaimMember claimMember = Objects.requireNonNull(claimConfiguration.getMember((OfflinePlayer) entity));
        //TODO: Send error message when not enough permission to remove current claim.
        if (claimMember.getRole() != ClaimMember.Role.Owner) {
            ChatUtils.sendPlayerColorMessage((Player) entity, "&cYou do not have the right to remove this claim.");
            return true;
        }

        this.removeClaim(worldSection, claimConfiguration);

        return true;
    }

    private boolean removeStandingClaim(Entity entity) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(entity.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(entity.getLocation());

        //TODO: Send error message for no claim.
        if (claimConfiguration == null) {
            ChatUtils.sendPlayerColorMessage((Player) entity, "&cYou are not in a claim.");
            return true;
        }

        ClaimMember claimMember = claimConfiguration.getMember((OfflinePlayer) entity);

        //TODO: Send error message when not enough permission to remove current claim.
        if ((claimMember != null && claimMember.getRole() != ClaimMember.Role.Owner) || !entity.hasPermission("xclaim.admin.commands.remove.others")) {
            ChatUtils.sendPlayerColorMessage((Player) entity, "&cYou do not have the right to remove this claim.");
            return true;
        }

        this.removeClaim(worldSection, claimConfiguration);
        if(!claimConfiguration.isMember((OfflinePlayer) entity)) ChatUtils.sendPlayerColorMessage((Player) entity, String.format("&aClaim %s successfully removed.", claimConfiguration.getName()));

        return true;
    }

    @LibertyCommand(command = "claim.remove")
    @LibertyCommandPermission(permission = "xclaim.commands.remove.self")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String, optional = true)
    public boolean removeClaimCommand(CommandParameters params) {
        Entity entity = (Entity) params.sender;
        if (this.preventCommand(entity)) return true;
        String name = params.args.length > 0 ? params.args[0] : null;
        return name != null ? this.removeClaimByName(entity, name) : this.removeStandingClaim(entity);
    }

    private void displayClaimInfo(Player player) {
        IPlayerClaimCache pcc = PlayerClaimCacheManager.getInstance().getPlayerClaimCache(player);
        if (!pcc.isInWild()) {
            @NotNull ClaimConfiguration claim = Objects.requireNonNull(pcc.getClaim());
            ChatUtils.sendPlayerColorMessage(player, String.format("""
                    &7Claim Info:
                    Name: &b%s
                    &7Borders: &b%s
                    &7Radius: &b%s
                    &7Members: &b%s
                    &7Owners: &b%s""", claim.getName(), claim.getRegion(), claim.getRegion().getRadius(), claim.getMembers(), claim.getOwners()));
        } else {
            Messages.sendInformation("on-not-in-claim", player);
        }
    }

    @LibertyCommand(command = "claim.info")
    @LibertyCommandPermission(permission = "xclaim.commands.info")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    public boolean infoCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;
        this.displayClaimInfo(player);
        return true;
    }

    private void displayClaimList(Player player) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        List<ClaimConfiguration> claimConfigurations = worldSection.getPlayerConfiguration(player).getClaimConfigurations();
        if (claimConfigurations.isEmpty()) {
            Messages.sendInformation("on-no-claim", player);
            return;
        }

        List<String> claimNames = new ArrayList<>();
        for (ClaimConfiguration claimConfiguration : claimConfigurations) claimNames.add(claimConfiguration.getName());
        Messages.sendInformation("on-claims-disp", player, claimNames.toString());
    }

    @LibertyCommand(command = "claim.list")
    @LibertyCommandPermission(permission = "xclaim.commands.list")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    public boolean listClaimCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if (this.preventCommand(player)) return true;
        displayClaimList(player);
        return true;
    }

    @LibertyCommand(command = "claim.home")
    @LibertyCommandPermission(permission = "xclaim.commands.home")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String)
    public boolean homeCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;

        String claimName = params.args[0];
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration(player);
        ClaimConfiguration claimConfiguration = playerConfiguration.getClaimConfigurationByName(claimName);

        if(claimConfiguration == null) {
            //TODO: Send error message on wrong claim name
            ChatUtils.sendPlayerColorMessage(player, String.format("&cYou do not have any claim named %s.", claimName));
            return true;
        }

        Location homeLocation = Objects.requireNonNull(playerConfiguration.getPlayerClaimConfiguration(claimConfiguration).getHome());

        Messages.sendInformation("on-home-teleport", player, claimName);
        player.teleport(homeLocation);

        return true;
    }

    @LibertyCommand(command = "claim.sethome")
    @LibertyCommandPermission(permission = "xclaim.commands.sethome")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    public boolean setHomeCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;

        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(player.getLocation());

        if (claimConfiguration == null) {
            Messages.sendInformation("on-not-in-claim", player);
            return true;
        }

        if (!claimConfiguration.isMember(player)) {
            Messages.sendInformation("on-not-in-own-claim", player);
            return true;
        }

        PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration(player);
        playerConfiguration.getPlayerClaimConfiguration(claimConfiguration).setHome(player.getLocation());

        Messages.sendInformation("on-sethome", player, claimConfiguration.getName());
        return true;
    }

    private boolean addMemberToClaim(Player sender, String playerNameToAdd, ClaimMember.Role role) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(sender.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(sender.getLocation());

        if(claimConfiguration == null) {
            //TODO: Send error message when player is not in a claim
            ChatUtils.sendPlayerColorMessage(sender, "&cYou are not in a claim.");
            return true;
        } else if(!claimConfiguration.isMember(sender) || claimConfiguration.getMember(sender).getRole() != ClaimMember.Role.Owner) {
            //TODO: Send error message when player is not member of the standing claim.
            ChatUtils.sendPlayerColorMessage(sender, "&cYou do not have the right to manage this claim.");
            return true;
        }

        Player playerToAdd = Bukkit.getPlayer(playerNameToAdd);
        if(playerToAdd == null) {
            //TODO: Send error message when typed player named does not exists or is not connected.
            ChatUtils.sendPlayerColorMessage(sender, String.format("&cPlayer %s does not exists or is not connected.", playerNameToAdd));
            return true;
        }

        if(claimConfiguration.isMember(playerToAdd)) {
            //TODO: Send error when player to add is already a member of this claim.
            ChatUtils.sendPlayerColorMessage(sender, String.format("&cPlayer %s is already a member of this claim.", playerToAdd.getName()));
            return true;
        }

        PlayerConfiguration senderConfig = worldSection.getPlayerConfiguration(sender);
        PlayerConfiguration playerToAddConfig = worldSection.getPlayerConfiguration(playerToAdd);

        PlayerClaimConfiguration senderPCC = Objects.requireNonNull(senderConfig.getPlayerClaimConfiguration(claimConfiguration));

        claimConfiguration.getMembers().add(new ClaimMember(playerToAdd, role));
        playerToAddConfig.addClaimConfiguration(claimConfiguration, senderPCC.getHome().clone());

        //TODO: Notify sender and claim members from the new member.
        for (ClaimMember member : claimConfiguration.getMembers()) {
            if(member.getSpigotPlayer().isOnline()) {
                Player onlineMember = member.getSpigotPlayer().getPlayer();
                ChatUtils.sendPlayerColorMessage(onlineMember, String.format("&aPlayer %s was added to %s claim.", playerToAdd.getName(), claimConfiguration.getName()));
            }
        }

        return true;
    }

    @LibertyCommand(command = "claim.members.add")
    @LibertyCommandPermission(permission = "xclaim.commands.members.add")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String)
    public boolean addMemberCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;
        return this.addMemberToClaim(player, params.args[0], ClaimMember.Role.Member);
    }

    @LibertyCommand(command = "claim.owners.add")
    @LibertyCommandPermission(permission = "xclaim.commands.owners.add")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String)
    public boolean addOwnerCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;
        return this.addMemberToClaim(player, params.args[0], ClaimMember.Role.Owner);
    }

    private boolean removeMemberFromClaim(Player sender, String playerNameToRemove) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(sender.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(sender.getLocation());

        if(claimConfiguration == null) {
            //TODO: Send error message when player is not in a claim
            ChatUtils.sendPlayerColorMessage(sender, "&cYou are not in a claim.");
            return true;
        } else if(!claimConfiguration.isMember(sender) || claimConfiguration.getMember(sender).getRole() != ClaimMember.Role.Owner) {
            //TODO: Send error message when player is not member of the standing claim.
            ChatUtils.sendPlayerColorMessage(sender, "&cYou do not have the right to manage this claim.");
            return true;
        }

        ClaimMember senderMember = Objects.requireNonNull(claimConfiguration.getMember(sender));
        ClaimMember memberToRemove = claimConfiguration.getMember(playerNameToRemove);

        if(memberToRemove == null) {
            //TODO: Send error message when typed player named does not belong to this claim.
            ChatUtils.sendPlayerColorMessage(sender, String.format("&cPlayer %s does not exists or is not member of this claim", playerNameToRemove));
            return true;
        } else if (senderMember.equals(memberToRemove)) {
            //TODO: Send error message when sender try to remove itself from his claim.
            ChatUtils.sendPlayerColorMessage(sender, "&cYou cannot remove yourself from a claim. Use the leave command instead.");
            return true;
        }

        PlayerConfiguration playerToRemoveConfig = worldSection.getPlayerConfiguration(memberToRemove.getSpigotPlayer());
        playerToRemoveConfig.removeClaimConfiguration(claimConfiguration);

        claimConfiguration.getMembers().remove(memberToRemove);
        //TODO: Notify sender and claim members from the removed member.
        for (ClaimMember member : claimConfiguration.getMembers()) {
            if(member.getSpigotPlayer().isOnline()) {
                Player onlineMember = member.getSpigotPlayer().getPlayer();
                ChatUtils.sendPlayerColorMessage(onlineMember, String.format("&aPlayer %s was removed from %s claim.", playerNameToRemove, claimConfiguration.getName()));
            }
        }

        return true;
    }

    @LibertyCommand(command = "claim.members.remove")
    @LibertyCommandPermission(permission = "xclaim.commands.members.remove")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String)
    public boolean removeMemberCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;
        return this.removeMemberFromClaim(player, params.args[0]);
    }

    private boolean leaveStandingClaim(Player player) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        ClaimConfiguration claimConfiguration = worldSection.getClaimConfigurationByLocation(player.getLocation());

        if (claimConfiguration == null) {
            //TODO: Send error message when player is not in a claim
            ChatUtils.sendPlayerColorMessage(player, "&cYou are not in a claim.");
            return true;
        } else if (!claimConfiguration.isMember(player)) {
            //TODO: Send error message when player is not member of this claim.
            ChatUtils.sendPlayerColorMessage(player, "&cYou are not member of this claim.");
            return true;
        }

        ClaimMember leavingMember = Objects.requireNonNull(claimConfiguration.getMember(player));

        if (leavingMember.getRole().equals(ClaimMember.Role.Owner) && claimConfiguration.getOwners().size() == 1) {
            //TODO: Send error message when last owner tries to leave his claim.
            ChatUtils.sendPlayerColorMessage(player, "&cYou are the last owner, you can't leave this claim. You must either add another owner or use the remove command instead.");
            return true;
        }

        PlayerConfiguration playerToRemoveConfig = worldSection.getPlayerConfiguration(leavingMember.getSpigotPlayer());
        playerToRemoveConfig.removeClaimConfiguration(claimConfiguration);
        claimConfiguration.getMembers().remove(leavingMember);
        //TODO: Notify remaining members from member depart.
        for (ClaimMember member : claimConfiguration.getMembers()) {
            if(member.getSpigotPlayer().isOnline()) {
                Player onlineMember = member.getSpigotPlayer().getPlayer();
                ChatUtils.sendPlayerColorMessage(onlineMember, String.format("&aPlayer %s left %s claim.", player.getName(), claimConfiguration.getName()));
            }
        }

        ChatUtils.sendPlayerColorMessage(player, String.format("&aYou left %s claim.", claimConfiguration.getName()));

        return true;
    }

    private boolean leaveClaim(Player player, String claimName) {
        WorldSection worldSection = Objects.requireNonNull(XClaimConfig.getConfiguration().getWorldSection(player.getWorld()));
        PlayerConfiguration playerConfiguration = worldSection.getPlayerConfiguration(player);
        ClaimConfiguration claimConfiguration = playerConfiguration.getClaimConfigurationByName(claimName);

        if (claimConfiguration == null) {
            //TODO: Send error message when claim name is incorrect.
            ChatUtils.sendPlayerColorMessage(player, String.format("&cYou do not have any claim named %s.", claimName));
            return true;
        }

        ClaimMember leaveMember = Objects.requireNonNull(claimConfiguration.getMember(player));

        if (leaveMember.getRole().equals(ClaimMember.Role.Owner) && claimConfiguration.getOwners().size() == 1) {
            ChatUtils.sendPlayerColorMessage(player, "&cYou are the last owner, you can't leave this claim. You must either add another owner or use the remove command instead.");
            //TODO: Send error message when last owner tries to leave his claim.
            return true;
        }

        PlayerConfiguration playerToRemoveConfig = worldSection.getPlayerConfiguration(leaveMember.getSpigotPlayer());
        playerToRemoveConfig.removeClaimConfiguration(claimConfiguration);
        claimConfiguration.getMembers().remove(leaveMember);
        //TODO: Notify remaining members from member depart.
        for (ClaimMember member : claimConfiguration.getMembers()) {
            if(member.getSpigotPlayer().isOnline()) {
                Player onlineMember = member.getSpigotPlayer().getPlayer();
                ChatUtils.sendPlayerColorMessage(onlineMember, String.format("&aPlayer %s left %s claim.", player.getName(), claimConfiguration.getName()));
            }
        }

        return true;
    }

    @LibertyCommand(command = "claim.leave")
    @LibertyCommandPermission(permission = "xclaim.commands.leave")
    @LibertyCommandExecutor(executor = CommandExecutor.ENTITY)
    @LibertyCommandArgument(type = ICommandArgument.ArgumentType.String, optional = true)
    public boolean leaveClaimCommand(CommandParameters params) {
        Player player = (Player) params.sender;
        if(this.preventCommand(player)) return true;

        String claimName = params.args.length > 0 ? params.args[0] : null;
        return claimName != null ? this.leaveClaim(player, claimName) : this.leaveStandingClaim(player);
    }

}
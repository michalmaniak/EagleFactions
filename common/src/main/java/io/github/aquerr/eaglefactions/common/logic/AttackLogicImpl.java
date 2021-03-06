package io.github.aquerr.eaglefactions.common.logic;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.AttackLogic;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AttackLogicImpl implements AttackLogic
{
    private static AttackLogic INSTANCE = null;

    private final FactionsConfig factionsConfig;
    private final FactionLogic factionLogic;

    private AttackLogicImpl(EagleFactions eagleFactions)
    {
        factionsConfig = eagleFactions.getConfiguration().getFactionsConfig();
        factionLogic = eagleFactions.getFactionLogic();
    }

    public static AttackLogic getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            INSTANCE = new AttackLogicImpl(eagleFactions);
        return INSTANCE;
    }

    @Override
    public void attack(final Player player, final Vector3i attackedChunk)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(new Consumer<Task>()
        {
            int seconds = 1;

            @Override
            public void accept(Task task)
            {
                if(attackedChunk.toString().equals(player.getLocation().getChunkPosition().toString()))
                {
                    if(seconds == factionsConfig.getAttackTime())
                    {
                        //Because it is not possible to attack territory that is not claimed then we can safely get faction here.
                        Faction chunkFaction = factionLogic.getFactionByChunk(player.getWorld().getUniqueId(), attackedChunk).get();

                        informAboutDestroying(chunkFaction);
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.CLAIM_DESTROYED));

                        final Claim claim = new Claim(player.getWorld().getUniqueId(), attackedChunk);
                        factionLogic.destroyClaim(chunkFaction, claim);
                        task.cancel();
                    }
                    else
                    {
                        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RESET, seconds));
                        seconds++;
                    }
                }
                else
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MOVED_FROM_THE_CHUNK));
                    task.cancel();
                }
            }
        }).submit(EagleFactionsPlugin.getPlugin());
    }

    @Override
    public void blockClaiming(String factionName)
    {
        if(EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(factionName))
        {
            EagleFactionsPlugin.ATTACKED_FACTIONS.replace(factionName, 120);
        }
        else
        {
            EagleFactionsPlugin.ATTACKED_FACTIONS.put(factionName, 120);
            runClaimingRestorer(factionName);
        }
    }

    @Override
    public void runClaimingRestorer(String factionName)
    {

        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(task ->
        {
            if(EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(factionName))
            {
                int seconds = EagleFactionsPlugin.ATTACKED_FACTIONS.get(factionName);

                if (seconds <= 0)
                {
                    EagleFactionsPlugin.ATTACKED_FACTIONS.remove(factionName);
                    task.cancel();
                }
                else
                {
                    EagleFactionsPlugin.ATTACKED_FACTIONS.replace(factionName, seconds, seconds - 1);
                }
            }
        }).submit(EagleFactionsPlugin.getPlugin());
    }

    @Override
    public void informAboutAttack(final Faction faction)
    {
        final List<Player> playersList = factionLogic.getOnlinePlayers(faction);
        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK)));
    }

    @Override
    public void informAboutDestroying(final Faction faction)
    {
        final List<Player> playersList = factionLogic.getOnlinePlayers(faction);
        playersList.forEach(x -> x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.ONE_OF_YOUR_CLAIMS_HAS_BEEN_DESTROYED_BY_AN_ENEMY)));
    }

    @Override
    public void blockHome(final UUID playerUUID)
    {
        if(EagleFactionsPlugin.BLOCKED_HOME.containsKey(playerUUID))
        {
            EagleFactionsPlugin.BLOCKED_HOME.replace(playerUUID, factionsConfig.getHomeBlockTimeAfterDeathInOwnFaction());
        }
        else
        {
            EagleFactionsPlugin.BLOCKED_HOME.put(playerUUID, factionsConfig.getHomeBlockTimeAfterDeathInOwnFaction());
            runHomeUsageRestorer(playerUUID);
        }
    }

    @Override
    public void runHomeUsageRestorer(final UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.SECONDS).execute(task ->
        {
            if (EagleFactionsPlugin.BLOCKED_HOME.containsKey(playerUUID))
            {
                int seconds = EagleFactionsPlugin.BLOCKED_HOME.get(playerUUID);

                if (seconds <= 0)
                {
                    EagleFactionsPlugin.BLOCKED_HOME.remove(playerUUID);
                    task.cancel();
                }
                else
                {
                    EagleFactionsPlugin.BLOCKED_HOME.replace(playerUUID, seconds, seconds - 1);
                }
            }
        }).submit(EagleFactionsPlugin.getPlugin());
    }

}

package io.github.aquerr.eaglefactions.common.managers;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.managers.PowerManager;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Singleton
public class PowerManagerImpl implements PowerManager
{
    private static PowerManagerImpl INSTANCE = null;

    private final EagleFactions plugin;
    private final PowerConfig powerConfig;

    private CommentedConfigurationNode _factionsNode;
    private final UUID dummyUUID = new UUID(0, 0);

    public static PowerManagerImpl getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new PowerManagerImpl(eagleFactions);
        else return INSTANCE;
    }

    private PowerManagerImpl(final EagleFactions eagleFactions)
    {
        INSTANCE = this;
        plugin = eagleFactions;
        powerConfig = eagleFactions.getConfiguration().getPowerConfig();
        Path configDir = eagleFactions.getConfigDir();

        try
        {
            _factionsNode = HoconConfigurationLoader.builder().setPath(Paths.get(configDir.resolve("data") + "/factions.conf")).build().load();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public int getFactionMaxClaims(final Faction faction)
    {
        float power = getFactionPower(faction);
        return (int)power;
    }

    @Override
    public float getPlayerPower(@Nullable final UUID playerUUID)
    {
        if (playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;
        return plugin.getPlayerManager().getPlayerPower(playerUUID);
    }

    @Override
    public float getFactionPower(final Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            return powerNode.getFloat(9999f);
        }

        float factionPower = 0;
        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionPower = factionPower + getPlayerPower(faction.getLeader());
        }
        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer: faction.getOfficers())
            {
                float officerPower = getPlayerPower(officer);
                factionPower =factionPower + officerPower;
            }
        }
        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member: faction.getMembers())
            {
                float memberPower = getPlayerPower(member);
                factionPower = factionPower + memberPower;
            }
        }
        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                float recruitPower = getPlayerPower(recruit);
                factionPower = factionPower + recruitPower;
            }
        }

        return factionPower;
    }

    @Override
    public float getFactionMaxPower(final Faction faction)
    {
        if(faction.getName().equals("SafeZone") || faction.getName().equals("WarZone"))
        {
            ConfigurationNode powerNode = _factionsNode.getNode("factions", faction.getName(), "power");

            return powerNode.getFloat(9999f);
        }

        float factionMaxPower = 0;

        if(faction.getLeader() != null && !faction.getLeader().toString().equals(""))
        {
            factionMaxPower = factionMaxPower + getPlayerMaxPower(faction.getLeader());
        }

        if(faction.getOfficers() != null && !faction.getOfficers().isEmpty())
        {
            for (UUID officer : faction.getOfficers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(officer);
            }
        }

        if(faction.getMembers() != null && !faction.getMembers().isEmpty())
        {
            for (UUID member : faction.getMembers())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(member);
            }
        }

        if(faction.getRecruits() != null && !faction.getRecruits().isEmpty())
        {
            for (UUID recruit: faction.getRecruits())
            {
                factionMaxPower = factionMaxPower + getPlayerMaxPower(recruit);
            }
        }

        return factionMaxPower;
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        if(playerUUID == null || playerUUID.equals(dummyUUID))
            return 0;

        return plugin.getPlayerManager().getPlayerMaxPower(playerUUID);
    }

    @Override
    public void addPower(final UUID playerUUID, final boolean isKillAward)
    {
        float playerPower = plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower + powerConfig.getPowerIncrement() < getPlayerMaxPower(playerUUID))
        {
            if(isKillAward)
            {
                float killAward = powerConfig.getKillAward();
                plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower + killAward);
            }
            else
            {
                float newPower = round(playerPower + powerConfig.getPowerIncrement(), 2);
                plugin.getPlayerManager().setPlayerPower(playerUUID, newPower);
            }
        }
    }

    public static float round(final float number, final int decimalPlace) {
        BigDecimal bd = new BigDecimal(number);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    @Override
    public void setPower(final UUID playerUUID, final float power)
    {
        plugin.getPlayerManager().setPlayerPower(playerUUID, power);
    }

    @Override
    public void startIncreasingPower(final UUID playerUUID)
    {
        Task.Builder taskBuilder = Sponge.getScheduler().createTaskBuilder();

        taskBuilder.interval(1, TimeUnit.MINUTES).execute(task ->
        {
            if (!plugin.getPlayerManager().isPlayerOnline(playerUUID)) task.cancel();

            if(getPlayerPower(playerUUID) + powerConfig.getPowerIncrement() < getPlayerMaxPower(playerUUID))
            {
                addPower(playerUUID, false);
            }
            else
            {
                setPower(playerUUID, getPlayerMaxPower(playerUUID));
            }
        }).async().submit(plugin);
    }

    @Override
    public void decreasePower(final UUID playerUUID)
    {
        float playerPower = plugin.getPlayerManager().getPlayerPower(playerUUID);

        if(playerPower - powerConfig.getPowerDecrement() > 0)
        {
                plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower - powerConfig.getPowerDecrement());
        }
        else
        {
            setPower(playerUUID, 0);
        }
    }

    @Override
    public void penalty(final UUID playerUUID)
    {
        float playerPower = plugin.getPlayerManager().getPlayerPower(playerUUID);
        float penalty = powerConfig.getPenalty();

        if(playerPower - penalty > 0)
        {
            plugin.getPlayerManager().setPlayerPower(playerUUID, playerPower - penalty);
        }
        else
        {
            plugin.getPlayerManager().setPlayerPower(playerUUID, 0);
        }
    }

    @Override
    public void setMaxPower(final UUID playerUUID, final float power)
    {
        plugin.getPlayerManager().setPlayerMaxPower(playerUUID, power);
    }
}

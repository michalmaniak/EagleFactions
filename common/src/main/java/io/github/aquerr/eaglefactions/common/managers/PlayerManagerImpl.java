package io.github.aquerr.eaglefactions.common.managers;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.common.storage.StorageManagerImpl;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManagerImpl implements PlayerManager
{
    private static PlayerManagerImpl INSTANCE = null;

    private final EagleFactions plugin;

    private final FactionsConfig factionsConfig;
    private final PowerConfig powerConfig;
    private final StorageManagerImpl storageManager;

    private UserStorageService userStorageService;

    private PlayerManagerImpl(final EagleFactions plugin)
    {
        INSTANCE = this;
        this.plugin = plugin;
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.storageManager = StorageManagerImpl.getInstance(plugin);

        Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
        optionalUserStorageService.ifPresent(x -> userStorageService = x);
    }

    public static PlayerManagerImpl getInstance(final EagleFactions eagleFactions)
    {
        if (INSTANCE == null)
            return new PlayerManagerImpl(eagleFactions);
        else return INSTANCE;
    }

    @Override
    public boolean addPlayer(final UUID playerUUID, final String playerName)
    {
        return storageManager.addPlayer(playerUUID, playerName, this.powerConfig.getStartingPower(), this.powerConfig.getGlobalMaxPower());
    }

    @Override
    public float getPlayerPower(final UUID playerUUID)
    {
        return storageManager.getPlayerPower(playerUUID);
    }

    @Override
    public boolean setPlayerPower(final UUID playerUUID, final float power)
    {
        return storageManager.setPlayerPower(playerUUID, power);
    }

    @Override
    public float getPlayerMaxPower(final UUID playerUUID)
    {
        return storageManager.getPlayerMaxPower(playerUUID);
    }

    @Override
    public boolean setPlayerMaxPower(final UUID playerUUID, final float maxpower)
    {
        return storageManager.setPlayerMaxPower(playerUUID, maxpower);
    }

    @Override
    public Optional<String> getPlayerName(final UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        if(oUser.isPresent())
            return Optional.of(oUser.get().getName());
        else
        {
            return getLastKnownPlayerName(playerUUID);
        }
    }

    @Override
    public FactionPlayer convertToFactionPlayer(final User user)
    {
        String factionName = "";
        FactionMemberType factionMemberType = null;
        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(user.getUniqueId());
        if (optionalFaction.isPresent())
        {
            factionName = optionalFaction.get().getName();
            factionMemberType = optionalFaction.get().getPlayerMemberType(user.getUniqueId());
        }
        return new FactionPlayerImpl(user.getName(), user.getUniqueId(), factionName, factionMemberType);
    }

    private Optional<String> getLastKnownPlayerName(final UUID playerUUID)
    {
        final String playerName = storageManager.getPlayerName(playerUUID);
        if(playerName.equals(""))
            return Optional.empty();
        return Optional.of(playerName);
    }

    @Override
    public Optional<Player> getPlayer(final UUID playerUUID)
    {
        final Optional<User> oUser = getUser(playerUUID);
        if(!oUser.isPresent())
            return Optional.empty();
        return oUser.get().getPlayer();
    }

    @Override
    public boolean isPlayerOnline(final UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);
        return oUser.map(User::isOnline).orElse(false);
    }

    @Override
    public Set<String> getServerPlayerNames()
    {
        return storageManager.getServerPlayerNames();
    }

    @Override
    public void setDeathInWarZone(final UUID playerUUID, final boolean didDieInWarZone)
    {
        storageManager.setDeathInWarzone(playerUUID, didDieInWarZone);
    }

    @Override
    public boolean lastDeathAtWarZone(final UUID playerUUID)
    {
       return storageManager.getLastDeathInWarzone(playerUUID);
    }

    @Override
    public boolean checkIfPlayerExists(final UUID playerUUID, final String playerName)
    {
        return storageManager.checkIfPlayerExists(playerUUID, playerName);
    }

    @Override
    @Nullable
    public FactionMemberType getFactionMemberType(final UUID playerUUID, final Faction faction)
    {
        if(faction.getLeader() != null && faction.getLeader().equals(playerUUID))
        {
            return FactionMemberType.LEADER;
        }
        else if(faction.getMembers().contains(playerUUID))
        {
            return FactionMemberType.MEMBER;
        }
        else if(faction.getOfficers().contains(playerUUID))
        {
            return FactionMemberType.OFFICER;
        }
        else if(faction.getRecruits().contains(playerUUID))
        {
            return FactionMemberType.RECRUIT;
        }
//        else if(faction.getAlliances().contains(factionPlayer.getUniqueId().toString()))
//        {
//            return FactionMemberType.ALLY;
//        }

        return null;
    }

    @Override
    public Set<FactionPlayer> getServerPlayers()
    {
        return this.storageManager.getServerPlayers();
//        final Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
//        final Set<FactionPlayer> factionPlayers = storageManager.getServerPlayers();
//        for(final Player player : onlinePlayers)
//        {
//            boolean playerExist = false;
//            for(final FactionPlayer factionPlayer : factionPlayers)
//            {
//                if(player.getUniqueId().equals(factionPlayer.getUniqueId()))
//                {
//                    playerExist = true;
//                    break;
//                }
//            }
//
//            if(!playerExist)
//                factionPlayers.add(new FactionPlayerImpl(player.getName(), player.getUniqueId(), null, null, 5, 10));
//        }
//
//        return factionPlayers;
    }

    @Override
    public void updatePlayerName(final UUID playerUUID, final String playerName)
    {
        this.storageManager.updatePlayerName(playerUUID, playerName);
    }

    private Optional<User> getUser(final UUID playerUUID)
    {
        final Optional<User> oUser = userStorageService.get(playerUUID);
        return oUser;
    }
}

package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.UnimplementedOperationException;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import net.simplyvanilla.simplyrank.data.GroupPermissionManager;
import net.simplyvanilla.simplyrank.data.PlayerData;
import net.simplyvanilla.simplyrank.data.PlayerPermissionManager;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SimplyRankPluginTest {
    private ServerMock server;
    private SimplyRankPlugin plugin;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(SimplyRankPlugin.class);
    }

    @Test
    void testPluginEnabled() {
        assertTrue(this.plugin.isEnabled());
    }

    @Test
    void testDefaultGroupExists() {
        assertTrue(this.plugin.getDataManager().groupExists("default"));
    }

    @Test
    void testPlayerHasDefaultGroupAndPermissionLoads() {
        PlayerMock player = this.server.addPlayer();
        PlayerData playerData = this.plugin.getDataManager().loadPlayerDataSync(player.getUniqueId());
        try {
            this.server.getScheduler().performTicks(1);
        } catch (UnimplementedOperationException ignored) {
            // this error comes because updateCommands is not implemented in MockBukkit
        }
        assertTrue(playerData.getPrimaryGroup().equals("default"));
        assertTrue(player.hasPermission("minecraft.command.me"));
        assertFalse(player.hasPermission("minecraft.command.tell"));
    }

    @Test
    void testPlayerPermissions() {
        PlayerMock player = this.server.addPlayer();

        PlayerPermissionManager playerPermissionManager = new PlayerPermissionManager(this.plugin, this.plugin.getDataManager());
        GroupPermissionManager groupPermissionManager = TestUtils.createDefaultGroupPermissions();
        PermissionApplier applier = new PermissionApplier(this.plugin.getDataManager(), playerPermissionManager, groupPermissionManager);


        PlayerData playerData = this.plugin.getDataManager().loadPlayerDataSync(player.getUniqueId());
        try {
            applier.applyPermission(player, playerData);
        } catch (UnimplementedOperationException ignored) {
            // this error comes because updateCommands is not implemented in MockBukkit
        }


        assertTrue(player.hasPermission("minecraft.command.me"));
        assertFalse(player.hasPermission("minecraft.command.tell"));
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }
}

package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.simplyvanilla.simplyrank.permission.PlayerDataService;
import net.simplyvanilla.simplyrank.database.group.GroupData;
import net.simplyvanilla.simplyrank.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.database.player.PlayerData;
import net.simplyvanilla.simplyrank.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class PlayerDataServiceTest {

    private ServerMock server;
    private MockPlugin plugin;
    private GroupRepository groupRepository;
    private PlayerDataRepository playerDataRepository;
    private PlayerDataService service;

    @BeforeEach
    public void setUp() {
        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.createMockPlugin();
        this.groupRepository = new GroupRepositoryMock();
        this.playerDataRepository = new PlayerDataRepositoryMock();
        this.service = new PlayerDataService(this.groupRepository, this.playerDataRepository);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testLoadPlayerDataAsyncWithDefaultGroup() {
        UUID uuid = UUID.randomUUID();
        PlayerData playerData = this.service.loadPlayerData(uuid);
        Assertions.assertEquals(PlayerData.getDefault().getPrimaryGroup(), playerData.getPrimaryGroup());
    }

    @Test
    void testLoadPlayerDataAsyncWithExtraGroup() {
        UUID uuid = UUID.randomUUID();
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.playerDataRepository.save(uuid, new PlayerData(List.of("admin")));

        PlayerData playerData = this.service.loadPlayerData(uuid);

        Assertions.assertEquals("admin", playerData.getPrimaryGroup());
    }

    @Test
    void testLoadPlayerDataSync() {
        UUID uuid = UUID.randomUUID();
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.playerDataRepository.save(uuid, new PlayerData(List.of("admin")));
        PlayerData playerData = this.service.loadPlayerData(uuid);

        Assertions.assertEquals("admin", playerData.getPrimaryGroup());
    }

    @Test
    void testLoadGroupDataSync() {
        GroupData createdGroupdata = new GroupData(NamedTextColor.WHITE, "admin");
        this.groupRepository.save("admin", createdGroupdata);
        GroupData groupData = this.service.loadGroupData("admin");

        Assertions.assertEquals(createdGroupdata, groupData);
    }

    @Test
    void testSavePlayerDataAsync() {
        UUID uuid = UUID.randomUUID();
        PlayerData playerData = new PlayerData(List.of("admin"));
        this.service.savePlayerData(uuid, playerData);
        Assertions.assertEquals(playerData, this.playerDataRepository.findById(uuid));
    }

    @Test
    void testSaveGroupDataAsync() {
        GroupData groupData = new GroupData(NamedTextColor.WHITE, "admin");
        this.service.saveGroupData("admin", groupData);
        Assertions.assertEquals(groupData, this.groupRepository.findByName("admin").orElse(null));
    }

    @Test
    void testGroupExists() {
        Assertions.assertFalse(this.service.groupExists("admin"));
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        Assertions.assertTrue(this.service.groupExists("admin"));
    }
}

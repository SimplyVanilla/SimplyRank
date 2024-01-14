package net.simplyvanilla.simplyrank;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.MockPlugin;
import be.seeseemelk.mockbukkit.ServerMock;
import net.kyori.adventure.text.format.NamedTextColor;
import net.simplyvanilla.simplyrank.callback.CallbackMock;
import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.database.group.GroupData;
import net.simplyvanilla.simplyrank.data.database.group.GroupRepository;
import net.simplyvanilla.simplyrank.data.database.player.PlayerData;
import net.simplyvanilla.simplyrank.data.database.player.PlayerDataRepository;
import net.simplyvanilla.simplyrank.database.GroupRepositoryMock;
import net.simplyvanilla.simplyrank.database.PlayerDataRepositoryMock;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
        this.service = new PlayerDataService(this.plugin, this.groupRepository, this.playerDataRepository);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testLoadPlayerDataAsyncWithDefaultGroup() {
        UUID uuid = UUID.randomUUID();
        CallbackMock<PlayerData, Exception> callback = CallbackMock.create();
        this.service.loadPlayerDataAsync(uuid, callback);

        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            this.server.getScheduler().performOneTick();
            return callback.isExecuted();
        });

        Assertions.assertTrue(callback.isSuccessCalled());
        Assertions.assertFalse(callback.isErrorCalled());
        Assertions.assertEquals(PlayerData.getDefault().getPrimaryGroup(), callback.getData().getPrimaryGroup());
    }

    @Test
    void testLoadPlayerDataAsyncWithExtraGroup() {
        UUID uuid = UUID.randomUUID();
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.playerDataRepository.save(uuid, new PlayerData(List.of("admin")));
        CallbackMock<PlayerData, Exception> callback = CallbackMock.create();
        this.service.loadPlayerDataAsync(uuid, callback);
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            this.server.getScheduler().performOneTick();
            return callback.isExecuted();
        });

        Assertions.assertTrue(callback.isSuccessCalled());
        Assertions.assertFalse(callback.isErrorCalled());
        Assertions.assertEquals("admin", callback.getData().getPrimaryGroup());
    }

    @Test
    void testLoadPlayerDataSync() {
        UUID uuid = UUID.randomUUID();
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        this.playerDataRepository.save(uuid, new PlayerData(List.of("admin")));
        PlayerData playerData = this.service.loadPlayerDataSync(uuid);

        Assertions.assertEquals("admin", playerData.getPrimaryGroup());
    }

    @Test
    void testLoadGroupDataSync() {
        GroupData createdGroupdata = new GroupData(NamedTextColor.WHITE, "admin");
        this.groupRepository.save("admin", createdGroupdata);
        GroupData groupData = this.service.loadGroupDataSync("admin");

        Assertions.assertEquals(createdGroupdata, groupData);
    }

    @Test
    void testSavePlayerDataAsync() {
        UUID uuid = UUID.randomUUID();
        PlayerData playerData = new PlayerData(List.of("admin"));
        CallbackMock<Void, Exception> callback = CallbackMock.create();
        this.service.savePlayerDataAsync(uuid, playerData, callback);
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            this.server.getScheduler().performOneTick();
            return callback.isExecuted();
        });

        Assertions.assertTrue(callback.isSuccessCalled());
        Assertions.assertFalse(callback.isErrorCalled());
        Assertions.assertEquals(playerData, this.playerDataRepository.findById(uuid));
    }

    @Test
    void testSaveGroupDataAsync() {
        GroupData groupData = new GroupData(NamedTextColor.WHITE, "admin");
        CallbackMock<Void, Exception> callback = CallbackMock.create();
        this.service.saveGroupDataAsync("admin", groupData, callback);
        Awaitility.await().atMost(2, TimeUnit.SECONDS).until(() -> {
            this.server.getScheduler().performOneTick();
            return callback.isExecuted();
        });

        Assertions.assertTrue(callback.isSuccessCalled());
        Assertions.assertFalse(callback.isErrorCalled());
        Assertions.assertEquals(groupData, this.groupRepository.findByName("admin").orElse(null));
    }

    @Test
    void testGroupExists() {
        Assertions.assertFalse(this.service.groupExists("admin"));
        this.groupRepository.save("admin", new GroupData(NamedTextColor.WHITE, "admin"));
        Assertions.assertTrue(this.service.groupExists("admin"));
    }
}
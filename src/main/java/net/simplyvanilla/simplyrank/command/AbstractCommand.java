package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.data.PlayerDataService;
import net.simplyvanilla.simplyrank.data.PermissionApplyService;

public abstract class AbstractCommand implements SubCommand {
    protected final CommandErrorMessages errorMessages;
    protected final PlayerDataService playerDataService;
    protected final PermissionApplyService permissionApplyService;

    protected AbstractCommand(
        CommandErrorMessages errorMessages,
        PlayerDataService playerDataService,
        PermissionApplyService permissionApplyService) {
        this.errorMessages = errorMessages;
        this.playerDataService = playerDataService;
        this.permissionApplyService = permissionApplyService;
    }

    @Override
    public CommandErrorMessages getErrorMessages() {
        return errorMessages;
    }

    @Override
    public PlayerDataService getDataManager() {
        return playerDataService;
    }

    @Override
    public PermissionApplyService getPermissionApplier() {
        return permissionApplyService;
    }
}

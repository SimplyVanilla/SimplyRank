package net.simplyvanilla.simplyrank.command;

import net.simplyvanilla.simplyrank.data.DataManager;
import net.simplyvanilla.simplyrank.utils.PermissionApplier;

public abstract class AbstractCommand implements SubCommand {
  protected final CommandErrorMessages errorMessages;
  protected final DataManager dataManager;
  protected final PermissionApplier permissionApplier;

  protected AbstractCommand(
      CommandErrorMessages errorMessages,
      DataManager dataManager,
      PermissionApplier permissionApplier) {
    this.errorMessages = errorMessages;
    this.dataManager = dataManager;
    this.permissionApplier = permissionApplier;
  }

  @Override
  public CommandErrorMessages getErrorMessages() {
    return errorMessages;
  }

  @Override
  public DataManager getDataManager() {
    return dataManager;
  }

  @Override
  public PermissionApplier getPermissionApplier() {
    return permissionApplier;
  }
}

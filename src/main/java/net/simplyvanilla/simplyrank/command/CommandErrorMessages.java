package net.simplyvanilla.simplyrank.command;

public class CommandErrorMessages {

  public String noPermission() {
    return "No permission";
  }

  public String useCorrectFormat() {
    return "Please use /simplyrank <create|set|add|get>";
  }

  public String createCommandFormatError() {
    return "Please use /simplyrank create <RANK_NAME> <COLOR> [PREFIX]";
  }

  public String setCommandFormatError() {
    return "Please use /simplyrank set <PLAYER_NAME> <RANK_NAME>";
  }

  public String addCommandFormatError() {
    return "Please use /simplyrank add <PLAYER_NAME> <RANK_NAME>";
  }

  public String getCommandFormatError() {
    return "Please use /simplyrank get <PLAYER_NAME>";
  }

  public String remCommandFormatError() {
    return "Please use /simplyrank add <PLAYER_NAME> <RANK_NAME>";
  }

  public String colorDoesNotExistError() {
    return "That color does not exist!";
  }

  public String cannotFindPlayerError() {
    return "Could not find player! (Neither by name, nor by UUID";
  }
}

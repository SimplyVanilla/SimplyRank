package net.simplyvanilla.simplyrank.command;

class CommandErrorMessages {

  public String noPermission() {
    return "No permission";
  }

  public String useCorrectFormat(String label) {
    return "Please use /%s <create|set|add|get>".formatted(label);
  }

  public String createCommandFormatError(String label) {
    return "Please use /%s create <RANK_NAME> <COLOR> [PREFIX]".formatted(label);
  }

  public String setCommandFormatError(String label) {
    return "Please use /%s set <PLAYER_NAME> <RANK_NAME>".formatted(label);
  }

  public String addCommandFormatError(String label) {
    return "Please use /%s add <PLAYER_NAME> <RANK_NAME>".formatted(label);
  }

  public String getCommandFormatError(String label) {
    return "Please use /%s get <PLAYER_NAME>".formatted(label);
  }

  public String remCommandFormatError(String label) {
    return "Please use /%s add <PLAYER_NAME> <RANK_NAME>".formatted(label);
  }

  public String colorDoesNotExistError() {
    return "That color does not exist!";
  }

  public String cannotFindPlayerError() {
    return "Could not find player! (Neither by name, nor by UUID";
  }
}

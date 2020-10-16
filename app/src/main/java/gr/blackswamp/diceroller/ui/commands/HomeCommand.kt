package gr.blackswamp.diceroller.ui.commands

sealed class HomeCommand {
    object ShowHelp : HomeCommand()
    class ShowNameDialog(val nextId: Int) : HomeCommand()
}
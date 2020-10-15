package gr.blackswamp.diceroller.ui.commands

sealed class HomeCommand() {
    class ShowNameDialog(val nextId: Int) : HomeCommand()
}
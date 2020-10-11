package gr.blackswamp.diceroller.ui

data class MainActivityState(
    val showDelete: Boolean = false,
    val showSave: Boolean = false,
    val d4times: Int = 0,
    val d6times: Int = 0,
    val d8times: Int = 0,
    val d10times: Int = 0,
    val d12times: Int = 0,
    val d20times: Int = 0,
) {

}
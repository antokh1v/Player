import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.fxml.FXMLLoader.load

class MPlayer : Application() {
    val layout = "player.fxml"

    override fun start(primaryStage: Stage?) {
        System.setProperty("prism.lcdtext", "false")
        primaryStage?.scene = Scene(load<Parent?>(MPlayer::class.java.getResource(layout)))
        primaryStage?.setTitle("XML App")
        primaryStage?.show()
    }


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(MPlayer::class.java)
        }
    }

}

import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSlider
import javafx.concurrent.Service
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.media.Media
import javafx.scene.media.MediaPlayer
import java.io.File
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.concurrent.WorkerStateEvent
import javafx.event.EventType
import javafx.scene.input.DragEvent
import java.awt.event.MouseEvent
import java.text.DecimalFormat

class MainController {
    @FXML
    lateinit var play: JFXButton
    lateinit var stop: JFXButton
    lateinit var slider2: JFXSlider
    lateinit var slider1: JFXSlider
    lateinit var time: Label
    lateinit var next: JFXButton
    var mediaPlayer: MediaPlayer? = null
    var selectedFile: File? = null
    fun initialize() {
        selectedFile = File("./resources/test.mp3")
        var url = selectedFile!!.toURI()
        mediaPlayer = MediaPlayer(Media(url.toString()))
        mediaPlayer?.volume = 0.015
        val service = object : Service<Void>() {
            protected override fun createTask(): Task<Void> {
                return object : Task<Void>() {
                    @Throws(Exception::class)
                    protected override fun call(): Void? {
                        println("Start playing")
                        while (mediaPlayer?.currentTime != mediaPlayer?.stopTime) {
                            val curMin = mediaPlayer?.currentTime?.toMinutes()?.toInt()
                            val curSec = mediaPlayer?.currentTime?.toSeconds()?.rem(60)?.toInt()
                            val Str = String.format("%02d",curSec)

                            Platform.runLater {
                                time.text = "$curMin:$Str"
                            }
                            slider1.value = (mediaPlayer?.currentTime!!.toSeconds() * 100 / mediaPlayer?.stopTime!!.toSeconds())

                            Thread.sleep(100)

                        }
                        println("Stop playing")
                        return null
                    }
                }
            }
        }

        mediaPlayer?.setOnPlaying {
            if(mediaPlayer?.currentTime?.toSeconds() == 0.0)
                service.start()
        }

        play.setOnAction {
            if (mediaPlayer?.status.toString() == "READY" || mediaPlayer?.status.toString() == "PAUSED") {
                mediaPlayer?.play()
            } else {
                mediaPlayer?.pause()
            }
        }

        slider2.valueProperty().addListener { _, _, newValue ->
            mediaPlayer?.volume = newValue.toDouble() / 100
        }



    }

}


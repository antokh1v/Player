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
import javafx.collections.MapChangeListener
import javafx.concurrent.WorkerStateEvent
import javafx.event.Event
import javafx.event.EventType
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseButton
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.text.DecimalFormat
import javafx.scene.media.MediaPlayer.Status
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.util.Duration
import javax.swing.JViewport
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


class MainController {
    @FXML
    lateinit var play: JFXButton
    lateinit var slider2: JFXSlider
    lateinit var slider1: JFXSlider
    lateinit var time: Label
    lateinit var next: JFXButton
    lateinit var title_song: Label
    lateinit var artist: Label
    lateinit var method: JFXButton
    lateinit var table: TableView<SongInf>
    lateinit var num: TableColumn<SongInf, Int>
    lateinit var name: TableColumn<SongInf, String>
    lateinit var dur: TableColumn<SongInf, String>
    lateinit var previous: JFXButton
    var mediaPlayer: MediaPlayer? = null
    var selectedFile: File? = null
    var media: Media? = null
    var duration: Duration? = null
    var PlayerList: MutableList<MediaPlayer> = mutableListOf<MediaPlayer>()
    val Folder: File = File("./resources")
    var fileChooser: FileChooser? = null
    var CurrentSongNum: Int = 0
    var currentPlayingSong: Int = 0

    companion object {
        var s: Stage? = null
    }

    fun initialize() {
        getMusic()
        fileChooser = FileChooser()
        fileChooser?.title = "Open File"
        mediaPlayer?.volume = 0.015
        PlayerList.forEachIndexed {ind, it ->
            it.currentTimeProperty()?.addListener { _ ->
                updateValues()
            }

            it.setOnReady {
                duration = it.stopTime
                title_song.text = it.media.metadata["title"].toString()
                artist.text = it.media.metadata["artist"].toString()
                time.text = "0:00"
                addSong(it, CurrentSongNum++)

            }

            it.setOnPlaying {
                duration = it.stopTime
                title_song.text = it.media.metadata["title"].toString()
                artist.text = it.media.metadata["artist"].toString()
                currentPlayingSong = ind
                table.selectionModel.select(currentPlayingSong)
            }

            it.setOnEndOfMedia {
                nextSong()
            }

        }

        mediaPlayer = PlayerList.first()
        num.cellValueFactory = PropertyValueFactory("num")
        name.cellValueFactory = PropertyValueFactory("name")
        dur.cellValueFactory = PropertyValueFactory("dur")

        next.setOnAction {
            nextSong()
        }

        previous.setOnAction {
            prevSong()
        }

        table.setOnMouseClicked { event ->
            if (event.button == MouseButton.PRIMARY && event.clickCount == 2) {
                var ind = table.selectionModel.selectedItem.num - 1
                mediaPlayer?.stop()
                slider1.value = 0.0
                mediaPlayer = PlayerList[ind]
                mediaPlayer?.play()
            }
        }

        method.setOnAction {
            selectedFile = fileChooser?.showOpenDialog(s)
            if (selectedFile != null) {
                val t_media = Media(selectedFile?.toURI().toString())
                PlayerList.add(MediaPlayer(t_media))
                PlayerList.last().setOnReady {
                    addSong(PlayerList.last(), PlayerList.size - 1)
                }
                PlayerList.last().setOnPlaying {
                    duration = PlayerList.last().stopTime
                    currentPlayingSong = PlayerList.size - 1
                    slider1.value = 0.0
                    title_song.text = PlayerList.last().media.metadata["title"].toString()
                    artist.text = PlayerList.last().media.metadata["artist"].toString()
                    table.selectionModel.select(currentPlayingSong)
                }
                PlayerList.last().currentTimeProperty().addListener{_->
                    updateValues()
                }

                PlayerList.last().setOnEndOfMedia {
                    nextSong()
                }

            }

        }

        play.setOnAction{
            val status = mediaPlayer?.status
            if (status == Status.READY || status == Status.PAUSED || status == Status.STOPPED) {
                mediaPlayer?.play()
            } else
                mediaPlayer?.pause()
        }



        slider2.valueProperty().addListener{ _, _, newValue ->
            mediaPlayer?.volume = newValue.toDouble() / 100
        }

        slider1.setOnMouseReleased{
            mediaPlayer?.seek(duration?.multiply(slider1.value / 100.0))
        }
    }

    fun updateValues() {
        Platform.runLater {
            val curTime = mediaPlayer?.currentTime
            val curMin = mediaPlayer?.currentTime?.toMinutes()?.toInt()
            val curSec = mediaPlayer?.currentTime?.toSeconds()?.rem(60)?.toInt()
            time.text = "$curMin:" + String.format("%02d", curSec)
            if (!slider1.isValueChanging && !slider1.isPressed)
                slider1.value = curTime!!.toSeconds() * 100.0 / duration!!.toSeconds()
        }
    }

    private fun getMusic() {
        for ((ind, file) in Folder.listFiles().withIndex()) {
            PlayerList.add(MediaPlayer(Media(file.toURI().toString())))
        }
    }

    fun addSong(file: MediaPlayer, ind: Int) {
        Thread(object : Task<SongInf>() {
            override fun call(): SongInf {
                return fetchData(file, ind)
            }

            override fun succeeded() {
                table.items.add(value)
            }
        }).start()

    }

    fun fetchData(file: MediaPlayer, ind: Int): SongInf {
        val curTime = file.stopTime
        val curMin = curTime.toMinutes().toInt()
        val curSec = curTime.toSeconds().rem(60).toInt()
        val time = "$curMin:" + String.format("%02d", curSec)
        return SongInf(ind + 1, file.media.metadata["title"].toString(), time)
    }

    fun nextSong(){
        if (currentPlayingSong + 1 < PlayerList.size){
            mediaPlayer?.stop()
            slider1.value = 0.0
            mediaPlayer = PlayerList[currentPlayingSong + 1]
            mediaPlayer?.play()
        }
    }
    fun prevSong(){
        if (currentPlayingSong - 1 >= 0){
            mediaPlayer?.stop()
            slider1.value = 0.0
            mediaPlayer = PlayerList[currentPlayingSong - 1]
            mediaPlayer?.play()
        }
    }


}

data class SongInf(val num: Int, val name: String, val dur: String)

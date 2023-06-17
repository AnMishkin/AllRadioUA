//package download.mishkindeveloper.AllRadioUA.data.entity
//
//import androidx.room.Entity
//import androidx.room.Index
//import androidx.room.PrimaryKey
//import java.io.Serializable
//
//@Entity(indices = [Index(value = ["name"], unique = true)])
//data class RadioWaveWithoutFavorite(
//    @PrimaryKey(autoGenerate = true)
//    var id: Int?,
//    var name: String?,
//    var image: String?,
//    var url: String?,
//    var fmFrequency: String?,
//    var isFavorite: Boolean, // новое поле
//    var custom:Boolean?,
//    var countOpen:Int?
//) : Serializable {
//    constructor() : this(null, null, null, null, null, false, false, 1)
//}

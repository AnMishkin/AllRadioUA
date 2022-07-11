package download.mishkindeveloper.AllRadioUA.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["name"], unique = true)])
class Track(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var name: String?,
    var station: String?,
    var date: String?,
    var image: String?
) {
    constructor() : this(null, null, null, null, null)

    override fun toString(): String {
        return "Track(id=$id, name=$name, station=$station, date=$date)"
    }
}
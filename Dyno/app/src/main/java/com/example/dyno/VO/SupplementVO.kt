package com.example.dyno.VO

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import com.example.dyno.LocalDB.RoomDAO
import com.google.gson.annotations.SerializedName

@Entity(tableName = "Supplement")
class SupplementVO() : Parcelable{

    @PrimaryKey
    @ColumnInfo(name="s_name")
    @SerializedName("s_name")
    var m_name: String = ""

    @ColumnInfo(name="s_company")
    @SerializedName("s_company")
    var m_company: String = ""

    @ColumnInfo(name="s_date")
    @SerializedName("s_date")
    var m_date: String = ""

    @ColumnInfo(name="s_ingredient")
    @SerializedName("s_ingredient")
    var m_ingredients : ArrayList<String> = arrayListOf()           // 기능성원재료

    @ColumnInfo(name="s_info")
    @SerializedName("s_info")
    var m_ingredients_info : ArrayList<String> = arrayListOf()      // 기능성내용, 위 배열과 사이즈가 같아야한다.


    constructor(name: String, company : String, date : String, ingredients : ArrayList<String>, infos : ArrayList<String>) : this() {
        this.m_company = company
        this.m_name=name
        this.m_ingredients=ingredients
        this.m_ingredients_info=infos
        this.m_date = date
    }

    constructor(parcel: Parcel) : this(parcel.readString()!!,parcel.readString()!!,parcel.readString()!!,
        parcel.readArrayList(String::class.java.classLoader) as ArrayList<String>,
        parcel.readArrayList(String::class.java.classLoader) as ArrayList<String>)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(m_name)
        parcel.writeString(m_company)
        parcel.writeString(m_date)
        parcel.writeList(m_ingredients)
        parcel.writeList(m_ingredients_info)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SupplementVO> {
        override fun createFromParcel(parcel: Parcel): SupplementVO {
            return SupplementVO(parcel)
        }

        override fun newArray(size: Int): Array<SupplementVO?> {
            return arrayOfNulls(size)
        }
    }


}

@Dao
interface SupplementDAO : RoomDAO<SupplementVO>{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSupplement(supplementVO: SupplementVO): Long
}
package com.example.dyno.View.MyPage.Detail

import android.annotation.SuppressLint
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dyno.LocalDB.RoomDB
import com.example.dyno.Network.RetrofitClient
import com.example.dyno.Network.RetrofitService
import com.example.dyno.View.MyPage.Detail.Adapters.DetailMAdapter
import com.example.dyno.R
import com.example.dyno.VO.*
import com.example.dyno.View.MyPage.DUR.DurActivity
import com.google.android.gms.common.util.ArrayUtils
import kotlinx.android.synthetic.main.activity_detail_medicine.*
import retrofit2.Call
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class DetailMedicineActivity : AppCompatActivity() {
    private lateinit var medicines : MutableList<MedicineVO>
    private val TAG = this::class.java.simpleName
    private lateinit var data : DiseaseVO
    private lateinit var  durItems:ArrayList<DurMMTestVO>
    private lateinit var  userDurItem:ArrayList<DurVO>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_medicine)

        getData()
        setView()

        m_dur_btn.setOnClickListener {
            val intent = Intent(this,DurActivity::class.java)
            intent.putExtra("DATA_DISEASE",data)
            startActivity(intent)
        }

    }

    // 복용 마지막 날짜를 계산하는 함수
    /*fun calculateEdate(medicines : ArrayList<MedicineVO>, sDate : String) : String {
        // date format은 YYYY-MM-dd
        // medicines 중 투약일 수(total)가 제일 긴 것 = maxPeriod
        // sDate + maxPeriod
        var maxPeriod = -1
        for(medicineModel in medicines){
            if(maxPeriod < medicineModel.total)
                maxPeriod = medicineModel.total
        }
        // sDate에 더하기를 할 수 있게 LocalDate로 바꾸는 작업.
        val localDate =
            // API Level 26이상부터 가능.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDate.parse(sDate, DateTimeFormatter.ISO_DATE)      // ISO_DATE : YYYY-mm-dd
            }
            // 보류.
            else {
                TODO("VERSION.SDK_INT < O")
            }

        // 더하기 후 리턴.
        val addedDate = localDate.plusDays(maxPeriod.toLong())
        return addedDate.toString()
    }*/

    @SuppressLint("SimpleDateFormat")
    private fun getData(){

        /* From RegistMedicineActivity / MyPageActivity(MedicineAdapter) */
        data = intent.getParcelableExtra("DATA_DISEASE")
        //Log.d(TAG,"${data.d_name},${data.d_company}")

        // 등록일자에 값이 없는 경우 = RegistSupplementActivty로부터 데이터를 받아온 경우
        if(data.d_date==""){
            // 1. d_date(등록일자)를 현재 시간으로 셋팅
            // 오레오 이상 SDK 28
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val now = LocalDateTime.now()
                data.d_date = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            }
            //그 이하
            else{
                val today = SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Date())
                data.d_date = today
            }

            // 2. 로컬 DB에 저장(Room)
            insertLocalDB()

            getDurInof()
        }
    }

    private fun insertLocalDB(){
        Log.d(TAG,"RoomDB 접근")
        // DB 싱글톤으로 생성.
        val localDB = RoomDB.getInstance(this)
        localDB.diseaseDAO().insertDisease(data)
        Toast.makeText(this,"나의 의약품에 추가했습니다.", Toast.LENGTH_SHORT).show()

        // DB 닫기.
        RoomDB.destroyInstance()
    }

    private fun setView(){
        //UI
        detail_m_name.text = data.d_name                                             // 질병 명
        detail_m_sdate.text = data.d_date                                             // 등록날짜(복용시작날짜)
        //detail_m_edate.text = calculateEdate(data.medicines,data.date)              // 등록날짜 + 의약품 중 제일 긴 복용기간 = 복용끝날짜
        recycler_detail_m.adapter = DetailMAdapter(this,data.d_medicines)     // RecyclerView Adapter
        recycler_detail_m.layoutManager = LinearLayoutManager(this)         // 이거 해줘야 레이아웃 보임.
    }
    private fun getDurInof(){
        val retrofit =RetrofitClient.getInstance()
        val durService=retrofit.create(RetrofitService::class.java)
        val localDB = RoomDB.getInstance(this)
        medicines = data.d_medicines
 
        for(i in medicines){
            Log.d(TAG,"dur서버로 보내는 데이터 : $i")
            durService.requestDurMM(i.name).enqueue(object : retrofit2.Callback<ArrayList<DurMMTestVO>>{
                override fun onFailure(call: Call<ArrayList<DurMMTestVO>>, t: Throwable) {
                    Log.d(TAG,"실패 {$t}")

                }
                override fun onResponse(
                    call: Call<ArrayList<DurMMTestVO>>,
                    response: Response<ArrayList<DurMMTestVO>>
                ) {
                    for(dur in response.body()!!){
                        Log.d(TAG,dur.printDetail())
                        dur.dDate=data.d_date
                        durItems.add(dur)
                    }
                }
                
            })
        }
        if(durItems.size==0){
            Log.d(TAG,"dur 없음")
        }else{
            val diseaseList: List<DiseaseMinimal> = localDB.diseaseDAO().getDiseaseMinimal()
            for(item in diseaseList){//처방전마다
                var durMMList:ArrayList<DurMMTestVO> = ArrayList()
                var check=0
                for(med in item.d_medicines){//의약품 하나하나
                    for(dur in durItems){
                        if(med.name==dur.durName){//병용하면 안됨
                            Log.d(TAG,"병용함 안됨:"+med.name+"/"+dur.mName)
                            durMMList.add(dur)
                            check=1
                        }else{//병용가능
                            Log.d(TAG,"병용가능")
                        }
                    }
                }
                val today = SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(Date())
                var durVO=DurVO(today, data.d_date, data.d_name, item.d_date,
                    item.d_name,"",durMMList,ArrayList(),check)
                userDurItem.add(durVO)
            }

        }

    }



}


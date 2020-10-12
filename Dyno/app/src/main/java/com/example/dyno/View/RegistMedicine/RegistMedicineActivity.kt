package com.example.dyno.View.MyPage.RegistMedicine

import android.annotation.SuppressLint
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.dyno.OCR.OcrParsing
import com.example.dyno.OCR.OcrProc
import com.example.dyno.R
import com.example.dyno.Network.RetrofitClient
import com.example.dyno.Network.RetrofitService
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import java.io.File
import kotlin.collections.ArrayList

class RegistMedicineActivity : AppCompatActivity() {

    private val OCR_API_GW_URL ="https://4613fa1b45164de0814a2450c31bfc1c.apigw.ntruss.com/custom/v1/3398/2065ad05effce12ce5c7cb354380e6a13c219ae2f00c996d31988fe0eeb4c844/general"
    private val OCR_SECRET_KEY = "SGhKZ3pERXpHQnZWZEtpQlVaeHJqb2JoZWlVaUpWcW4="
    private lateinit var imgFilePath : String
    private lateinit var imgFile : File
    private val TAG = this::class.java.simpleName


    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regist_medicine)

        // 1. CameraActivity로부터 온 파일 저장
        initFile()

        // 2. OCR(글자인식) -> 3. Parsing
        ClovaOcrTask().execute(OCR_API_GW_URL,OCR_SECRET_KEY,imgFilePath)

        // -> 4. Server로부터 의약품 정보 획득. (순서대로일어나게) ArrayList<MedicineVO>

        // UI 뿌려지기
        // 질병 추측 -> DiseaseVO


    }

    private fun initFile(){
        imgFilePath = intent.getStringExtra("bitmapImg")
        imgFile = File(imgFilePath)
    }

    @SuppressLint("StaticFieldLeak")
    inner class ClovaOcrTask : AsyncTask<String, String, String>() {
        override fun doInBackground(vararg params: String): String? {
            return OcrProc()
                .start(params[0], params[1], params[2])
        }

        override fun onPostExecute(result: String?) {
            if (result != null) {
                Log.d("json", result.length.toString())
                val ocrPasredData = getResultParsed(result)!!
                getMedicine(ocrPasredData)

            } else {
                Log.d("no_result", "")
            }
        }

        // 인식한 결과 파싱
        private fun getResultParsed(result: String) : ArrayList<String>? {
            var translateText = ""
            try {
                // 인식된 결과를 Json으로 변환
                val jsonObject = JSONObject(result)
                // images -> fields -> inferText
                val jsonArray = jsonObject.getJSONArray("images")
                for (i in 0 until jsonArray.length()) {
                    val jsonArrayFields = jsonArray.getJSONObject(i).getJSONArray("fields")
                    for (j in 0 until jsonArrayFields.length()) {
                        val inferText = jsonArrayFields.getJSONObject(j).getString("inferText")
                        translateText += inferText
                        translateText += "/"
                    }
                }
                // 파싱된 결과를 리턴 ArrayList<String>
                return OcrParsing().prescriptionDrugsR(translateText)

            } catch (e: Exception) {
                Log.d("parse_error", e.toString())
            }
            return null
        }

        private fun getMedicine(data : ArrayList<String>){
            val retrofit = RetrofitClient.getInstance()
            val medicineService = retrofit.create(RetrofitService::class.java)

            for(d in data)
                Log.d(TAG,"데이터 - $d")

            medicineService.requestListM(data).enqueue(object : retrofit2.Callback<String> {
                override fun onFailure(call: Call<String>, t: Throwable) {
                    Log.d(TAG,"실패 {$t}")
                }

                override fun onResponse(call: Call<String>, response: Response<String>) {
                    Log.d(TAG,"성공^^")
                    Log.d(TAG,"${response.body()}")
                }

            })
        }


    }
}
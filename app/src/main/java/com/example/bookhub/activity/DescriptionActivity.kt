package com.example.bookhub.activity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.database.BookDatabase
import com.example.bookhub.database.BookEntity
import com.example.bookhub.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_description.*
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

class DescriptionActivity : AppCompatActivity() {

    lateinit var progressBar : ProgressBar
    lateinit var progressLayout : RelativeLayout
    lateinit var txtBookName : TextView
    lateinit var txtBookAuthor : TextView
    lateinit var txtBookPrice : TextView
    lateinit var txtBookRating : TextView
    lateinit var imgBookImage : ImageView
    lateinit var desc : TextView
    lateinit var favBtn : Button
    var bookId : String? ="100"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_description)

        txtBookName = findViewById(R.id.txtBookName)
        txtBookAuthor = findViewById(R.id.txtBookAuthor)
        txtBookPrice = findViewById(R.id.txtBookPrice)
        txtBookRating = findViewById(R.id.txtBookRating)
        imgBookImage = findViewById((R.id.imgBookImage))
        progressBar = findViewById(R.id.progressBar)
        progressLayout = findViewById(R.id.progressLayout)
        desc = findViewById(R.id.description)
        favBtn = findViewById(R.id.btnfav)

        progressLayout.visibility= View.VISIBLE
        progressBar.visibility = View.VISIBLE


        if(intent != null)
        {
            bookId=intent.getStringExtra("book_id")
        }else{
            finish()
            Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occured",Toast.LENGTH_LONG).show()
        }


        if(bookId=="100"){
            finish()
            Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occured",Toast.LENGTH_LONG).show()
        }
        progressLayout.visibility =View.GONE

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        val url ="http://13.235.250.119/v1/book/get_book/"

        val jsonParams = JSONObject()
        jsonParams.put("book_id",bookId)


        if(ConnectionManager().checkConnectivity(this@DescriptionActivity)){

            val jsonRequest = object : JsonObjectRequest(Request.Method.POST,url,jsonParams,
                Response.Listener {

                    try {
                        val success = it.getBoolean("success")
                        if(success)
                        {
                            val bookJsonObject = it.getJSONObject("book_data")
                            progressLayout.visibility =View.GONE

                            val bookImageUrl = bookJsonObject.getString("image")
                            Picasso.get().load(bookJsonObject.getString("image")).error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text =bookJsonObject.getString("name")
                            txtBookAuthor.text =bookJsonObject.getString("author")
                            txtBookPrice.text =bookJsonObject.getString("price")
                            txtBookRating.text =bookJsonObject.getString("rating")
                            desc.text = bookJsonObject.getString("description")

                            val bookEntity = BookEntity(
                                bookId?.toInt() as Int,
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookPrice.text.toString(),
                                txtBookRating.text.toString(),
                                desc.text.toString(),
                                bookImageUrl
                            )

                            val checkFav = DBAsyncTask(applicationContext,bookEntity,1).execute()
                            val isFav = checkFav.get()
                            if(isFav){
                                btnfav.text= "Remove From Favourites"
                                val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavourite)
                                btnfav.setBackgroundColor(favColor)
                            }else{
                                btnfav.text= "Add to Favourites"
                                val favColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                btnfav.setBackgroundColor(favColor)
                            }

                            btnfav.setOnClickListener {
                                if(!isFav){
                                    val async = DBAsyncTask(applicationContext,bookEntity,2).execute()
                                    val result = async.get()
                                    if(result){
                                        Toast.makeText(this@DescriptionActivity, "Successfully added",Toast.LENGTH_LONG).show()
                                        btnfav.text= "Remove From Favourites"
                                        val favColor = ContextCompat.getColor(applicationContext, R.color.colorFavourite)
                                        btnfav.setBackgroundColor(favColor)
                                    }else{
                                        Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occurred",Toast.LENGTH_LONG).show()
                                    }

                                }else{
                                    val async = DBAsyncTask(applicationContext,bookEntity,3).execute()
                                    val result = async.get()
                                    if(result){
                                        Toast.makeText(this@DescriptionActivity, "Successfully Removed",Toast.LENGTH_LONG).show()
                                        btnfav.text= "Add to Favourites"
                                        val favColor = ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                                        btnfav.setBackgroundColor(favColor)
                                    }else{
                                        Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occurred",Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }else{
                            Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occurred",Toast.LENGTH_LONG).show()
                        }

                    }catch (e: Exception) {
                        Toast.makeText(this@DescriptionActivity, "Some Unexpected Error Occurred",Toast.LENGTH_LONG).show()
                    }

                },Response.ErrorListener {
                    Toast.makeText(this@DescriptionActivity, "Volley Error Occurred : $it",Toast.LENGTH_LONG).show()

                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String,String>()
                    headers["Content-type"] =  "application/java"
                    headers["token"] = "9a5b7e4b6805f2"
                    return headers
                }
            }
            queue.add(jsonRequest)

        }else{
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }
            dialog.setNegativeButton("Cancel") { _, _ ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()

        }
    }

    class DBAsyncTask(val context: Context,val bookEntity: BookEntity, val mode: Int) : AsyncTask<Void, Void, Boolean>(){
        /*
        Mode 1-> check DB if the book is favourite or not
        Mode 2-> save the book info to favourites
        Mode 3-> Remove the book from favourites
         */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db" ).build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            when(mode)
            {
                1-> {
                    val book: BookEntity? =db.bookDao().getBookById(bookEntity.book_id.toString())
                    db.close()
                    return book != null
                }
                2 ->{
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3->{
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }

            }
            return false
        }
    }
}

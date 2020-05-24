package com.example.bookhub.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.bookhub.R
import com.example.bookhub.adapter.DashboardRecyclerAdapter
import com.example.bookhub.model.Book
import com.example.bookhub.util.ConnectionManager
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.json.JSONException

class DashboardFragment : Fragment() {

    lateinit var recyclerDashboard : RecyclerView
    lateinit var layoutManager : RecyclerView.LayoutManager

    lateinit var progressBar : ProgressBar
    lateinit var progressLayout : RelativeLayout

    var bookInfoList : ArrayList<Book> = ArrayList()


    lateinit var recyclerAdapter : DashboardRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =inflater.inflate(R.layout.fragment_dashboard, container, false)

        recyclerDashboard= view.findViewById(R.id.recyclerDashboard)

        progressLayout = view.findViewById(R.id.progressLayout)
        progressBar = view.findViewById(R.id.progressBar)

        progressLayout.visibility = View.VISIBLE


        layoutManager = LinearLayoutManager(activity)



        val queue = Volley.newRequestQueue(activity as Context)
        val url = "http://13.235.250.119/v1/book/fetch_books/"

        if(ConnectionManager().checkConnectivity(activity as Context)){
            val jsonObjectRequest = object : JsonObjectRequest(Method.GET,url,null,
                Response.Listener {
                    // Handel Response
                    try {
                        progressLayout.visibility =View.GONE
                        val success = it.getBoolean("success")
                        if (success){
                            val data= it.getJSONArray("data")
                            for(i in 0 until data.length()){
                                val bookJsonObject = data.getJSONObject(i)
                                val bookObject = Book(
                                    bookJsonObject.getString("book_id"),
                                    bookJsonObject.getString("name"),
                                    bookJsonObject.getString("author"),
                                    bookJsonObject.getString("rating"),
                                    bookJsonObject.getString("price"),
                                    bookJsonObject.getString("image")
                                )
                                bookInfoList.add(bookObject)

                                recyclerAdapter = DashboardRecyclerAdapter(activity as Context, bookInfoList)

                                recyclerDashboard.adapter = recyclerAdapter
                                recyclerDashboard.layoutManager = layoutManager


                            }
                        }else{
                            Toast.makeText(activity as Context,"Some Error Occured !!!",Toast.LENGTH_LONG).show()
                        }

                    }catch (e: JSONException){
                        Toast.makeText(activity as Context, "Some Unexpected Error Occured !!!",Toast.LENGTH_LONG).show()
                    }

                },Response.ErrorListener {
                    //Handel Error
                    Toast.makeText(activity as Context, "Volly Error Occured !!!",Toast.LENGTH_LONG).show()
                }){
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String,String>()
                    headers["Content-type"] = "application/json"
                    headers["token"] ="9a5b7e4b6805f2"
                    return headers
                }
            }

            queue.add(jsonObjectRequest)

        }else{
            val dialog = AlertDialog.Builder(activity as Context)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            dialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                activity?.finish()
            }
            dialog.setNegativeButton("Cancel") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            dialog.create()
            dialog.show()

        }



        return view
    }

}

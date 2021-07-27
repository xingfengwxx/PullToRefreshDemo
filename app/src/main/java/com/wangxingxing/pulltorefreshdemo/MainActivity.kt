package com.wangxingxing.pulltorefreshdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private var mPullRefreshView: PullToRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()
    }

    private fun initView() {
        mPullRefreshView = findViewById(R.id.pull_refresh)
        val recyclerView: RecyclerView = mPullRefreshView?.getContentView() as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = MyAdapter()
        mPullRefreshView?.setOnRefreshListener(object : PullToRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                mPullRefreshView?.postDelayed({
                    mPullRefreshView?.refreshComplete()
                }, 1000)
            }

        })
    }

    internal class MyAdapter : RecyclerView.Adapter<MyVH>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyVH {
            val context = parent.context
            val view = LayoutInflater.from(context)
                .inflate(R.layout.layout_refresh_item, parent, false)
            return MyVH(view)
        }

        override fun onBindViewHolder(holder: MyVH, position: Int) {

        }

        override fun getItemCount(): Int {
            return 10
        }

    }

    internal class MyVH(itemView: View) : RecyclerView.ViewHolder(itemView)
}
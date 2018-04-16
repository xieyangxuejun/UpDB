package com.foretree.updb

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.foretree.updb.db.greendao.DaoSession
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private var daoSession: DaoSession? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        daoSession = DatabaseManager.getDaoSession()
        showAllUser()
    }

    fun onAdd(view: View) {
        daoSession!!
                .userDao
                .insert(User(null, "xieyang", 100, "programmer", false, 1, "1", java.util.Date()))
        showAllUser()
    }

    fun onDeleteAll(view: View) {
        daoSession!!.userDao.deleteAll()
        Toast.makeText(this, "delete all", Toast.LENGTH_SHORT).show()
        showAllUser()
    }

    fun showAllUser() {
        var string = ""
        daoSession?.userDao.let {
            if (it == null) return@let
            it.queryBuilder().list().forEach {
                string += it.toString() + "\n"
            }
        }
        text.text = string
    }
}

package com.example.myservice

import RetrofitInstance
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.myservice.theme.MyServiceAppTheme
import com.example.myservice.ui.navigation.MyServiceAppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RetrofitInstance.init(this)
        setContent {
            MyServiceAppTheme {
                MyServiceAppNavigation()
            }
        }
    }
}
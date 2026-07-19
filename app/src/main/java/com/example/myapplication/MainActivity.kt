package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.myapplication.ui.screen.TownHubScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 开启沉浸式无边框适配
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // 启动西幻城镇/酒馆主大厅
                TownHubScreen()
            }
        }
    }
}

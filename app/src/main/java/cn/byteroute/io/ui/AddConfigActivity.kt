package cn.byteroute.io.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.byteroute.io.databinding.ActivityAddConfigBinding

class AddConfigActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddConfigBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddConfigBinding.inflate(getLayoutInflater());
        setContentView(binding.root)
        binding.root
    }
}
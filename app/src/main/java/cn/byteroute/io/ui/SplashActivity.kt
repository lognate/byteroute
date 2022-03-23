package cn.byteroute.io.ui

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import cn.byteroute.io.BuildConfig
import cn.byteroute.io.R
import cn.byteroute.io.base.BaseActivity
import cn.byteroute.io.base.BaseViewModel
import cn.byteroute.io.ext.gone
import cn.byteroute.io.ext.visible
import cn.byteroute.io.http.HttpService
import cn.byteroute.io.http.OutrangeService
import cn.byteroute.io.ui.widget.UpdateDialog
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.android.synthetic.main.dialog_update.*

class SplashActivity : BaseActivity<BaseViewModel>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val alpha = ObjectAnimator.ofFloat(iv_logo, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(iv_logo, "scaleX", 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(iv_logo, "scaleY", 0f, 1f)
        AnimatorSet().apply {
            interpolator = DecelerateInterpolator()
            duration = 3000
            playTogether(alpha, scaleX, scaleY)
            start()
        }
        val tAlpha = ObjectAnimator.ofFloat(tv_splash, "alpha", .5f, 1f)
        val tScaleX = ObjectAnimator.ofFloat(tv_splash, "scaleX", .5f, 1f)
        val tScaleY = ObjectAnimator.ofFloat(tv_splash, "scaleY", .5f, 1f)
        AnimatorSet().apply {
            interpolator = DecelerateInterpolator()
            duration = 3000
            playTogether(tAlpha, tScaleX, tScaleY)
            start()
            addListener(object : Animator.AnimatorListener{
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    Intent(this@SplashActivity,MainActivity::class.java).apply {
                        startActivity(this)
                        finish()
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            })
        }
        viewModel?.dataResponse(this, {
            HttpService.instance(OutrangeService::class.java).getUpdate()
        }, {
            it?.apply {
                var info = this
                Log.d("SplashActivity", "onCreate: $info")
                if (versionCode > BuildConfig.VERSION_CODE) {//更新提示
                    UpdateDialog(this@SplashActivity).apply {
                        when (!force) {
                            true -> tvCancel.gone
                            else -> tvCancel.visible
                        }
                        tv_version.text = info.version
                        tv_date.text = "更新日期:${info.updateDate}"
                        tv_weight.text = "大小:${info.weight}"
                        tv_content.text = info.updateContent
                        tvCancel.setOnClickListener {
                            dismiss()
                        }
                        tvConfirm.setOnClickListener {

                        }
                        //show()
                    }
                }
            }
        })
    }
}
package jp.kuluna.eventgridview

import android.annotation.SuppressLint
import android.content.Context
import android.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import jp.kuluna.eventgridview.databinding.ViewCounterBinding
import java.util.*

/**
 * イベントを1列内に表示するためのView
 * @param context Android Context
 */
@SuppressLint("ViewConstructor")
open class CounterColumnView(context: Context) : FrameLayout(context) {

    /** ディスプレイの密度取得 (この値にdpを掛けるとpxになる) */
    private val density = context.resources.displayMetrics.density
    /** RecyclerViewにおけるこのViewの現在のPosition */
    private var layoutPosition = 0
    private var counters: MutableList<Counter> = mutableListOf()
    /** EventViewの格納用 */
    var counterViews = mutableListOf<View>()

    init {
        layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT).apply {
            setMargins(density.toInt(), 0, density.toInt(), 0)
        }
    }

    /**
     * イベントを表示します。
     * @param day 表示する日
     * @param counters カウンターリスト
     * @param layoutPosition RecyclerViewから見たLayoutPosition
     */
    fun set(day: Date, counters: List<Counter>, layoutPosition: Int) {
        removeAllViews()
        this.layoutPosition = layoutPosition
        this.counters = counters.toMutableList()

        val inflater = LayoutInflater.from(context)

        counters.forEachIndexed { index, counter ->
            val binding = DataBindingUtil.inflate<ViewCounterBinding>(inflater, R.layout.view_counter, null, false)
            binding.counter = counter

            // 開始位置と高さを設定
            // 翌日にまたぐものなのかそうでないかで計算が大きく変わる
            val (fromY, y) = when (counter.getCrossOverType(day)) {
                Counter.CrossOver.None -> {
                    val startParams = getParams(counter.start)
                    val endParams = getParams(counter.end)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Counter.CrossOver.ToNextDay -> {
                    val startParams = getParams(counter.start)
                    val endParams = getParams(counter.end, 1)
                    startParams.fromY to endParams.fromY - startParams.fromY
                }

                Counter.CrossOver.FromPreviousDay -> {
                    val endParams = getParams(counter.end)
                    0 to endParams.fromY
                }
            }

            // マージン指定
            val marginParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (y * density).toInt()).apply {
                topMargin = (fromY * density).toInt()
            }

            addView(binding.root, FrameLayout.LayoutParams(marginParams))
        }
    }

    private fun getParams(date: Date, addDays: Int = 0): TimeParams {
        val cal = Calendar.getInstance().apply { time = date }
        return TimeParams(cal[Calendar.HOUR_OF_DAY] + (addDays * 24), cal[Calendar.MINUTE])
    }
}
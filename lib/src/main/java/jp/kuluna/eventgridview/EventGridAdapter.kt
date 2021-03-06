package jp.kuluna.eventgridview

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.view_event.view.*
import java.util.*

/**
 * EventGridView用Adapter
 * @param context [Context]
 */
open class EventGridAdapter(val context: Context, private val draggable: Boolean = false) : RecyclerView.Adapter<EventGridViewHolder>() {
    /** Eventのクリックイベント */
    var onEventClickListener: OnEventClickListener? = null
    /** Eventをドラッグしたことによる変更イベント */
    var onEventChangedListener: OnEventChangedListener? = null

    private var events = emptyList<Event>()
    private var group = emptyList<Pair<Int, List<Event>>>()
    private var day = Date()

    /** EventViewColumnで生成されたのEventView格納用 */
    private var eventViews = mutableListOf<View>()
    /** ViewHolder全体のEventViewの配列の格納用 */
    private var eventViewGroup = mutableListOf<List<View>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventGridViewHolder = EventGridViewHolder(EventColumnView(context, draggable))

    override fun getItemCount(): Int = group.size

    override fun onBindViewHolder(holder: EventGridViewHolder, position: Int) {
        val event = group[holder.layoutPosition].second
        holder.view.set(day, event, holder.layoutPosition)
        holder.view.onEventClickListener = {
            onEventClickListener?.onEventClick(it)
        }
        holder.view.onEventChangedListener = { old, new , hideAll ->
            onEventChangedListener?.onChange(old, new)

            // イベント長さ調整ボタンを全て非表示
            if(hideAll) {
                hideAllAdjustButton()
            }
        }
        eventViews = holder.view.eventViews
        eventViewGroup.add(eventViews)
    }

    /**
     * 今のイベントを全てクリアして引数で渡すイベントに差し替えます。
     * @param events イベントリスト
     */
    fun replace(events: List<Event>, day: Date) {
        this.events = events
        this.group = this.events.groupBy { it.groupId }.toList()
        this.day = day

        notifyDataSetChanged()
    }

    /**
     * 全てのEventViewの長さ調整ボタンを非表示にします
     */
    fun hideAllAdjustButton() {
        eventViewGroup.forEach { views ->
            views.forEach {  view ->
                view.topAdjust.visibility = View.INVISIBLE
                view.bottomAdjust.visibility = View.INVISIBLE
            }
        }
    }
}

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.sillylife.knocknock.utils.ImageManager


class CommonViewPagerAdapter(val context: Context, val layout:Int, private val list: List<String>) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun instantiateItem(parent: ViewGroup, position: Int): Any {
        val item = list[position]
        val layout= LayoutInflater.from(context).inflate(layout, parent, false)
//        ImageManager.loadImage(layout.productImage, item)
        parent.addView(layout)
        return layout
    }

    override fun destroyItem(parent: ViewGroup, position: Int, view: Any) {
        parent.removeView(view as View)
    }

}
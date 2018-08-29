package com.familyflavors.familyflavors

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.familyflavors.familyflavors.R.id.profilePhotoImageView
import com.familyflavors.familyflavorss.MealForListView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_fragment.*


class MealAdapter(private val context: Context,
                  private val dataSource: ArrayList<MealForListView>) : BaseAdapter() {

  private val inflater: LayoutInflater
      = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

  companion object {
      private val LABEL_COLORS = hashMapOf(
              "Breakfast" to R.color.colorBreakfast,
              "Lunch" to R.color.colorLunch,
              "Dinner" to R.color.colorDinner,
              "Other" to R.color.colorOther
      )
  }

  override fun getCount(): Int {
    return dataSource.size
  }

  override fun getItem(position: Int): Any {
    return dataSource[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view: View
    val holder: ViewHolder

    // 1
    if (convertView == null) {

      // 2
      view = inflater.inflate(R.layout.list_item_meal, parent, false)

      // 3
      holder = ViewHolder()
      holder.thumbnailImageView = view.findViewById(R.id.meal_list_thumbnail) as ImageView
      holder.titleTextView = view.findViewById(R.id.meal_list_title) as TextView
      holder.subtitleTextView = view.findViewById(R.id.meal_list_subtitle) as TextView
      holder.detailTextView = view.findViewById(R.id.meal_list_detail) as TextView

      // 4
      view.tag = holder
    } else {
      // 5
      view = convertView
      holder = convertView.tag as ViewHolder
    }

    // 6
    val titleTextView = holder.titleTextView
    val subtitleTextView = holder.subtitleTextView
    val detailTextView = holder.detailTextView
    val thumbnailImageView = holder.thumbnailImageView



    val meal = getItem(position) as MealForListView
      val picassoBuilder = Picasso.Builder(context)

      // Picasso.Builder creates the Picasso object to do the actual requests
      val picasso = picassoBuilder.build()
      //picasso.load("https://abhilasha230587.appspot.com/images/Carousel-Image1.jpg").into(thumbnailImageView);
      picasso.load(meal.imageUrl).into(thumbnailImageView);
      //Picasso.get().load(meal.imageUrl).into(thumbnailImageView)
      println("||||||||||||||||||||||||||||||||||||||||||||||||||||| "+meal.imageUrl)
      //TODO: FIX PICASSO IMAGE LOADING

    titleTextView.text = meal.title
    subtitleTextView.text = meal.description
    detailTextView.text = meal.label


      detailTextView.setTextColor(
              ContextCompat.getColor(context, LABEL_COLORS[meal.label] ?: R.color.colorPrimary))




    return view
  }

  private class ViewHolder {
    lateinit var titleTextView: TextView
    lateinit var subtitleTextView: TextView
    lateinit var detailTextView: TextView
    lateinit var thumbnailImageView: ImageView
  }
}

package jp.ac.ncc.sugisakihuuta.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class ShelterAdapter(context: Context, private var shelters: List<Shelter>) :
    ArrayAdapter<Shelter>(context, 0, shelters) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val shelter = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_shelter, parent, false)

        val nameTextView = view.findViewById<TextView>(R.id.shelterNameTextView)
        val addressTextView = view.findViewById<TextView>(R.id.shelterAddressTextView)

        nameTextView.text = shelter?.name
        addressTextView.text = shelter?.address // 住所を追加

        return view
    }

    fun updateResults(newResults: List<Shelter>) {
        shelters = newResults
        notifyDataSetChanged()
    }
}

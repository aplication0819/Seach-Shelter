package jp.ac.ncc.sugisakihuuta.myapplication

import android.content.Context
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import jp.ac.ncc.sugisakihuuta.myapplication.data.SavedShelter

/*class SavedSheltersAdapter(
    private val context: Context,
    private val onShelterClick: (SavedShelter) -> Unit,
    private val onDeleteShelter: (SavedShelter) -> Unit
) : RecyclerView.Adapter<SavedSheltersAdapter.ViewHolder>() {

    private var shelters: List<SavedShelter> = emptyList()

    companion object {
        private const val VIEW_TYPE_SHELTER = 1
        private const val VIEW_TYPE_EMPTY = 0
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shelterNameTextView: TextView = itemView.findViewById(R.id.shelter_name_text_view)
        val shelterAddressTextView: TextView = itemView.findViewById(R.id.shelter_address_text_view)
        val menuButton: ImageView = itemView.findViewById(R.id.menu_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.saved_shelter_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val shelter = shelters[position]
        holder.shelterNameTextView.text = shelter.name

        // 住所の設定
        holder.shelterAddressTextView.text = shelter.address

        holder.itemView.setOnClickListener {
            onShelterClick(shelter) // コールバックを使用
        }

        holder.menuButton.setOnClickListener {
            showPopupMenu(it, shelter)
        }
    }

    private fun showPopupMenu(view: View, shelter: SavedShelter) {
        val popupMenu = PopupMenu(context, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.shelter_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onDeleteShelter(shelter)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return shelters.size
    }

    fun updateData(newList: List<SavedShelter>) {
        shelters = newList
        notifyDataSetChanged()
    }
}*/

class SavedSheltersAdapter(
    private val context: Context,
    private val onShelterClick: (SavedShelter) -> Unit,
    private val onDeleteShelter: (SavedShelter) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var shelters: List<SavedShelter> = emptyList()

    companion object {
        private const val VIEW_TYPE_SHELTER = 1
        private const val VIEW_TYPE_EMPTY = 0
    }

    inner class ShelterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shelterNameTextView: TextView = itemView.findViewById(R.id.shelter_name_text_view)
        val shelterAddressTextView: TextView = itemView.findViewById(R.id.shelter_address_text_view)
        val menuButton: ImageView = itemView.findViewById(R.id.menu_button)
    }

    inner class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyTextView: TextView = itemView.findViewById(R.id.empty_text_view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (shelters.isEmpty()) VIEW_TYPE_EMPTY else VIEW_TYPE_SHELTER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_EMPTY) {
            val view = LayoutInflater.from(context).inflate(R.layout.item_no_shelter, parent, false)
            EmptyViewHolder(view)
        } else {
            val view = LayoutInflater.from(context).inflate(R.layout.saved_shelter_list_item, parent, false)
            ShelterViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ShelterViewHolder) {
            val shelter = shelters[position]
            holder.shelterNameTextView.text = shelter.name

            // 住所の設定
            holder.shelterAddressTextView.text = shelter.address

            holder.itemView.setOnClickListener {
                onShelterClick(shelter) // コールバックを使用
            }

            holder.menuButton.setOnClickListener {
                showPopupMenu(it, shelter)
            }
        } else if (holder is EmptyViewHolder) {
            holder.emptyTextView.text = "保存している避難場所はありません"
        }
    }

    private fun showPopupMenu(view: View, shelter: SavedShelter) {
        val popupMenu = PopupMenu(context, view)
        val inflater: MenuInflater = popupMenu.menuInflater
        inflater.inflate(R.menu.shelter_item_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    onDeleteShelter(shelter)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    override fun getItemCount(): Int {
        return if (shelters.isEmpty()) 1 else shelters.size
    }

    fun updateData(newList: List<SavedShelter>) {
        shelters = newList
        notifyDataSetChanged()
    }
}

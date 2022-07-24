package com.example.retrofittest



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.retrofittest.databinding.ItemShopBinding


class ShopInfoAdapter : ListAdapter<Row, ShopInfoAdapter.ShopInfoViewHolder>(ShopInfoCallback){
    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener{
        fun onItemClick(position : Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopInfoViewHolder {
        val binding = ItemShopBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ShopInfoViewHolder(binding, mListener)
    }

    override fun onBindViewHolder(holder: ShopInfoViewHolder, position: Int) {
        holder.bind(getItem(position))

    }

    class ShopInfoViewHolder(private val binding : ItemShopBinding, listener: OnItemClickListener) : RecyclerView.ViewHolder(binding.root){

        init{
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }

        fun bind(item : Row){
            with(binding){
                tvName.text = item.sHNAME
                tvAddress.text = item.sHADDR
                tvPride.text = item.sHPRIDE
                val imgUrl : String? = item.sHPHOTO
                if(imgUrl != null){
                    Glide.with(this@ShopInfoViewHolder.itemView).load(imgUrl).into(ivImg)
                }
            }
        }
    }

    companion object{
        private val ShopInfoCallback = object : DiffUtil.ItemCallback<Row>(){
            override fun areItemsTheSame(oldItem: Row, newItem: Row): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }

            override fun areContentsTheSame(oldItem: Row, newItem: Row): Boolean {
                return oldItem == newItem
            }

        }
    }

}
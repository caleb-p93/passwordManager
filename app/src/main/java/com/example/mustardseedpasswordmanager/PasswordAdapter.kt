package com.example.mustardseedpasswordmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mustardseedpasswordmanager.data.PasswordEntry

class PasswordAdapter(
    private val passwordList: MutableList<PasswordEntry>,
    private val editCallback: (PasswordEntry, Int) -> Unit,
    private val deleteCallback: (Int) -> Unit
) : RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasswordViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.password_item, parent, false)
        return PasswordViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PasswordViewHolder, position: Int) {
        val currentPasswordEntry = passwordList[position]
        holder.websiteTextView.text = currentPasswordEntry.website
        holder.usernameTextView.text = currentPasswordEntry.username
        holder.passwordTextView.text = currentPasswordEntry.password

        holder.editButton.setOnClickListener {
            editCallback(currentPasswordEntry, position)
        }

        holder.deleteButton.setOnClickListener {
            deleteCallback(position)
        }
    }

    override fun getItemCount() = passwordList.size

    class PasswordViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val websiteTextView: TextView = itemView.findViewById(R.id.website_view)
        val usernameTextView: TextView = itemView.findViewById(R.id.username_view)
        val passwordTextView: TextView = itemView.findViewById(R.id.password_view)
        val editButton: Button = itemView.findViewById(R.id.edit_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_button)
    }
}

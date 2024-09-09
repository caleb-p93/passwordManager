package com.example.mustardseedpasswordmanager

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mustardseedpasswordmanager.data.PasswordEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PasswordAdapter
    private lateinit var websiteInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private var passwordList = mutableListOf<PasswordEntry>()
    private var filteredList = mutableListOf<PasswordEntry>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("PasswordManager", MODE_PRIVATE)

        recyclerView = findViewById(R.id.recycler_view)
        websiteInput = findViewById(R.id.website_input)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)

        adapter = PasswordAdapter(filteredList, this::editPassword, this::deletePassword)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        passwordList = loadPasswords()
        filteredList.addAll(passwordList)

        setupSearchView()
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return true
            }
        })
        searchView.setOnCloseListener {
            searchView.setQuery("", false)
            filter("")
            true
        }
    }

    private fun filter(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(passwordList)
        } else {
            val lowerCaseQuery = query.lowercase(Locale.getDefault())
            passwordList.forEach { entry ->
                if (entry.website.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                    || entry.username.lowercase(Locale.getDefault()).contains(lowerCaseQuery)
                ) {
                    filteredList.add(entry)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun savePassword(view: View) {
        val website = websiteInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (website.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            val newPasswordEntry = PasswordEntry(website, username, password) // Show actual password
            passwordList.add(newPasswordEntry)
            filteredList.add(newPasswordEntry) // Add to filtered list as well
            adapter.notifyDataSetChanged()

            savePasswords()

            websiteInput.text.clear()
            usernameInput.text.clear()
            passwordInput.text.clear()
        }
    }

    private fun editPassword(passwordEntry: PasswordEntry, position: Int) {
        websiteInput.setText(passwordEntry.website)
        usernameInput.setText(passwordEntry.username)
        passwordInput.setText(passwordEntry.password)

        // Remove the old entry
        passwordList.removeAt(position)
        filteredList.removeAt(position)
        adapter.notifyItemRemoved(position)

        // Notify the user
        Toast.makeText(this, "Edit the entry and press Save", Toast.LENGTH_SHORT).show()
    }

    private fun deletePassword(position: Int) {
        passwordList.removeAt(position)
        filteredList.removeAt(position)
        adapter.notifyItemRemoved(position)
        savePasswords()
    }

    private fun savePasswords() {
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(passwordList)
        editor.putString("password_list", json)
        editor.apply()
    }

    private fun loadPasswords(): MutableList<PasswordEntry> {
        val gson = Gson()
        val json = sharedPreferences.getString("password_list", null)
        val type = object : TypeToken<MutableList<PasswordEntry>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear -> {
                passwordList.clear()
                filteredList.clear()
                adapter.notifyDataSetChanged()
                clearPasswords()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun clearPasswords() {
        val editor = sharedPreferences.edit()
        editor.remove("password_list")
        editor.apply()
    }
}

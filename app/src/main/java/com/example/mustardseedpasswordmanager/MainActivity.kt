@file:Suppress("DEPRECATION")

package com.example.mustardseedpasswordmanager

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mustardseedpasswordmanager.data.PasswordEntry
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.opencsv.CSVReader
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PasswordAdapter
    private lateinit var websiteInput: EditText
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var saveButton: Button
    private lateinit var importCsvButton: Button

    private var passwordList = mutableListOf<PasswordEntry>()
    private var filteredList = mutableListOf<PasswordEntry>()

    private val CSV_FILE_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("PasswordManager", MODE_PRIVATE)

        recyclerView = findViewById(R.id.recycler_view)
        websiteInput = findViewById(R.id.website_input)
        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        saveButton = findViewById(R.id.save_button)
        importCsvButton = findViewById(R.id.import_csv_button)

        adapter = PasswordAdapter(filteredList, this::editPassword, this::deletePassword)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        passwordList = loadPasswords()
        filteredList.addAll(passwordList)

        setupSearchView()

        saveButton.setOnClickListener { savePassword() }
        importCsvButton.setOnClickListener { importCSV() }
    }

    private fun setupSearchView() {
        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

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

    /** Filters password entries by website or username, ignoring spaces and case */
    private fun filter(query: String?) {
        filteredList.clear()
        val trimmedQuery = query?.trim()?.lowercase(Locale.getDefault()) ?: ""
        if (trimmedQuery.isEmpty()) {
            filteredList.addAll(passwordList)
        } else {
            passwordList.forEach { entry ->
                if (entry.website.lowercase(Locale.getDefault()).contains(trimmedQuery)
                    || entry.username.lowercase(Locale.getDefault()).contains(trimmedQuery)
                ) {
                    filteredList.add(entry)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun savePassword() {
        val website = websiteInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        if (website.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
            val existingIndex = passwordList.indexOfFirst {
                it.website.equals(website, ignoreCase = true) &&
                        it.username.equals(username, ignoreCase = true)
            }

            if (existingIndex != -1) {
                passwordList[existingIndex] = PasswordEntry(website, username, password)
                Toast.makeText(this, "Entry updated", Toast.LENGTH_SHORT).show()
            } else {
                passwordList.add(PasswordEntry(website, username, password))
                Toast.makeText(this, "Entry saved", Toast.LENGTH_SHORT).show()
            }

            filteredList.clear()
            filteredList.addAll(passwordList)
            adapter.notifyDataSetChanged()
            savePasswords()

            websiteInput.text.clear()
            usernameInput.text.clear()
            passwordInput.text.clear()
        } else {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
        }
    }

    private fun editPassword(passwordEntry: PasswordEntry, position: Int) {
        websiteInput.setText(passwordEntry.website)
        usernameInput.setText(passwordEntry.username)
        passwordInput.setText(passwordEntry.password)

        Toast.makeText(this, "Edit the entry and press Save", Toast.LENGTH_SHORT).show()
    }

    /** Properly deletes the selected password entry */
    private fun deletePassword(position: Int) {
        if (position < 0 || position >= filteredList.size) return

        val entryToDelete = filteredList[position]
        filteredList.removeAt(position)
        adapter.notifyItemRemoved(position)

        // Remove from master list as well
        passwordList.removeAll {
            it.website.equals(entryToDelete.website, ignoreCase = true) &&
                    it.username.equals(entryToDelete.username, ignoreCase = true)
        }

        savePasswords()
        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
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
        return if (json != null) gson.fromJson(json, type) else mutableListOf()
    }

    private fun importCSV() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, CSV_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CSV_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream = contentResolver.openInputStream(uri)
                    val reader = CSVReader(InputStreamReader(inputStream))
                    reader.forEach { row ->
                        if (row.size >= 3) {
                            val website = row[0].trim()
                            val username = row[1].trim()
                            val password = row[2].trim()
                            val entry = PasswordEntry(website, username, password)
                            passwordList.add(entry)
                            filteredList.add(entry)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    savePasswords()
                    Toast.makeText(this, "CSV Import Successful", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error importing CSV: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

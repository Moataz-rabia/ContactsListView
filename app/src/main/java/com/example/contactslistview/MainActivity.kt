package com.example.contactslistview


import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val contacts = mutableListOf(
        "Ahmed Ben Ali",
        "Sarra Trabelsi",
        "Mohamed Gharbi",
        "Ines Jaziri",
        "Youssef Mansouri",
        "Amira Chaabane",
        "Karim Bouazizi",
        "Nour Hammami"
    )

    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var etContact: EditText
    private lateinit var btnModifier: Button
    private lateinit var btnAjouter: Button
    private lateinit var lvContacts: ListView

    private var positionSelectionnee = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Exercise 5: restore data after a screen rotation
        savedInstanceState?.getStringArrayList("contacts")?.let {
            contacts.clear()
            contacts.addAll(it)
        }
        positionSelectionnee = savedInstanceState?.getInt("position", -1) ?: -1

        etContact = findViewById(R.id.etContact)
        btnModifier = findViewById(R.id.btnModifier)
        btnAjouter = findViewById(R.id.btnAjouter)
        lvContacts = findViewById(R.id.lvContacts)

        adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_activated_1,
            contacts
        )
        lvContacts.adapter = adapter

        // Re-highlight the selected row after rotation
        if (positionSelectionnee in contacts.indices) {
            lvContacts.setItemChecked(positionSelectionnee, true)
        } else {
            positionSelectionnee = -1
        }

        // Click: select a contact
        lvContacts.setOnItemClickListener { _, _, position, _ ->
            positionSelectionnee = position
            lvContacts.setItemChecked(position, true)
            etContact.setText(contacts[position])
            etContact.setSelection(etContact.text.length)
            etContact.requestFocus()
        }

        // Exercise 2: long click -> delete with confirmation
        lvContacts.setOnItemLongClickListener { _, _, position, _ ->
            confirmerSuppression(position)
            true
        }

        btnModifier.setOnClickListener { modifierContact() }
        btnAjouter.setOnClickListener { ajouterContact() }

        etContact.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                modifierContact()
                true
            } else {
                false
            }
        }
    }

    // Exercise 5: save data before the activity is destroyed
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList("contacts", ArrayList(contacts))
        outState.putInt("position", positionSelectionnee)
    }

    private fun modifierContact() {
        if (positionSelectionnee == -1) {
            Toast.makeText(this, R.string.msg_aucune_selection, Toast.LENGTH_SHORT).show()
            return
        }

        val nouveauNom = etContact.text.toString().trim()
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }

        // Exercise 3: refuse duplicates (ignoring the contact being edited)
        val index = contacts.indexOfFirst { it.equals(nouveauNom, ignoreCase = true) }
        if (index != -1 && index != positionSelectionnee) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        contacts[positionSelectionnee] = nouveauNom

        // Exercise 4: sort, then reset the selection (positions changed)
        contacts.sortBy { it.lowercase() }
        reinitialiserSelection()
        adapter.notifyDataSetChanged()

        Toast.makeText(this, "Contact modifié : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    // Exercise 1: add a contact
    private fun ajouterContact() {
        val nouveauNom = etContact.text.toString().trim()
        if (nouveauNom.isEmpty()) {
            etContact.error = getString(R.string.msg_nom_vide)
            return
        }
        if (contacts.any { it.equals(nouveauNom, ignoreCase = true) }) {
            etContact.error = getString(R.string.msg_doublon)
            return
        }

        contacts.add(nouveauNom)
        contacts.sortBy { it.lowercase() }
        reinitialiserSelection()
        adapter.notifyDataSetChanged()

        // Scroll to the new contact
        lvContacts.smoothScrollToPosition(contacts.indexOf(nouveauNom))
        Toast.makeText(this, "Contact ajouté : $nouveauNom", Toast.LENGTH_SHORT).show()
    }

    // Exercise 2: delete after confirmation
    private fun confirmerSuppression(position: Int) {
        val nom = contacts[position]
        AlertDialog.Builder(this)
            .setTitle(R.string.dialog_titre)
            .setMessage("Supprimer « $nom » ?")
            .setPositiveButton(R.string.dialog_oui) { _, _ ->
                contacts.removeAt(position)
                reinitialiserSelection()
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Contact supprimé", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.dialog_non, null)
            .show()
    }

    private fun reinitialiserSelection() {
        positionSelectionnee = -1
        lvContacts.clearChoices()
        etContact.text.clear()
    }
}
package com.example.sezin.ui.sos

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sezin.serversezin.EmergencyContact
import com.example.sezin.databinding.FragmentSosBinding
import com.example.sezin.serversezin.EmergencyContactDatabaseHelper

class SOSFragment : Fragment() {

    private var _binding: FragmentSosBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: EmergencyContactDatabaseHelper
    private lateinit var viewModel: SOSViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSosBinding.inflate(inflater, container, false)
        dbHelper = EmergencyContactDatabaseHelper(requireContext())
        viewModel = ViewModelProvider(this).get(SOSViewModel::class.java)

        setupButtonListeners()
        requestPermissions()

        return binding.root
    }


    private fun setupButtonListeners() {
        binding.btnLow.setOnClickListener { handleLowRiskAlert() }
        binding.btnHighRisk.setOnClickListener { handleHighRiskAlert() }
        binding.btnSevere.setOnClickListener { handleSevereAlert() }
    }

    private fun handleLowRiskAlert() {
        val contact = getEmergencyContact()
        if (contact != null) {
            sendSOSMessage(contact.phone, "Low")
        } else {
            askForContact()
        }
    }

    private fun handleHighRiskAlert() {
        val contact = getEmergencyContact()
        if (contact != null) {
            makeEmergencyCall("+77071050415")
        } else {
            askForContact()
        }
    }

    private fun handleSevereAlert() {
        val contact = getEmergencyContact()
        if (contact != null) {
            sendSOSMessage(contact.phone, "Severe")
            notifyLawEnforcement()
        } else {
            askForContact()
        }
    }

    private fun getEmergencyContact(): EmergencyContact? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            EmergencyContactDatabaseHelper.TABLE_NAME,
            null, null, null, null, null, null
        )
        return if (cursor.moveToFirst()) {
            EmergencyContact(
                cursor.getString(cursor.getColumnIndexOrThrow(EmergencyContactDatabaseHelper.COLUMN_NAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(EmergencyContactDatabaseHelper.COLUMN_PHONE)),
                cursor.getString(cursor.getColumnIndexOrThrow(EmergencyContactDatabaseHelper.COLUMN_TYPE))
            )
        } else {
            null
        }.also {
            cursor.close() // Ensure you close the cursor after use
        }
    }

    private fun askForContact() {
        Toast.makeText(requireContext(), "Please specify an emergency contact in settings.", Toast.LENGTH_SHORT).show()
    }

    private fun sendSOSMessage(phoneNumber: String, dangerLevel: String) {
        val smsManager = SmsManager.getDefault()
        val message = "SOS Alert! Danger level: $dangerLevel. Please help!"
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        Toast.makeText(requireContext(), "SOS message sent to $phoneNumber", Toast.LENGTH_SHORT).show()
    }

    private fun makeEmergencyCall(phoneNumber: String) {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = android.net.Uri.parse("tel:$phoneNumber")
            startActivity(callIntent)
        } else {
            Toast.makeText(requireContext(), "Call permission not granted.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun notifyLawEnforcement() {
        // Replace with an appropriate number or API integration
        sendSOSMessage("+77071050415", "Severe") // Example
    }

    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS, Manifest.permission.CALL_PHONE),
                1
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

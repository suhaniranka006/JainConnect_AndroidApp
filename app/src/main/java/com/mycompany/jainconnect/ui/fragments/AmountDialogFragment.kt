package com.mycompany.jainconnect.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.mycompany.jainconnect.R

class AmountDialogFragment : DialogFragment() {

    // Interface to communicate the result back to HomeFragment
    interface AmountDialogListener {
        fun onAmountEntered(amountInPaise: Int)
    }

    private var listener: AmountDialogListener? = null
    private lateinit var etAmount: TextInputEditText
    private lateinit var btnCustomDonate: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Use the custom layout
        return inflater.inflate(R.layout.dialog_amount_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set the listener to the parent Fragment (HomeFragment)
        listener = parentFragment as? AmountDialogListener

        etAmount = view.findViewById(R.id.etAmount)
        btnCustomDonate = view.findViewById(R.id.btnCustomDonate)

        btnCustomDonate.setOnClickListener {
            handleDonationClick()
        }
    }

    override fun onResume() {
        super.onResume()
        // Make the dialog full width
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun handleDonationClick() {
        val amountText = etAmount.text.toString().trim()

        if (amountText.isEmpty()) {
            etAmount.error = "Please enter an amount"
            return
        }

        val amountInRupees = amountText.toFloatOrNull()

        if (amountInRupees == null || amountInRupees < 1.0f) { // Minimum amount check
            etAmount.error = "Please enter a valid amount (Min. ₹1)"
            return
        }

        // Convert Rupees to Paise (multiply by 100)
        val amountInPaise = (amountInRupees * 100).toInt()

        // Pass the final amount back to the HomeFragment
        listener?.onAmountEntered(amountInPaise)

        // Close the dialog
        dismiss()
    }

    // Optional: If you need to attach the listener from the Activity (less common for Fragments)
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = parentFragment as? AmountDialogListener
        } catch (e: ClassCastException) {
            // Handle error if the parent Fragment doesn't implement the interface
        }
    }
}
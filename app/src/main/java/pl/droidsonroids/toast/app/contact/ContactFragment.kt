package pl.droidsonroids.toast.app.contact

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import br.com.ilhasoft.support.validation.Validator
import kotlinx.android.synthetic.main.fragment_contact.*
import pl.droidsonroids.toast.R
import pl.droidsonroids.toast.app.base.BaseFragment
import pl.droidsonroids.toast.app.home.ContactInputFormTextWatcher
import pl.droidsonroids.toast.databinding.FragmentContactBinding
import pl.droidsonroids.toast.viewmodels.contact.ContactViewModel


class ContactFragment : BaseFragment() {

    private lateinit var contactViewModel: ContactViewModel
    private lateinit var validator: Validator

    override fun onAttach(context: Context) {
        super.onAttach(context)
        contactViewModel = ViewModelProviders.of(this, viewModelFactory)[ContactViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val binding = FragmentContactBinding.inflate(inflater, container, false)
        binding.contactViewModel = contactViewModel
        setupValidator(binding)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTopicSpinner()
        setupInputsTextWatcher()
    }

    private fun setupValidator(binding: FragmentContactBinding) {
        validator = Validator(binding)
        validator.enableFieldValidationMode()
        validator.setValidationListener(object : Validator.ValidationListener {

            override fun onValidationError() {
                contactViewModel.validateForm(false)
            }

            override fun onValidationSuccess() {
                contactViewModel.validateForm(true)
            }

        })
    }

    private fun setupTopicSpinner() {
        val adapter = object : ArrayAdapter<String>(context, R.layout.item_contact_spinner, resources.getStringArray(R.array.contact_topics)) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val dropDownView = super.getDropDownView(position, convertView, parent)
                val isHintItem = position == 0
                dropDownView.isClickable = isHintItem
                dropDownView.isEnabled = !isHintItem
                return dropDownView
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        topicSpinner.adapter = adapter
        topicSpinner.setOnLongClickListener { false }
    }

    private fun setupInputsTextWatcher() {
        with(contactEmailEditText) { addTextChangedListener(ContactInputFormTextWatcher(this, validator)) }
        with(contactNameEditText) { addTextChangedListener(ContactInputFormTextWatcher(this, validator)) }
        with(contactMessageEditText) { addTextChangedListener(ContactInputFormTextWatcher(this, validator)) }
    }

}

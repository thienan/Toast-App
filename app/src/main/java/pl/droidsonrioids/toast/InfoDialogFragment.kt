package pl.droidsonrioids.toast

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_info_dialog.*
import pl.droidsonrioids.toast.app.home.MainActivity

class InfoDialogFragment : DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_info_dialog, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialSetUp()
    }

    private fun initialSetUp() {
        setDialogBackgroundDrawable()
        setButtonsOnClickListeners()
        setVersionText()
    }

    private fun setDialogBackgroundDrawable() {
        dialog.window.setBackgroundDrawableResource(R.drawable.dialog_background_round_rectangle)
    }

    private fun setButtonsOnClickListeners() {
        setCloseImageButtonOnClickListener()
        setFanpageLinkTextOnClickListener()
    }

    private fun setCloseImageButtonOnClickListener() {
        closeImageButton.setOnClickListener { dismiss() }
    }

    private fun setFanpageLinkTextOnClickListener() {
        fanpageLinkText.setOnClickListener {
            openFanpageSite()
            dismiss()
        }
    }

    private fun openFanpageSite() {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.toast_fanpage_url))
            startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            showBrowserNotFoundErrorToast()
        }
    }

    private fun showBrowserNotFoundErrorToast() {
        Snackbar.make((activity as MainActivity).mainCoordinatorLayout, R.string.error_internet_browser_not_found, Snackbar.LENGTH_SHORT).show()
    }

    private fun setVersionText() {
        appVersionText.text = BuildConfig.VERSION_NAME
    }
}

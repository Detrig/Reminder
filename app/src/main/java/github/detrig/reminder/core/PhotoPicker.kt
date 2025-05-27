package github.detrig.corporatekanbanboard.core

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore

object PhotoPicker {
    private const val PICK_IMAGE_REQUEST = 1001

    fun openGallery(activity: Activity) {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    fun handleActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onImageSelected: (Uri) -> Unit
    ) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                onImageSelected(uri)
            }
        }
    }
}
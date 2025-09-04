package github.detrig.reminder.presentation.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import github.detrig.reminder.core.AbstractFragment
import github.detrig.reminder.databinding.FragmentRecordAudioBinding

class RecordAudioFragment : AbstractFragment<FragmentRecordAudioBinding>() {

    override fun bind(inflater: LayoutInflater, container: ViewGroup?): FragmentRecordAudioBinding =
        FragmentRecordAudioBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}
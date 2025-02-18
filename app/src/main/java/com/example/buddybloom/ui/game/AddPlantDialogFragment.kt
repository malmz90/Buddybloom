import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.buddybloom.databinding.FragmentDialogAddPlantBinding
import com.example.buddybloom.ui.game.ChoosePlantFragment
import com.example.buddybloom.ui.game.GameActivity

class AddPlantDialogFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentDialogAddPlantBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDialogAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Click events for buttons in the dialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Replace the fragment with ChoosePlantFragment
        binding.btnYes.setOnClickListener {
            (activity as? GameActivity)?.replaceFragment(ChoosePlantFragment())
            dismiss()
        }

        // If "No" is pressed, close the dialog
        binding.btnNo.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

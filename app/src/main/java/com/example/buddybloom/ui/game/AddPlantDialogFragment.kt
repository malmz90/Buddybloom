import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.example.buddybloom.databinding.DialogAddPlantBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.buddybloom.ui.game.PlantViewModel
import com.example.buddybloom.ui.game.ReplacePlantDialogFragment

class AddPlantDialogFragment(private val newPlant: Plant) :
    BottomSheetDialogFragment(R.layout.dialog_add_plant) {

    private lateinit var pvm: PlantViewModel
    private var _binding: DialogAddPlantBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        pvm = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        _binding = DialogAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Click events for buttons in the dialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sets plant info text in popup dialog
        val tvPlantInfo = view.findViewById<TextView>(R.id.tv_plant_info)
        tvPlantInfo.text = newPlant.info

        binding.btnAddPlant.setOnClickListener {
            pvm.localSessionPlant.observe(viewLifecycleOwner) { currentPlant ->
                //I user has no plant (null), skip confirmation dialog.
                if (currentPlant == null) {
                    pvm.savePlantToRemote(newPlant)
                    pvm.resetPlantDeath()
                    dismiss()
                } else {
                    ReplacePlantDialogFragment(newPlant).show(parentFragmentManager, null)
                    dismiss()
                }
            }
        }

        // If "No" is pressed, close the dialog
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
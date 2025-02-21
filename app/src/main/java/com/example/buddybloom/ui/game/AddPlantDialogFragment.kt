import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.buddybloom.databinding.FragmentDialogAddPlantBinding
import com.example.buddybloom.ui.game.PlantViewModel
import com.example.buddybloom.ui.game.ReplacePlantDialogFragment
import kotlin.math.log

class AddPlantDialogFragment(private val newPlant: Plant) :
    BottomSheetDialogFragment(R.layout.fragment_dialog_add_plant) {

    private lateinit var plantViewModel: PlantViewModel
    private var _binding: FragmentDialogAddPlantBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        plantViewModel = ViewModelProvider(requireActivity())[PlantViewModel::class.java]
        _binding = FragmentDialogAddPlantBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Click events for buttons in the dialog
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sets plant info text in popup dialog
        val tvPlantInfo = view.findViewById<TextView>(R.id.tv_plant_info)
        tvPlantInfo.text = newPlant.info

        binding.btnAddPlant.setOnClickListener {
            plantViewModel.getCurrentUserPlant { fetchedPlant ->
                //I user has no plant (null), skip confirmation dialog.
                if (fetchedPlant == null) {
                    plantViewModel.savePlantForCurrentUser(newPlant) {
                        dismiss()
                    }
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
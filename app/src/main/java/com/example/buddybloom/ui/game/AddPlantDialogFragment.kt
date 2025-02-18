import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.buddybloom.R
import com.example.buddybloom.data.model.Plant
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.example.buddybloom.databinding.FragmentDialogAddPlantBinding
import com.example.buddybloom.ui.game.ChoosePlantFragment
import com.example.buddybloom.ui.game.GameActivity
import com.example.buddybloom.ui.game.PlantViewModel
import com.example.buddybloom.ui.game.ReplacePlantDialogFragment
import com.example.buddybloom.ui.game.StartPagePlantFragment

class AddPlantDialogFragment(private val plant: Plant) : BottomSheetDialogFragment(R.layout.fragment_dialog_add_plant) {

    private lateinit var plantViewModel : PlantViewModel
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

         val tvPlantInfo = view.findViewById<TextView>(R.id.tv_plant_info)

        tvPlantInfo.text = plant.info

        // Replace the fragment with ChoosePlantFragment
        binding.btnAddPlant.setOnClickListener {
            plantViewModel.setSelectedPlant(plant)

            if(plantViewModel.isFirstTimeChoosingPlant) {
                plantViewModel.savePlantForCurrentUser()
                plantViewModel.isFirstTimeChoosingPlant = false
                Log.d("AddPlant", "First plant chosen")

                Toast.makeText(requireContext(), "${plant.name} Selected!", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fvc_game_activity, StartPagePlantFragment())
                    .commit()
            } else {
                val replaceDialog = ReplacePlantDialogFragment(plant)
                replaceDialog.show(parentFragmentManager, "ReplacePlantDialog")
            }
            dismiss()
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
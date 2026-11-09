package com.kasif.arrowbuddies.feature.levellab

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kasif.arrowbuddies.data.repository.LevelRepository
import com.kasif.arrowbuddies.engine.codec.LevelCodec
import com.kasif.arrowbuddies.engine.generator.GenParams
import com.kasif.arrowbuddies.engine.generator.LevelGenerator
import com.kasif.arrowbuddies.engine.model.LevelDef
import com.kasif.arrowbuddies.engine.model.WorldSpecies
import com.kasif.arrowbuddies.engine.solver.LevelSolver
import com.kasif.arrowbuddies.engine.solver.SolveResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LevelLabUiState(
    val seed: Long = 12345L,
    val cols: Int = 5,
    val rows: Int = 6,
    val buddyCount: Int = 7,
    val allowedMechanics: Set<String> = setOf("rock", "mud"),
    val obstacleBudget: Int = 2,
    val targetScoreMin: Double = 20.0,
    val targetScoreMax: Double = 40.0,
    val world: Int = 2,
    val isGenerating: Boolean = false,
    val generatedLevel: LevelDef? = null,
    val solveResult: SolveResult? = null,
    val jsonString: String = "",
    val errorMessage: String? = null,
    val copySuccess: Boolean = false
)

sealed interface LevelLabEvent {
    data class UpdateSeed(val seed: Long) : LevelLabEvent
    data class UpdateCols(val cols: Int) : LevelLabEvent
    data class UpdateRows(val rows: Int) : LevelLabEvent
    data class UpdateBuddyCount(val count: Int) : LevelLabEvent
    data class ToggleMechanic(val mechanic: String) : LevelLabEvent
    data class UpdateObstacleBudget(val budget: Int) : LevelLabEvent
    data class UpdateScoreRange(val min: Double, val max: Double) : LevelLabEvent
    data class UpdateWorld(val world: Int) : LevelLabEvent
    data object Generate : LevelLabEvent
    data object CopyJson : LevelLabEvent
    data object ResetCopyStatus : LevelLabEvent
}

@HiltViewModel
class LevelLabViewModel @Inject constructor(
    private val levelRepository: LevelRepository,
    @ApplicationContext private val context: Context? = null
) : ViewModel() {

    internal var defaultDispatcher: CoroutineDispatcher = Dispatchers.Default

    constructor(
        levelRepository: LevelRepository,
        context: Context?,
        dispatcher: CoroutineDispatcher
    ) : this(levelRepository, context) {
        this.defaultDispatcher = dispatcher
    }

    private val _uiState = MutableStateFlow(LevelLabUiState())
    val uiState: StateFlow<LevelLabUiState> = _uiState.asStateFlow()

    fun onEvent(event: LevelLabEvent) {
        when (event) {
            is LevelLabEvent.UpdateSeed -> _uiState.update { it.copy(seed = event.seed) }
            is LevelLabEvent.UpdateCols -> _uiState.update { it.copy(cols = event.cols.coerceIn(4, 7)) }
            is LevelLabEvent.UpdateRows -> _uiState.update { it.copy(rows = event.rows.coerceIn(4, 9)) }
            is LevelLabEvent.UpdateBuddyCount -> _uiState.update { it.copy(buddyCount = event.count.coerceIn(2, 20)) }
            is LevelLabEvent.ToggleMechanic -> _uiState.update { state ->
                val newMechanics = if (event.mechanic in state.allowedMechanics) {
                    state.allowedMechanics - event.mechanic
                } else {
                    state.allowedMechanics + event.mechanic
                }
                state.copy(allowedMechanics = newMechanics)
            }
            is LevelLabEvent.UpdateObstacleBudget -> _uiState.update { it.copy(obstacleBudget = event.budget.coerceIn(0, 6)) }
            is LevelLabEvent.UpdateScoreRange -> _uiState.update { it.copy(targetScoreMin = event.min, targetScoreMax = event.max) }
            is LevelLabEvent.UpdateWorld -> _uiState.update { it.copy(world = event.world.coerceIn(1, 8)) }
            is LevelLabEvent.Generate -> generate()
            is LevelLabEvent.CopyJson -> copyJsonToClipboard()
            is LevelLabEvent.ResetCopyStatus -> _uiState.update { it.copy(copySuccess = false) }
        }
    }

    private fun generate() {
        val state = _uiState.value
        _uiState.update { it.copy(isGenerating = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                val params = GenParams(
                    seed = state.seed,
                    cols = state.cols,
                    rows = state.rows,
                    buddyCount = state.buddyCount,
                    allowedMechanics = state.allowedMechanics,
                    obstacleBudget = state.obstacleBudget,
                    targetScoreRange = state.targetScoreMin..state.targetScoreMax,
                    speciesPool = WorldSpecies.forWorld(state.world),
                    world = state.world,
                    index = 1
                )

                val (level, result, json) = withContext(defaultDispatcher) {
                    val genLevel = LevelGenerator.generate(params)
                    val solveRes = LevelSolver.solve(genLevel)
                    val jsonStr = LevelCodec.encode(genLevel)
                    Triple(genLevel, solveRes, jsonStr)
                }

                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generatedLevel = level,
                        solveResult = result,
                        jsonString = json,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        errorMessage = e.message ?: "Failed to generate level"
                    )
                }
            }
        }
    }

    private fun copyJsonToClipboard() {
        val json = _uiState.value.jsonString
        if (json.isNotBlank()) {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("ArrowBuddies LevelDef", json)
            clipboard?.setPrimaryClip(clip)
            _uiState.update { it.copy(copySuccess = true) }
        }
    }

    fun prepareForPlay(): String? {
        val level = _uiState.value.generatedLevel ?: return null
        return levelRepository.registerCustomLevel(level)
    }
}

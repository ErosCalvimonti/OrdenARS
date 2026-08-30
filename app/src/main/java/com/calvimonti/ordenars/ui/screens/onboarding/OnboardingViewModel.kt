package com.calvimonti.ordenars.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calvimonti.ordenars.data.repository.PreferenciasRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferenciasRepository: PreferenciasRepository
) : ViewModel() {

    fun completarOnboarding(alTerminar: () -> Unit) {
        viewModelScope.launch {
            preferenciasRepository.setOnboardingCompletado()
            alTerminar()
        }
    }
}

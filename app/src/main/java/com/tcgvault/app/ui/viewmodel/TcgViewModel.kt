package com.tcgvault.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tcgvault.app.BuildConfig
import com.tcgvault.app.data.local.model.CardWithCollection
import com.tcgvault.app.data.local.model.CollectionStats
import com.tcgvault.app.data.local.model.PreconCardDetail
import com.tcgvault.app.data.local.model.PreconWithSummary
import com.tcgvault.app.data.local.model.SetWithProgress
import com.tcgvault.app.data.model.ReleaseDateFilter
import com.tcgvault.app.data.model.TcgGame
import com.tcgvault.app.data.remote.AppUpdateInfo
import com.tcgvault.app.data.remote.GitHubUpdateChecker
import com.tcgvault.app.data.remote.InAppUpdateInstaller
import com.tcgvault.app.data.remote.UpdateDownloadState
import com.tcgvault.app.data.repository.TcgRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CardOwnershipFilter(val label: String) {
    ALL("All"),
    COLLECTED("Collected"),
    MISSING("Missing")
}

enum class CardSortOrder(val label: String, val shortLabel: String) {
    NUMBER("Collector #", "#"),
    NAME_ASC("Name (A–Z)", "A–Z"),
    NAME_DESC("Name (Z–A)", "Z–A"),
    RARITY_DESC("Rarity (High → Low)", "Rarity"),
    OWNED_DESC("Owned (Most First)", "Owned")
}

@OptIn(ExperimentalCoroutinesApi::class)
class TcgViewModel(
    private val repository: TcgRepository,
    initialGame: TcgGame = TcgGame.YUGIOH,
    private val onGamePersisted: (TcgGame) -> Unit = {},
    private val updateChecker: GitHubUpdateChecker = GitHubUpdateChecker()
) : ViewModel() {

    private val _selectedGame = MutableStateFlow(initialGame)
    val selectedGame: StateFlow<TcgGame> = _selectedGame.asStateFlow()

    private val _setsQuery = MutableStateFlow("")
    val setsQuery: StateFlow<String> = _setsQuery.asStateFlow()

    private val _preconsQuery = MutableStateFlow("")
    val preconsQuery: StateFlow<String> = _preconsQuery.asStateFlow()

    private val _collectionQuery = MutableStateFlow("")
    val collectionQuery: StateFlow<String> = _collectionQuery.asStateFlow()

    private val _isSyncingCatalog = MutableStateFlow(false)
    val isSyncingCatalog: StateFlow<Boolean> = _isSyncingCatalog.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    // Active set detail state
    private val _activeSetId = MutableStateFlow<String?>(null)
    private val _setCardQuery = MutableStateFlow("")
    val setCardQuery: StateFlow<String> = _setCardQuery.asStateFlow()

    private val _setCardFilter = MutableStateFlow(CardOwnershipFilter.ALL)
    val setCardFilter: StateFlow<CardOwnershipFilter> = _setCardFilter.asStateFlow()

    private val _setCardSort = MutableStateFlow(CardSortOrder.NUMBER)
    val setCardSort: StateFlow<CardSortOrder> = _setCardSort.asStateFlow()

    private val _isLoadingSetCards = MutableStateFlow(false)
    val isLoadingSetCards: StateFlow<Boolean> = _isLoadingSetCards.asStateFlow()

    private val _setLoadError = MutableStateFlow<String?>(null)
    val setLoadError: StateFlow<String?> = _setLoadError.asStateFlow()

    // Active precon detail state
    private val _activePreconId = MutableStateFlow<String?>(null)
    private val _isAddingPreconId = MutableStateFlow<String?>(null)
    val isAddingPreconId: StateFlow<String?> = _isAddingPreconId.asStateFlow()

    private val _isLoadingPreconCards = MutableStateFlow(false)
    val isLoadingPreconCards: StateFlow<Boolean> = _isLoadingPreconCards.asStateFlow()

    private val _availableUpdate = MutableStateFlow<AppUpdateInfo?>(null)
    val availableUpdate: StateFlow<AppUpdateInfo?> = _availableUpdate.asStateFlow()

    private val _updateDownloadState = MutableStateFlow<UpdateDownloadState>(UpdateDownloadState.Idle)
    val updateDownloadState: StateFlow<UpdateDownloadState> = _updateDownloadState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            syncCatalogInBackground()
        }
        viewModelScope.launch {
            val update = updateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
            if (update != null) {
                _availableUpdate.value = update
            }
        }
    }

    fun dismissUpdateDialog() {
        if (_updateDownloadState.value is UpdateDownloadState.Downloading) return
        _availableUpdate.value = null
        _updateDownloadState.value = UpdateDownloadState.Idle
    }

    fun startInAppUpdate(context: Context) {
        val update = _availableUpdate.value ?: return
        if (_updateDownloadState.value is UpdateDownloadState.Downloading) return

        val appContext = context.applicationContext
        viewModelScope.launch {
            _updateDownloadState.value = UpdateDownloadState.Downloading(0f)
            val result = InAppUpdateInstaller.downloadApk(
                context = appContext,
                url = update.downloadUrl,
                version = update.latestVersion,
                onProgress = { progress ->
                    _updateDownloadState.value = UpdateDownloadState.Downloading(progress)
                }
            )
            result.onSuccess { apkFile ->
                val canInstall = InAppUpdateInstaller.canInstallPackages(appContext)
                _updateDownloadState.value = UpdateDownloadState.ReadyToInstall(
                    apkFile = apkFile,
                    needsPermission = !canInstall
                )
                if (canInstall) {
                    InAppUpdateInstaller.launchSystemInstaller(appContext, apkFile)
                } else {
                    InAppUpdateInstaller.openInstallPermissionSettings(appContext)
                }
            }.onFailure { err ->
                _updateDownloadState.value = UpdateDownloadState.Error(
                    err.localizedMessage ?: "Failed to download update"
                )
            }
        }
    }

    fun triggerDownloadedUpdateInstall(context: Context) {
        val state = _updateDownloadState.value as? UpdateDownloadState.ReadyToInstall ?: return
        val appContext = context.applicationContext
        if (InAppUpdateInstaller.canInstallPackages(appContext)) {
            _updateDownloadState.value = state.copy(needsPermission = false)
            InAppUpdateInstaller.launchSystemInstaller(appContext, state.apkFile)
        } else {
            InAppUpdateInstaller.openInstallPermissionSettings(appContext)
        }
    }

    private suspend fun syncCatalogInBackground() {
        if (_isSyncingCatalog.value) return
        _isSyncingCatalog.value = true
        repository.syncAllCatalogSets()
        _isSyncingCatalog.value = false
    }

    val sets: StateFlow<List<SetWithProgress>> = combine(_selectedGame, _setsQuery) { game, query ->
        game to query
    }.flatMapLatest { (game, query) ->
        repository.observeSets(game, query).map { list ->
            val today = ReleaseDateFilter.todayIso()
            list.filter { ReleaseDateFilter.isReleased(it.set.releaseDate, today) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val precons: StateFlow<List<PreconWithSummary>> = combine(_selectedGame, _setsQuery) { game, query ->
        game to query
    }.flatMapLatest { (game, query) ->
        repository.observePrecons(game, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ownedCards: StateFlow<List<CardWithCollection>> = combine(_selectedGame, _collectionQuery) { game, query ->
        game to query
    }.flatMapLatest { (game, query) ->
        repository.observeOwnedCards(game, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val stats: StateFlow<CollectionStats> = repository.observeCollectionStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CollectionStats())

    val activeSet: StateFlow<SetWithProgress?> = _activeSetId
        .flatMapLatest { setId ->
            if (setId == null) flowOf(null) else repository.observeSet(setId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val activeSetCards: StateFlow<List<CardWithCollection>> = combine(
        _activeSetId,
        _setCardQuery,
        _setCardFilter,
        _setCardSort
    ) { setId, query, ownershipFilter, sortOrder ->
        Quadruple(setId, query, ownershipFilter, sortOrder)
    }.flatMapLatest { (setId, query, ownershipFilter, sortOrder) ->
        if (setId == null) {
            flowOf(emptyList())
        } else {
            repository.observeCardsInSet(setId, query).map { cards ->
                val filtered = when (ownershipFilter) {
                    CardOwnershipFilter.ALL -> cards
                    CardOwnershipFilter.COLLECTED -> cards.filter { it.isCollected }
                    CardOwnershipFilter.MISSING -> cards.filter { !it.isCollected }
                }
                sortSetCards(filtered, sortOrder)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activePreconCards: StateFlow<List<PreconCardDetail>> = _activePreconId
        .flatMapLatest { preconId ->
            if (preconId == null) flowOf(emptyList()) else repository.observePreconCards(preconId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun selectGame(game: TcgGame) {
        _selectedGame.value = game
        onGamePersisted(game)
    }

    fun updateSetsQuery(query: String) {
        _setsQuery.value = query
    }

    fun updatePreconsQuery(query: String) {
        _preconsQuery.value = query
    }

    fun updateCollectionQuery(query: String) {
        _collectionQuery.value = query
    }

    fun updateSetCardQuery(query: String) {
        _setCardQuery.value = query
    }

    fun updateSetCardFilter(filter: CardOwnershipFilter) {
        _setCardFilter.value = filter
    }

    fun updateSetCardSort(sortOrder: CardSortOrder) {
        _setCardSort.value = sortOrder
    }

    fun dismissBanner() {
        _statusBanner.value = null
    }

    fun syncAllCatalogSets() {
        if (_isSyncingCatalog.value) return
        viewModelScope.launch {
            _isSyncingCatalog.value = true
            _statusBanner.value = null
            val result = repository.syncAllCatalogSets()
            result.onSuccess { count ->
                _statusBanner.value = "Synced $count official MTG & Yu-Gi-Oh! sets locally."
            }.onFailure { err ->
                _statusBanner.value = "Offline or sync failed: ${err.localizedMessage ?: "Check connection"}"
            }
            _isSyncingCatalog.value = false
        }
    }

    fun openSet(setId: String) {
        _activeSetId.value = setId
        _setCardQuery.value = ""
        _setCardFilter.value = CardOwnershipFilter.ALL
        _setLoadError.value = null
        viewModelScope.launch {
            _isLoadingSetCards.value = true
            val result = repository.ensureSetCardsLoaded(setId)
            result.onFailure { err ->
                _setLoadError.value = "Could not load cards from public catalog: ${err.localizedMessage ?: "Offline"}"
            }
            _isLoadingSetCards.value = false
        }
    }

    fun onCardQuickAdd(cardId: String) {
        viewModelScope.launch {
            repository.toggleOrAddCard(cardId)
        }
    }

    fun adjustCardQuantity(cardId: String, delta: Int) {
        viewModelScope.launch {
            repository.adjustCardQuantity(cardId, delta)
        }
    }

    fun setCardQuantity(cardId: String, quantity: Int) {
        viewModelScope.launch {
            repository.setCardQuantity(cardId, quantity)
        }
    }

    fun toggleSetFavorite(setId: String) {
        viewModelScope.launch {
            repository.toggleSetFavorite(setId)
        }
    }

    fun togglePreconFavorite(preconId: String) {
        viewModelScope.launch {
            repository.togglePreconFavorite(preconId)
        }
    }

    fun toggleCardFavorite(cardId: String) {
        viewModelScope.launch {
            repository.toggleCardFavorite(cardId)
        }
    }

    fun selectPreconForDetail(preconId: String?) {
        _activePreconId.value = preconId
        if (preconId != null) {
            viewModelScope.launch {
                _isLoadingPreconCards.value = true
                repository.ensurePreconCardsLoaded(preconId)
                _isLoadingPreconCards.value = false
            }
        }
    }

    fun addPreconToCollection(preconId: String) {
        if (_isAddingPreconId.value != null) return
        viewModelScope.launch {
            _isAddingPreconId.value = preconId
            val result = repository.addPreconToCollection(preconId)
            result.onSuccess {
                _statusBanner.value = "Added precon and its cards to your local collection!"
            }.onFailure { err ->
                _statusBanner.value = "Could not add precon: ${err.localizedMessage ?: "Error"}"
            }
            _isAddingPreconId.value = null
        }
    }

    fun removePreconFromCollection(preconId: String) {
        viewModelScope.launch {
            repository.removePreconFromCollection(preconId)
            _statusBanner.value = "Removed 1 copy of precon from your collection."
        }
    }

    fun createCustomPrecon(
        name: String,
        game: TcgGame,
        category: String,
        releaseYear: Int,
        cardLines: String,
        addImmediately: Boolean
    ) {
        viewModelScope.launch {
            repository.createAndAddCustomPrecon(
                name = name,
                game = game,
                category = category,
                releaseYear = releaseYear,
                cardLines = cardLines,
                addImmediatelyToCollection = addImmediately
            )
            _statusBanner.value = if (addImmediately) {
                "Created & added '$name' to your collection!"
            } else {
                "Saved custom precon '$name' locally."
            }
        }
    }

    private data class Quadruple<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

    private fun sortSetCards(
        cards: List<CardWithCollection>,
        sortOrder: CardSortOrder
    ): List<CardWithCollection> {
        fun naturalNum(raw: String): Int =
            Regex("\\d+").find(raw)?.value?.toIntOrNull() ?: Int.MAX_VALUE

        fun rarityRank(rarity: String): Int {
            val r = rarity.trim().lowercase()
            return when {
                r.contains("quarter century") || r.contains("starlight") || r.contains("ghost") -> 7
                r.contains("mythic") || r.contains("secret") || r.contains("collector") || r.contains("ultimate") -> 6
                r.contains("special") || r.contains("bonus") || r.contains("timeshifted") -> 5
                r.contains("ultra") -> 5
                r.contains("super") -> 4
                r.contains("rare") && !r.contains("uncommon") -> 3
                r.contains("uncommon") -> 2
                else -> 1
            }
        }

        val comparator: Comparator<CardWithCollection> = when (sortOrder) {
            CardSortOrder.NUMBER -> compareByDescending<CardWithCollection> { it.card.isFavorite }
                .thenBy { naturalNum(it.card.collectorNumber) }
                .thenBy { it.card.collectorNumber }
                .thenBy { it.card.name }

            CardSortOrder.NAME_ASC -> compareByDescending<CardWithCollection> { it.card.isFavorite }
                .thenBy { it.card.name.lowercase() }

            CardSortOrder.NAME_DESC -> compareByDescending<CardWithCollection> { it.card.isFavorite }
                .thenByDescending { it.card.name.lowercase() }

            CardSortOrder.RARITY_DESC -> compareByDescending<CardWithCollection> { it.card.isFavorite }
                .thenByDescending { rarityRank(it.card.rarity) }
                .thenBy { naturalNum(it.card.collectorNumber) }
                .thenBy { it.card.name }

            CardSortOrder.OWNED_DESC -> compareByDescending<CardWithCollection> { it.card.isFavorite }
                .thenByDescending { it.ownedQuantity }
                .thenBy { naturalNum(it.card.collectorNumber) }
                .thenBy { it.card.name }
        }
        return cards.sortedWith(comparator)
    }

    class Factory(
        private val repository: TcgRepository,
        private val initialGame: TcgGame = TcgGame.YUGIOH,
        private val onGamePersisted: (TcgGame) -> Unit = {}
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TcgViewModel(repository, initialGame, onGamePersisted) as T
        }
    }
}

package com.example.horairebusmihanbot

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.horairebusmihanbot.databinding.ActivityMainBinding
import com.example.horairebusmihanbot.services.DataRefreshManager
import com.example.horairebusmihanbot.services.PermissionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private lateinit var refreshManager: DataRefreshManager

    private var isSyncFragment = false

    // Pattern Facade : Encapsule la complexité des permissions
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            Log.w("PERM", "Certaines permissions ont été refusées par l'utilisateur.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialisation de la vue (Binding)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Initialisation des composants techniques
        setupNavigation()
        setupPermissions()
        refreshManager = DataRefreshManager(this)
    }

    /**
     * Centralise la configuration de la navigation.
     * Respecte le principe de "Single Responsibility" (S de SOLID).
     */
    private fun setupNavigation() {
        // Récupération propre du NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Toolbar setup
        setSupportActionBar(binding.toolbar)

        // AppBarConfiguration :
        // - Définit les destinations racines (sans bouton "retour")
        // - Lie le DrawerLayout pour gérer l'icône burger
        appBarConfiguration = AppBarConfiguration(
            setOf(),
            binding.drawerLayout
        )

        // Synchronisation automatique de la Toolbar et du Titre avec le NavGraph
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Observer sur les changements de destination (Pattern Observer)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            isSyncFragment = destination.id == R.id.fragment_sync

            // LOGIQUE DE VISIBILITÉ DU BURGER/BACK
            // Si vous voulez masquer complètement l'icône de navigation sur ces vues :
            if (destination.id == R.id.fragment_sync || destination.id == R.id.fragment_selection) {
                supportActionBar?.setDisplayHomeAsUpEnabled(false) // Masque l'icône (burger ou back)
            } else {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }

            invalidateOptionsMenu()
        }
    }

    private fun setupPermissions() {
        if (!PermissionManager.hasAllPermissions(this)) {
            requestPermissionsLauncher.launch(PermissionManager.requiredPermissions)
        }
    }

    // --- GESTION DU MENU ---

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Gonfle le menu (le bouton de refresh par exemple)
        menuInflater.inflate(R.menu.drawer_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // Masque le bouton de rafraîchissement si on est déjà en cours de synchro
        menu.findItem(R.id.refresh_database)?.isVisible = !isSyncFragment
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.refresh_database -> {
                refreshManager.showRefreshDialog(navController)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Gère la navigation retour et l'ouverture du Drawer.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
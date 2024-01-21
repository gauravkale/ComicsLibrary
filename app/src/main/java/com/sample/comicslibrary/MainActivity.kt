package com.sample.comicslibrary

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sample.comicslibrary.ui.theme.ComicsLibraryTheme
import com.sample.comicslibrary.view.CharacterDetailsScreen
import com.sample.comicslibrary.view.CharactersBottomNav
import com.sample.comicslibrary.view.CollectionScreen
import com.sample.comicslibrary.view.LibraryScreen
import com.sample.comicslibrary.viewmodel.CollectionDbViewModel
import com.sample.comicslibrary.viewmodel.LibraryApiViewModel
import dagger.hilt.android.AndroidEntryPoint


sealed class Destination(val route: String) {
    object Library : Destination("library")
    object Collection : Destination("collection")
    object CharacterDetail : Destination("character/{characterId}") {
        fun createRoute(characterId: Int?) = "character/$characterId"
    }
    object NewDetailsPage : Destination("new/{characterId}") {
        fun createRoute(characterId: Int?) = "new/$characterId"
    }
}


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val lvm by viewModels<LibraryApiViewModel>()
    private val cvm by viewModels<CollectionDbViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComicsLibraryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()
                    CharactersScaffold(navController = navController, lvm = lvm, cvm = cvm)
                }
            }
        }
    }


}

@Composable
fun CharactersScaffold(navController: NavHostController, lvm: LibraryApiViewModel, cvm: CollectionDbViewModel) {
    val scaffoldState = rememberScaffoldState()
    val ctx = LocalContext.current
    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = { CharactersBottomNav(navController = navController) }
    ) { paddingValues ->
        NavHost(navController = navController, startDestination = Destination.Library.route) {
            composable(Destination.Library.route) {
                LibraryScreen(navController = navController, lvm, paddingValues = paddingValues)
            }
            composable(Destination.Collection.route) {
                CollectionScreen(cvm = cvm, navController = navController)
            }
            composable(Destination.CharacterDetail.route) { navBackStackEntry ->
                val id =  navBackStackEntry.arguments?.getString("characterId")?.toIntOrNull()
                if (id == null)
                    Toast.makeText(ctx, "Character id is required",Toast.LENGTH_SHORT).show()
                else {
                    lvm.retrieveSingleCharacter(id)
                    CharacterDetailsScreen(
                        lvm = lvm,
                        cvm = cvm,
                        paddingValues = paddingValues,
                        navController = navController
                    )
                }
            }
            composable(Destination.NewDetailsPage.route) { navBackStackEntry ->
                val id = navBackStackEntry.arguments?.getString("characterId")?.toIntOrNull()
                if (id == null)
                    Toast.makeText(ctx,"Char id required",Toast.LENGTH_SHORT).show()

            }
        }
    }
}


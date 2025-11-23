package com.example.lab_week_9

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.lab_week_9.ui.theme.LAB_WEEK_9Theme
import com.example.lab_week_9.ui.theme.OnBackgroundItemText
import com.example.lab_week_9.ui.theme.OnBackgroundTitleText
import com.example.lab_week_9.ui.theme.PrimaryTextButton

import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_9Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(navController = navController)
                }
            }
        }
    }
}

@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { jsonEncoded ->
                navController.navigate("resultContent/?listData=$jsonEncoded")
            }
        }
        composable(
            route = "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") { type = NavType.StringType })
        ) {
            ResultContent(it.arguments?.getString("listData").orEmpty())
        }
    }
}

data class Student(var name: String)

@Composable
fun Home(
    navigateFromHomeToResult: (String) -> Unit
) {
    val listData = remember {
        mutableStateListOf(
            Student("Tanu"),
            Student("Tina"),
            Student("Tono")
        )
    }
    val inputField = remember { mutableStateOf(Student("")) }

    val moshi = remember {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    val listType = remember { Types.newParameterizedType(List::class.java, Student::class.java) }
    val adapter = remember { moshi.adapter<List<Student>>(listType) }

    HomeContent(
        listData = listData,
        inputField = inputField.value,
        onInputValueChange = { input ->
            inputField.value = inputField.value.copy(name = input)
        },
        onButtonClick = {
            val trimmed = inputField.value.name.trim()
            if (trimmed.isNotEmpty()) {
                listData.add(Student(trimmed))
                inputField.value = Student("")
            }
        },
        navigateFromHomeToResult = {
            val json = adapter.toJson(listData.toList())
            val encoded = Uri.encode(json)
            navigateFromHomeToResult(encoded)
        }
    )
}

@Composable
fun HomeContent(
    listData: SnapshotStateList<Student>,
    inputField: Student,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundTitleText(text = stringResource(id = R.string.enter_item))

                TextField(
                    value = inputField.name,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    onValueChange = { onInputValueChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )

                Row {
                    PrimaryTextButton(
                        text = stringResource(id = R.string.button_click)
                    ) { onButtonClick() }

                    PrimaryTextButton(
                        text = stringResource(id = R.string.button_navigate)
                    ) { navigateFromHomeToResult() }
                }
            }
        }

        items(listData) { item ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundItemText(text = item.name)
            }
        }
    }
}

@Composable
fun ResultContent(listData: String) {
    val decoded = remember(listData) { Uri.decode(listData) }
    val moshi = remember {
        Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    val listType = remember { Types.newParameterizedType(List::class.java, Student::class.java) }
    val adapter = remember { moshi.adapter<List<Student>>(listType) }
    val parsed: List<Student> = remember(decoded) {
        runCatching { adapter.fromJson(decoded) ?: emptyList() }.getOrElse { emptyList() }
    }

    LazyColumn(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(parsed) { student ->
            OnBackgroundItemText(text = student.name)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewApp() {
    LAB_WEEK_9Theme {
        val nav = rememberNavController()
        App(navController = nav)
    }
}